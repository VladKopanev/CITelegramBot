package com.vladkopaniev.ci.telegram.persistence.model

import java.util.UUID

final case class SubscriberInfo(subscriberId: UUID, chatId: Long)