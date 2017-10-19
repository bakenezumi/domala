package domala.internal.macros.reflect

import domala.internal.macros.reflect.util.TypeUtil._

import scala.reflect.macros.blackbox

sealed trait ParamType

object ParamType {
  case class Iterable[C <: blackbox.Context](c: C, elementDomaType: ParamType) extends ParamType
  case class Entity[C <: blackbox.Context](c: C, tpe: C#Type) extends ParamType
  case class Option[C <: blackbox.Context](c: C, elementDomaType: ParamType) extends ParamType
  case class Holder[C <: blackbox.Context](c: C, tpe: C#Type) extends ParamType
  case class Basic[C <: blackbox.Context](c: C, tpe: C#Type) extends ParamType
  case class Other[C <: blackbox.Context](c: C, tpe: C#Type) extends ParamType

  def convert[C <: blackbox.Context](c: C)(tpe: C#Type): ParamType = {
    tpe match {
      case t if isIterable(c)(t) => ParamType.Iterable(c, convert(c)(t.typeArgs.head))
      case t if isEntity(c)(t) => ParamType.Entity(c, t)
      case t if isOption(c)(t) => ParamType.Option(c, convert(c)(t.typeArgs.head))
      case t if isHolder(c)(t) => ParamType.Holder(c, t)
      case t if isBasic(c)(t) => ParamType.Basic(c, t)
      case _ => ParamType.Other(c, tpe)
    }
  }
}