package domala.internal.macros.reflect.util

import java.math.BigInteger
import java.sql.{Blob, Clob, NClob, SQLXML, Time, Timestamp}
import java.time.{LocalDate, LocalDateTime, LocalTime}

import domala.jdbc.`type`.{TypeConverter, Types}
import domala.jdbc.entity.EntityCompanion
import domala.jdbc.holder.HolderCompanion

import scala.reflect.macros.blackbox

class MacroTypeConverter[C <: blackbox.Context](c: C) extends TypeConverter {
  import c.universe._
  type T = C#Type

  override def hasParam(tpe: T): Boolean =
    tpe.members.exists(_.isConstructor) &&
    tpe.members.find(_.isConstructor).get.asMethod.paramLists.flatten.nonEmpty

  override def headParamType(tpe: T): T =
    tpe.members.find(_.isConstructor).get.asMethod.paramLists.flatten.head.typeSignature.asInstanceOf[T]

  override def typeArgs(tpe: T): List[T] = tpe.typeArgs.asInstanceOf[List[T]]

  override def isString(tpe: T): Boolean = tpe =:= typeOf[String]

  override def isBoolean(tpe: T): Boolean = tpe =:= typeOf[Boolean] || tpe =:= typeOf[java.lang.Boolean]

  override def isByte(tpe: T): Boolean = tpe =:= typeOf[Byte] || tpe =:= typeOf[java.lang.Byte]

  override def isShort(tpe: T): Boolean = tpe =:= typeOf[Short] || tpe =:= typeOf[java.lang.Short]

  override def isInt(tpe: T): Boolean = tpe =:= typeOf[Int] || tpe =:= typeOf[java.lang.Integer]

  override def isLong(tpe: T): Boolean = tpe =:= typeOf[Long] || tpe =:= typeOf[java.lang.Long]

  override def isFloat(tpe: T): Boolean = tpe =:= typeOf[Float] || tpe =:= typeOf[java.lang.Float]

  override def isDouble(tpe: T): Boolean = tpe =:= typeOf[Double] || tpe =:= typeOf[java.lang.Double]

  override def isAny(tpe: T): Boolean = tpe =:= typeOf[Any] || tpe =:= typeOf[Object]  || tpe =:= typeOf[AnyRef]

  override def isBigDecimal(tpe: T): Boolean = tpe =:= typeOf[BigDecimal]

  override def isJavaBigDecimal(tpe: T): Boolean = tpe =:= typeOf[java.math.BigDecimal]

  override def isBigInt(tpe: T): Boolean = tpe =:= typeOf[BigInt]

  override def isBigInteger(tpe: T): Boolean = tpe =:= typeOf[BigInteger]

  override def isTime(tpe: T): Boolean = tpe =:= typeOf[Time]

  override def isTimestamp(tpe: T): Boolean = tpe =:= typeOf[Timestamp]

  override def isDate(tpe: T): Boolean = tpe =:= typeOf[java.sql.Date]

  override def isUtilDate(tpe: T): Boolean = tpe =:= typeOf[java.util.Date]

  override def isLocalTime(tpe: T): Boolean = tpe =:= typeOf[LocalTime]

  override def isLocalDateTime(tpe: T): Boolean = tpe =:= typeOf[LocalDateTime]

  override def isLocalDate(tpe: T): Boolean = tpe =:= typeOf[LocalDate]

  override def isBytes(tpe: T): Boolean = tpe =:= typeOf[Array[Byte]]

  override def isBlob(tpe: T): Boolean = tpe =:= typeOf[Blob]

  override def isNClob(tpe: T): Boolean = tpe =:= typeOf[NClob]

  override def isClob(tpe: T): Boolean = tpe =:= typeOf[Clob]

  override def isSQLXML(tpe: T): Boolean = tpe =:= typeOf[SQLXML]

  override def isGeneratedEntity(tpe: T): Boolean = tpe.companion <:< typeOf[EntityCompanion[_]]

  override def isRuntimeEntity(tpe: T): Boolean = !(tpe <:< typeOf[AnyVal]) && tpe.typeSymbol.asClass.isCaseClass && {
    val constructor = tpe.decl(termNames.CONSTRUCTOR).asMethod
    constructor.paramLists.flatten.forall { p =>
      this.toType(p.typeSignature.asInstanceOf[T]) match {
        case _: Types.Basic[_] => true
        case _: Types.Holder[_, _] => true
        case Types.Option(_: Types.Basic[_]) => true
        case Types.Option(_: Types.Holder[_, _]) => true
        case t if t.isMacroEmbeddable => true
        case _ => false
      }
    }
  }

  override def isGeneratedHolder(tpe: T): Boolean = tpe.companion <:< typeOf[HolderCompanion[_, _]]

  override def isAnyValHolder(tpe: T): Boolean = tpe <:< typeOf[AnyVal] && hasParam(tpe) && this.toType(headParamType(tpe)).isBasic

  override def isMap(tpe: T): Boolean = tpe <:< typeOf[Map[String, Any]] || tpe <:< typeOf[Map[String, AnyRef]] || tpe <:< typeOf[Map[String, Object]]

  override def isOption(tpe: T): Boolean = tpe <:< typeOf[Option[_]]

  override def isSeq(tpe: T): Boolean = tpe <:< typeOf[Seq[_]]

  override def isIterable(tpe: T): Boolean = tpe <:< typeOf[Iterable[_]]

  override def isFunction(tpe: T): Boolean = !(tpe =:= typeOf[Nothing]) && (
    tpe <:< typeOf[() => _] ||
      tpe <:< typeOf[(_) => _] ||
      tpe <:< typeOf[(_, _) => _] ||
      tpe <:< typeOf[(_, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _) =>_] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
      tpe <:< typeOf[PartialFunction[_, _]])

}

object MacroTypeConverter {
  def of[C <: blackbox.Context](c: C) = new MacroTypeConverter[C](c)
}