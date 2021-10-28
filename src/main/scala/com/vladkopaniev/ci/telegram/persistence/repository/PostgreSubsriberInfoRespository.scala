package com.vladkopaniev.ci.telegram.persistence.repository

import cats.Monad
import com.vladkopaniev.ci.telegram.persistence.model.SubscriberInfo
import cats.effect.kernel.MonadCancelThrow

import java.util.UUID
import doobie.util.transactor.Transactor

class PostgreSubsriberInfoRespository[F[_]: MonadCancelThrow](xa: Transactor[F])
  extends SubsriberInfoRespository[F],
    PosPlatform,
    ReadPlatform:

  import cats.syntax.all._
  import doobie._
  import doobie.implicits._
  import doobie.postgres.implicits._

  override def save(si: SubscriberInfo): F[Unit] =
    sql"""INSERT INTO subscriber("chatid", "subscriberid") VALUES(${si.chatId}, ${si.subscriberId})""".update.run
      .transact(xa)
      .void

  override def find(subscriberId: UUID): F[Option[SubscriberInfo]] =
    sql"SELECT subscriberid, chatid FROM subscriber WHERE subscriberid = $subscriberId"
      .query[SubscriberInfo]
      .option
      .transact(xa)

  override def find(chatId: Long): F[Option[SubscriberInfo]] =
    sql"SELECT subscriberid, chatid FROM subscriber WHERE chatid = $chatId"
      .query[SubscriberInfo]
      .option
      .transact(xa)

  override def delete(chatId: Long): F[Unit] =
    sql"DELETE FROM subscriber WHERE chatid = $chatId".update.run
      .transact(xa)
      .void
