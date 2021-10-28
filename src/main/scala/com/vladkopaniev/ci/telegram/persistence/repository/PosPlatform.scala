package com.vladkopaniev.ci.telegram.persistence.repository

import doobie.util.pos.Pos
import scala.quoted.{ Expr, Quotes }

// backport from `doobie`, remove when project will only depend on libraries of Scala 3 version
trait PosPlatform {

  implicit inline def instance: Pos =
    ${PosPlatform.originImpl}

}

object PosPlatform {

  def originImpl(using ctx: Quotes): Expr[Pos] = {
    val rootPosition = ctx.reflect.Position.ofMacroExpansion
    val file = Expr(rootPosition.sourceFile.jpath.toString)
    val line = Expr(rootPosition.startLine + 1)
    '{Pos($file, $line)}
  }

}
