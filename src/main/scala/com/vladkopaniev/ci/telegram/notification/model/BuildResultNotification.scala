package com.vladkopaniev.ci.telegram.notification.model

import java.util.UUID
import cats.Show
import cats.syntax.show
import com.bot4s.telegram.methods.ParseMode

final case class BuildResultNotification(
  repoId: RepoId,
  subscriberId: SubscriberId,
  buildNumber: BuildNumber,
  commitMessage: CommitMessage,
  authorName: AuthorName,
  branch: BranchName,
  resultStatus: ResultStatus,
  failed: Boolean,
  buildUrl: BuildUrl
) extends HasSubscriberId

object BuildResultNotification:

  import unindent._
  given TelegramMarkup[BuildResultNotification] with
    extension(brn: BuildResultNotification) def markup: String = 
      val result = if (brn.failed) "Failed" else "Succeeded"
      i"""
      *Build* [#${brn.buildNumber}](${brn.buildUrl}) *$result*

      *Commit*: `${brn.commitMessage}`
      *Author*: ${brn.authorName}
      *Branch*: ${brn.branch}
      """
    extension(brn: BuildResultNotification) def parseMode: ParseMode.ParseMode = ParseMode.Markdown