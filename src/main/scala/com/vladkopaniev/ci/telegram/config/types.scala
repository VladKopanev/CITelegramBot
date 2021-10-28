package com.vladkopaniev.ci.telegram.config

import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.types.numeric.PosInt

final case class BotAppConfig(
  port: PosInt,
  databaseConfig: DatabaseConfig,
  telegramApiConfig: TelegramApiConfig,
  webhookConfig: WebhookConfig
)

final case class DatabaseConfig(
  url: NonEmptyString
)

final case class TelegramApiConfig(
  token: NonEmptyString
)

final case class WebhookConfig(
  hostBaseUrl: NonEmptyString,
  scheme: NonEmptyString
)