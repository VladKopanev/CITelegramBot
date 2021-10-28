package com.vladkopaniev.ci.telegram.persistence.repository

import com.vladkopaniev.ci.telegram.persistence.model.SubscriberInfo
import java.util.UUID

trait SubsriberInfoRespository[F[_]]:
  def save(si: SubscriberInfo): F[Unit]

  def find(subscriberId: UUID): F[Option[SubscriberInfo]]

  def find(chatId: Long): F[Option[SubscriberInfo]]

  def delete(chatId: Long): F[Unit]
