package domala.internal.macros.reflect

import domala.internal.macros.reflect.util.MacroTypeConverter
import domala.jdbc.`type`.Types

import scala.language.existentials
import scala.reflect.macros.blackbox

sealed trait ResultType

object ResultType {
  case object Map extends ResultType
  case class Seq[C <: blackbox.Context](c: C, elementDomaType: ResultType) extends ResultType
  case class Option[C <: blackbox.Context](c: C, elementDomaType: ResultType) extends ResultType
  case class Basic[C <: blackbox.Context](c: C, tpe: C#Type) extends ResultType
  case class GeneratedEntity[C <: blackbox.Context](c: C, tpe: C#Type) extends ResultType
  case class RuntimeEntity[C <: blackbox.Context](c: C, tpe: C#Type) extends ResultType
  case class GeneratedHolder[C <: blackbox.Context](c: C, tpe: C#Type) extends ResultType
  case class AnyValHolder[C <: blackbox.Context](c: C, tpe: C#Type) extends ResultType
  case class UnSupport[C <: blackbox.Context](c: C, tpe: C#Type) extends ResultType

  def convert[C <: blackbox.Context](c: C)(tpe: C#Type): ResultType = {
    MacroTypeConverter.of(c).toType(tpe) match {
      case Types.Map => ResultType.Map
      case Types.Seq(_) => ResultType.Seq(c, convert(c)(tpe.typeArgs.head.asInstanceOf[C#Type]))
      case Types.Option(_) => ResultType.Option(c, convert(c)(tpe.typeArgs.head.asInstanceOf[C#Type]))
      case t if t.isBasic => ResultType.Basic(c, tpe)
      case Types.GeneratedEntityType => ResultType.GeneratedEntity(c, tpe)
      case Types.MacroEntityType => ResultType.RuntimeEntity(c, tpe)
      case Types.GeneratedHolderType(_) => ResultType.GeneratedHolder(c, tpe)
      case Types.AnyValHolderType(_) => ResultType.AnyValHolder(c, tpe)
      case _ => ResultType.UnSupport(c, tpe)
    }
  }
}