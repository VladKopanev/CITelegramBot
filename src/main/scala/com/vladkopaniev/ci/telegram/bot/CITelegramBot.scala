package com.vladkopaniev.ci.telegram.bot

import cats.effect.MonadCancelThrow
import cats.effect.kernel.Sync
import cats.syntax.monad.*
import cats.syntax.flatMap.*
import cats.syntax.show.*
import com.bot4s.telegram.api.ChatActions
import com.bot4s.telegram.models.Message
import com.bot4s.telegram.api.declarative.{
  Action,
  CommandFilterMagnet,
  Commands
}
import com.bot4s.telegram.cats.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.{
  ChatId,
  KeyboardButton,
  Message,
  ReplyKeyboardMarkup
}
import com.vladkopaniev.ci.telegram.notification.BuildInfoNotifyer
import com.vladkopaniev.ci.telegram.notification.model.{
  HasSubscriberId,
  SubscriberId,
  TelegramMarkup
}
import sttp.client3.SttpBackend
import cats.syntax.functor.*
import com.vladkopaniev.ci.telegram.bot.CITelegramBot.*
import com.vladkopaniev.ci.telegram.persistence.repository.SubsriberInfoRespository
import com.vladkopaniev.ci.telegram.persistence.model.SubscriberInfo
import com.bot4s.telegram.models.InlineKeyboardMarkup
import com.bot4s.telegram.models.InlineKeyboardButton
import com.bot4s.telegram.api.declarative.Callbacks

import scala.collection.mutable
import java.util.concurrent.ConcurrentHashMap
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.api.Polling
import java.util.UUID
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.data.OptionT
import cats.Show
import com.bot4s.telegram.models.ReplyMarkup
import com.bot4s.telegram.methods.ParseMode
import com.vladkopaniev.ci.telegram.config.BotAppConfig
import eu.timepit.refined.auto._

class CITelegramBot[F[_]: Sync, N <: HasSubscriberId: TelegramMarkup] private (
  botAppConfig: BotAppConfig,
  subscriberInfoRepository: SubsriberInfoRespository[F],
  backend: SttpBackend[F, Any],
  logger: Logger[F]
)(using MCT: MonadCancelThrow[F])
  extends TelegramBot[F](botAppConfig.telegramApiConfig.token, backend),
    Commands[F],
    Polling[F],
    Callbacks[F],
    BuildInfoNotifyer[F, N]:

  override def notify(notification: N): F[Unit] =
    OptionT(subscriberInfoRepository.find(notification.subscriberId))
      .foldF(
        logger.warn(
          s"Subscriber not found : subscriberId - ${notification.subscriberId}"
        )
      ) { subscriber =>
        request(
          SendMessage(
            subscriber.chatId,
            notification.markup,
            parseMode = Some(notification.parseMode)
          )
        ).void
      }

  onCommand("subscribe") { implicit msg =>
    for
      _ <- createSubscriberWhenNew(msg.chat.id)
      _ <- reply(
        "Please choose your CI tool type:",
        replyMarkup = Some(SupportedCiBuildToolsKeyboard)
      ).void
    yield ()
  }

  onCommand("unsubscribe") { implicit msg =>
    for
      _ <- subscriberInfoRepository.delete(msg.chat.id)
      _ <- reply("Done").void
    yield ()
  }

  onCallbackQuery { callback =>
    (for
      msg <- callback.message
      cbd <- callback.data
      ciToolType = cbd.trim
    yield
      given message: Message = msg
      CiToolTypesAndCallbackData
        .get(ciToolType)
        .fold(
          reply(s"CI build tool type $ciToolType is not supported!").void
        ) { replyText =>
          OptionT(subscriberInfoRepository.find(msg.chat.id))
            .foldF(
              logger.warn(
                s"No subscriptions found for chat: chatId - ${msg.chat.id}"
              )
            ) { subscriberInfo =>
              reply(replyText.format(subscriberInfo.subscriberId.toString)).void
            }
        }
    ).getOrElse(logger.warn("Message or callback data was empty"))
  }

  private def createSubscriberWhenNew(chatId: Long): F[Unit] =
    subscriberInfoRepository.find(chatId).flatMap { maybeSubscriberInfo =>
      if (maybeSubscriberInfo.isEmpty) then
        (for
          newSubscriberId <- Sync[F].delay(UUID.randomUUID)
          _ <- subscriberInfoRepository
            .save(SubscriberInfo(newSubscriberId, chatId))
        yield ())
      else MCT.unit
    }


  import unindent._

  private val webhookConfig = botAppConfig.webhookConfig
  private val CiToolTypesAndCallbackData: Map[String, String] = Map(
    "travisci" -> i"""Use this url in your travisci webhook notification config: 
      '${webhookConfig.scheme}://${webhookConfig.hostBaseUrl}/travis/build/notify/subscriber/%s'"""
  )

object CITelegramBot:

  def create[F[_]: Sync, N <: HasSubscriberId: TelegramMarkup](
    botAppConfig: BotAppConfig,
    subscriberInfoRepository: SubsriberInfoRespository[F],
    backend: SttpBackend[F, Any]
  )(using
    MCT: MonadCancelThrow[F]
  ): F[CITelegramBot[F, N]] =
    Slf4jLogger
      .fromName[F]("CITelegramBot")
      .map(CITelegramBot(botAppConfig, subscriberInfoRepository, backend, _))

  private val SupportedCiBuildToolTypes = Seq("travisci")

  private val SupportedCiBuildToolsKeyboard = InlineKeyboardMarkup
    .singleColumn(buttonColumn =
      SupportedCiBuildToolTypes
        .map(tpe => InlineKeyboardButton.callbackData(tpe, tpe))
        .to(Seq)
    )
