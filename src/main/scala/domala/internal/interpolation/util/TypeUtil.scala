package domala.internal.interpolation.util

import java.math.BigInteger
import java.sql.{Blob, Clob, NClob, SQLXML, Time, Timestamp}
import java.time.{LocalDate, LocalDateTime, LocalTime}

import domala.jdbc.entity.{EmbeddableCompanion, EntityCompanion}
import domala.jdbc.holder.{AbstractHolderDesc, HolderCompanion}
import org.seasar.doma.jdbc.entity.EmbeddableType

import scala.collection.mutable.ArrayBuffer
import scala.reflect.runtime.universe._

object TypeUtil {

  def isBasic(tpe: Type): Boolean = {
    tpe =:= typeOf[String] ||
    tpe =:= typeOf[Boolean] ||
    tpe =:= typeOf[Byte] ||
    tpe =:= typeOf[Short] ||
    tpe =:= typeOf[Integer] ||
    tpe =:= typeOf[Int] ||
    tpe =:= typeOf[Long] ||
    tpe =:= typeOf[Float] ||
    tpe =:= typeOf[Double] ||
    tpe =:= typeOf[Object] ||
    tpe =:= typeOf[AnyRef] ||
    tpe =:= typeOf[Any] ||
    tpe =:= typeOf[BigDecimal] ||
    tpe =:= typeOf[BigInteger] ||
    tpe =:= typeOf[BigInt] ||
    tpe =:= typeOf[Time] ||
    tpe =:= typeOf[Timestamp] ||
    tpe =:= typeOf[java.sql.Date] ||
    tpe =:= typeOf[java.util.Date] ||
    tpe =:= typeOf[LocalTime] ||
    tpe =:= typeOf[LocalDateTime] ||
    tpe =:= typeOf[LocalDate] ||
    tpe =:= typeOf[Array[_]] ||
    tpe =:= typeOf[Blob] ||
    tpe =:= typeOf[NClob] ||
    tpe =:= typeOf[Clob] ||
    tpe =:= typeOf[SQLXML]
  }

  def isEntity(tpe: Type): Boolean = {
    tpe.companion <:< typeOf[EntityCompanion[_]]
  }

  def isHolder(tpe: Type): Boolean = {
    tpe.companion <:< typeOf[HolderCompanion[_, _]]
  }

  def isNumberHolder(tpe: Type): Boolean = {
    tpe.companion <:< typeOf[HolderCompanion[_ <: Number, _]]
  }

  def isAnyVal(tpe: Type): Boolean = {
    tpe <:< typeOf[AnyVal]
  }

  def isNumber(tpe: Type): Boolean = {
    tpe <:< typeOf[Number]
  }

  def isEmbeddable(tpe: Type): Boolean = {
    tpe.companion <:< typeOf[EmbeddableCompanion[_]]
  }

  def isIterable(tpe: Type): Boolean = {
    tpe <:< typeOf[Iterable[_]]
  }

  def isOption(tpe: Type): Boolean = {
    tpe <:< typeOf[Option[_]]
  }

  def isMap(tpe: Type): Boolean = {
    tpe =:= typeOf[Map[String, Any]] || tpe =:= typeOf[Map[String, AnyRef]] || tpe =:= typeOf[Map[String, Object]]
  }

  def isSeq(tpe: Type): Boolean = {
    val arrayBufferType: Type = weakTypeOf[ArrayBuffer[_]].erasure
    tpe <:< typeOf[Seq[_]] && arrayBufferType <:< tpe.erasure
  }

  def isFunction(tpe: Type): Boolean = {
    !(tpe =:= typeOf[Nothing]) && (
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
}
