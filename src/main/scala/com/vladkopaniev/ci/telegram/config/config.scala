package com.vladkopaniev.ci.telegram.config

import cats.Show
import cats.effect.Async
import ciris.*
import eu.timepit.refined.api.{RefType, Validate}
import eu.timepit.refined.auto.*
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.types.numeric.PosInt
import org.tpolecat.typename.TypeName

//switch to safer APIs like `ciris` once all dependencies migrate to Scala 3
def loadBotConfig: BotAppConfig = BotAppConfig(
  port = PosInt.unsafeFrom(sys.env.get("PORT").fold(8080)(_.toInt)),
  databaseConfig = DatabaseConfig(url =
    NonEmptyString.unsafeFrom(
      sys.env
        .get("JDBC_DATABASE_URL")
        .fold("jdbc:postgresql:subscribers")(identity)
    )
  ),
  telegramApiConfig = TelegramApiConfig(token =
    NonEmptyString.unsafeFrom(sys.env.getOrElse("BOT_API_TOKEN", "token"))
  ),
  webhookConfig = WebhookConfig(
    NonEmptyString.unsafeFrom(
      sys.env.getOrElse("WEBHOOK_HOST_BASE_URL", "localhost")
    ),
    NonEmptyString.unsafeFrom(
      sys.env.getOrElse("WEBHOOK_REQUEST_URL_SCHEME", "https")
    )
  )
)

implicit final def refTypeConfigDecoder[F[_, _], A, B, P](implicit
  decoder: ConfigDecoder[A, B],
  refType: RefType[F],
  show: Show[B],
  validate: Validate[B, P],
  typeName: TypeName[F[B, P]]
): ConfigDecoder[A, F[B, P]] = {
  val refine = refType.refine[P]
  decoder.mapOption(typeName.value)(refine(_).toOption)
}
