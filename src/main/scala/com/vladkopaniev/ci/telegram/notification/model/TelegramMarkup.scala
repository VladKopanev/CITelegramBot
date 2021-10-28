package com.vladkopaniev.ci.telegram.notification.model


import com.bot4s.telegram.methods.ParseMode

trait TelegramMarkup[M]:
  extension (m: M) def markup: String
  extension (m: M) def parseMode: ParseMode.ParseMode
