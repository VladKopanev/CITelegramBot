package com.vladkopaniev.ci.telegram.web.model

import com.vladkopaniev.ci.telegram.notification.model.BuildResultNotification
import com.vladkopaniev.ci.telegram.notification.model.SubscriberId
import io.circe.HCursor

final case class TravisCIBuildResultInfo(
  id: BuildId,
  number: BuildNumer,
  status: Status,
  result: Result,
  message: Message,
  authorName: AuthorName,
  branch: BranchName,
  statusMessage: StatusMessage,
  buildUrl: BuildUrl,
  repository: Repository
)

final case class Repository(id: RepositoryId)

object TravisCIBuildResultInfo:
  import io.circe.Decoder

  given Decoder[TravisCIBuildResultInfo] with
    def apply(c: HCursor): Decoder.Result[TravisCIBuildResultInfo] =
      for
        id            <- c.downField("id").as[Long]
        number        <- c.downField("number").as[String]
        status        <- c.downField("status").as[Byte]
        result        <- c.downField("result").as[Byte]
        message       <- c.downField("message").as[String]
        authorName    <- c.downField("author_name").as[String]
        branch        <- c.downField("branch").as[String]
        statusMessage <- c.downField("status_message").as[String]
        repoId        <- c.downField("repository").downField("id").as[Long]
        buildUrl      <- c.downField("build_url").as[String]
      yield TravisCIBuildResultInfo(
        id = id,
        number = number,
        status = status,
        result = result,
        message = message,
        authorName = authorName,
        branch = branch,
        statusMessage = statusMessage,
        buildUrl = buildUrl,
        repository = Repository(repoId)
      )

  val TravisCiFailedStatuses =
    Seq("failed", "errored", "broken", "still failing")

  extension (bri: TravisCIBuildResultInfo)
    def asNotification(subscriberId: SubscriberId): BuildResultNotification =
      BuildResultNotification(
        repoId = bri.repository.id.toString,
        subscriberId = subscriberId,
        buildNumber = bri.number,
        commitMessage = bri.message,
        authorName = bri.authorName,
        branch = bri.branch,
        bri.statusMessage,
        failed = TravisCiFailedStatuses.contains(bri.statusMessage.trim),
        bri.buildUrl
      )
