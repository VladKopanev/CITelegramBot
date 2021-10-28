package com.vladkopaniev.ci.telegram.config

import cats.effect.Async
import ciris._
import ciris.refined._
import eu.timepit.refined.auto._

import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.types.numeric.PosInt

//switch to safer APIs like `ciris` once all dependencies migrate to Scala 3
def loadBotConfig: BotAppConfig = BotAppConfig(
    port = PosInt.unsafeFrom(sys.env.get("PORT").fold(8080)(_.toInt)),
    databaseConfig = DatabaseConfig(url = NonEmptyString.unsafeFrom(
        sys.env
          .get("JDBC_DATABASE_URL")
          .fold("jdbc:postgresql:subscribers")(identity)
      )
    ),
    telegramApiConfig = TelegramApiConfig(token =  NonEmptyString.unsafeFrom(sys.env.getOrElse("BOT_API_TOKEN", "token"))),
    webhookConfig = WebhookConfig(
        NonEmptyString.unsafeFrom(sys.env.getOrElse("WEBHOOK_HOST_BASE_URL", "localhost")),
        NonEmptyString.unsafeFrom(sys.env.getOrElse("WEBHOOK_REQUEST_URL_SCHEME", "https"))
    )
)