package com.vladkopaniev.ci.telegram.notification

trait BuildInfoNotifyer[F[_], BI]:
  def notify(buildInfo: BI): F[Unit]
