package domala.internal.macros.reflect.util

import java.math.BigInteger
import java.sql.{Blob, Clob, NClob, SQLXML, Time, Timestamp}
import java.time.{LocalDate, LocalDateTime, LocalTime}

import org.seasar.doma.jdbc.domain.AbstractDomainType

import scala.reflect.macros.blackbox

object TypeUtil {

  def isBasic[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe <:< typeOf[String] ||
    tpe <:< typeOf[Boolean] ||
    tpe <:< typeOf[Byte] ||
    tpe <:< typeOf[Short] ||
    tpe <:< typeOf[Integer] ||
    tpe <:< typeOf[Int] ||
    tpe <:< typeOf[Long] ||
    tpe <:< typeOf[Float] ||
    tpe <:< typeOf[Double] ||
    tpe <:< typeOf[Object] ||
    tpe <:< typeOf[BigDecimal] ||
    tpe <:< typeOf[BigInteger] ||
    tpe <:< typeOf[BigInt] ||
    tpe <:< typeOf[Time] ||
    tpe <:< typeOf[Timestamp] ||
    tpe <:< typeOf[java.sql.Date] ||
    tpe <:< typeOf[java.util.Date] ||
    tpe <:< typeOf[LocalTime] ||
    tpe <:< typeOf[LocalDateTime] ||
    tpe <:< typeOf[LocalDate] ||
    tpe <:< typeOf[Array[_]] ||
    tpe <:< typeOf[Blob] ||
    tpe <:< typeOf[NClob] ||
    tpe <:< typeOf[Clob] ||
    tpe <:< typeOf[SQLXML]
  }

  def isDomain[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe.companion <:< typeOf[AbstractDomainType[_, _]]
  }

  def isIterable[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    tpe <:< typeOf[Iterable[_]]
  }

  def isFunction[C <: blackbox.Context](c: C)(tpe: C#Type): Boolean = {
    import c.universe._
    !(tpe =:= typeOf[Nothing]) && (
      tpe <:< typeOf[Function0[_]] ||
      tpe <:< typeOf[Function1[_, _]] ||
      tpe <:< typeOf[Function2[_, _, _]] ||
      tpe <:< typeOf[Function3[_, _, _, _]] ||
      tpe <:< typeOf[Function4[_, _, _, _, _]] ||
      tpe <:< typeOf[Function5[_, _, _, _, _, _]] ||
      tpe <:< typeOf[Function6[_, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function7[_, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function8[_, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function9[_, _, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function10[_, _, _, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function11[_, _, _, _, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function12[_, _, _, _, _, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function13[_, _, _, _, _, _, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function14[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[Function22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]] ||
      tpe <:< typeOf[PartialFunction[_, _]])
  }
}
