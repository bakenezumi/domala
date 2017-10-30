package domala.internal.macros.reflect.util

import java.math.BigInteger
import java.sql.{Blob, Clob, NClob, SQLXML, Time, Timestamp}
import java.time.{LocalDate, LocalDateTime, LocalTime}

import org.seasar.doma.jdbc.domain.AbstractDomainType
import org.seasar.doma.jdbc.entity.AbstractEntityType

import scala.collection.mutable.ArrayBuffer
import scala.reflect.macros.blackbox

object TypeUtil {

  def isBasic[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
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

  def isEntity[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe.companion <:< typeOf[AbstractEntityType[_]]
  }

  def isHolder[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe.companion <:< typeOf[AbstractDomainType[_, _]]
  }

  def isIterable[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe <:< typeOf[Iterable[_]]
  }

  def isOption[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe <:< typeOf[Option[_]]
  }

  def isMap[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe =:= typeOf[Map[String, Any]] || tpe =:= typeOf[Map[String, AnyRef]] || tpe =:= typeOf[Map[String, Object]]
  }

  def isSeq[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    val arrayBufferType: C#Type = weakTypeOf[ArrayBuffer[_]].erasure
    tpe <:< typeOf[Seq[_]] && arrayBufferType <:< tpe.erasure
  }

  def isFunction[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
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
