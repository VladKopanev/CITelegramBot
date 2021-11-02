package com.vladkopaniev.ci.telegram.web

import cats.effect.kernel.{Concurrent, Async}
import cats.implicits.*
import com.vladkopaniev.ci.telegram.notification.BuildInfoNotifyer
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import io.circe.*
import io.circe.syntax.*
import io.circe.parser.decode
import com.vladkopaniev.ci.telegram.web.model.TravisCIBuildResultInfo

import com.vladkopaniev.ci.telegram.notification.model.BuildResultNotification
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import java.util.UUID

class BotWebhookController[F[_]: Concurrent] private (
  notifier: BuildInfoNotifyer[F, BuildResultNotification],
  logger: Logger[F]
) extends Http4sDsl[F]:

  def ciWebHookRoute: HttpRoutes[F] =
    import org.http4s.circe.CirceEntityDecoder._
    HttpRoutes.of[F] {
      case req @ POST -> Root / "travis" / "build" / "notify" / "subscriber" / UUIDVar(subscriberId) =>
        for
          form <- req.as[UrlForm]
          maybePayload = form.values.get("payload").flatMap(_.headOption)
          _ <- maybePayload.fold(
            logger.warn("Payload was absscent in callback request!")
          ) { payload =>
            Concurrent[F]
              .fromEither(decode[TravisCIBuildResultInfo](payload))
              .flatMap(webhookData => notifier.notify(webhookData.asNotification(subscriberId)))
          }
          result <- Ok("ok")
        yield result
    }

object BotWebhookController:
  def create[F[_]: Async](
    bin: BuildInfoNotifyer[F, BuildResultNotification]
  ): F[BotWebhookController[F]] =
    Slf4jLogger
      .fromName[F](
        "BotWebhookController"
      ) //we use from name because 2.13 macro version is not available
      .map(BotWebhookController(bin, _))
