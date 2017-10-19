package domala.internal.macros.reflect

import domala.internal.macros.reflect.util.TypeUtil._

import scala.reflect.macros.blackbox

sealed trait ResultType

object ResultType {
  case object Map extends ResultType
  case class Seq[C <: blackbox.Context](c: C, elementDomaType: ResultType) extends ResultType
  case class Option[C <: blackbox.Context](c: C, elementDomaType: ResultType) extends ResultType
  case class Basic[C <: blackbox.Context](c: C, tpe: C#Type) extends ResultType
  case class Entity[C <: blackbox.Context](c: C, tpe: C#Type) extends ResultType
  case class Holder[C <: blackbox.Context](c: C, tpe: C#Type) extends ResultType
  case class UnSupport[C <: blackbox.Context](c: C, tpe: C#Type) extends ResultType

  def convert[C <: blackbox.Context](c: C)(tpe: C#Type): ResultType = {
    tpe match {
      case t if isMap(c)(t) => ResultType.Map
      case t if isSeq(c)(t) => ResultType.Seq(c, convert(c)(t.typeArgs.head))
      case t if isOption(c)(t) => ResultType.Option(c, convert(c)(t.typeArgs.head))
      case t if isBasic(c)(t) => ResultType.Basic(c, t)
      case t if isEntity(c)(t) => ResultType.Entity(c, t)
      case t if isHolder(c)(t) => ResultType.Holder(c, t)
      case _ => ResultType.UnSupport(c, tpe)
    }
  }
}