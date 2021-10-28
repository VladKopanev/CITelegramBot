package com.vladkopaniev.ci.telegram

import com.vladkopaniev.ci.telegram.bot.CITelegramBot
import com.vladkopaniev.ci.telegram.notification.model.BuildResultNotification
import com.vladkopaniev.ci.telegram.web.BotWebhookController
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import com.vladkopaniev.ci.telegram.persistence.repository.PostgreSubsriberInfoRespository
import com.vladkopaniev.ci.telegram.config._
import cats.effect.instances.*
import cats.effect.ResourceApp
import cats.effect.ExitCode
import cats.effect.*
import cats.effect.syntax.resource.*
import cats.implicits.*
import org.http4s.HttpRoutes
import org.http4s.syntax.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.blaze.server.BlazeServerBuilder
import scala.concurrent.ExecutionContext.global
import doobie.util.transactor.Transactor
import doobie.util.ExecutionContexts
import doobie.hikari.HikariTransactor
import com.zaxxer.hikari.HikariConfig

object Application extends ResourceApp.Forever:

  import eu.timepit.refined.auto._
  def run(args: List[String]): Resource[IO, Unit] = for
    config <- Resource.eval(IO(loadBotConfig))
    botClientBackend <- AsyncHttpClientCatsBackend.resource[IO]()
    tx               <- transactor(config.databaseConfig.url)
    subscriberRepo = PostgreSubsriberInfoRespository(tx)
    bot <- CITelegramBot
      .create[IO, BuildResultNotification](config, subscriberRepo, botClientBackend)
      .toResource
    controller <- BotWebhookController.create(bot).toResource
    _          <- Resource.make(bot.run().start)(_ => IO.blocking(bot.shutdown))
    _ <- BlazeServerBuilder[IO](global)
      .bindHttp(config.port, "0.0.0.0")
      .withHttpApp(controller.ciWebHookRoute.orNotFound)
      .resource
  yield ExitCode.Success

  def transactor(jdbcUrl: String): Resource[IO, HikariTransactor[IO]] =
    for
      ce <- ExecutionContexts.fixedThreadPool[IO](32)
      hikariConf = HikariConfig()
      _ = hikariConf.setJdbcUrl(jdbcUrl)
      _ = hikariConf.setDriverClassName("org.postgresql.Driver")
      xa <- HikariTransactor.fromHikariConfig[IO](hikariConf, ce)
    yield xa
