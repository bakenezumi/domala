package domala.internal.macros.reflect

import domala.internal.macros.reflect.util.MacroTypeConverter
import domala.jdbc.`type`.Types
import scala.language.existentials

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
    MacroTypeConverter.of(c).toType(tpe) match {
      case Types.Iterable(_) => ParamType.Iterable(c, convert(c)(tpe.typeArgs.head.asInstanceOf[C#Type]))
      case Types.GeneratedEntityType => ParamType.Entity(c, tpe)
      case Types.Option(_) => ParamType.Option(c, convert(c)(tpe.typeArgs.head.asInstanceOf[C#Type]))
      case t if t.isHolder => ParamType.Holder(c, tpe)
      case t if t.isBasic  => ParamType.Basic(c, tpe)
      case _ => ParamType.Other(c, tpe)
    }
  }
}
