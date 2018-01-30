package domala.internal.reflect.util

import java.math.BigInteger
import java.sql.{Blob, Clob, NClob, SQLXML, Time, Timestamp}
import java.time.{LocalDate, LocalDateTime, LocalTime}

import domala.jdbc.`type`.TypeConverter


object ClassTypeConverter extends TypeConverter {
  type T = Class[_]

  override def hasParam(tpe: T): Boolean = false

  override def headParamType(tpe: T): T = classOf[Nothing]

  override def typeArgs(tpe: T): List[T] = Nil

  override def isString(tpe: T): Boolean = tpe == classOf[String]

  override def isBoolean(tpe: T): Boolean = tpe == classOf[Boolean]

  override def isByte(tpe: T): Boolean = tpe == classOf[Byte] || tpe == classOf[java.lang.Byte]

  override def isShort(tpe: T): Boolean = tpe == classOf[Short] || tpe == classOf[java.lang.Short]

  override def isInt(tpe: T): Boolean = tpe == classOf[Int] || tpe == classOf[java.lang.Integer]

  override def isLong(tpe: T): Boolean = tpe == classOf[Long] || tpe == classOf[java.lang.Long]

  override def isFloat(tpe: T): Boolean = tpe == classOf[Float] || tpe == classOf[java.lang.Float]

  override def isDouble(tpe: T): Boolean = tpe == classOf[Double] || tpe == classOf[java.lang.Double]

  override def isAny(tpe: T): Boolean = tpe == classOf[Any] || tpe == classOf[Object]  || tpe == classOf[AnyRef]

  override def isBigDecimal(tpe: T): Boolean = tpe == classOf[BigDecimal]

  override def isJavaBigDecimal(tpe: T): Boolean = tpe == classOf[java.math.BigDecimal]

  override def isBigInt(tpe: T): Boolean = tpe == classOf[BigInt]

  override def isBigInteger(tpe: T): Boolean = tpe == classOf[BigInteger]

  override def isTime(tpe: T): Boolean = tpe == classOf[Time]

  override def isTimestamp(tpe: T): Boolean = tpe == classOf[Timestamp]

  override def isDate(tpe: T): Boolean = tpe == classOf[java.sql.Date]

  override def isUtilDate(tpe: T): Boolean = tpe == classOf[java.util.Date]

  override def isLocalTime(tpe: T): Boolean = tpe == classOf[LocalTime]

  override def isLocalDateTime(tpe: T): Boolean = tpe == classOf[LocalDateTime]

  override def isLocalDate(tpe: T): Boolean = tpe == classOf[LocalDate]

  override def isBytes(tpe: T): Boolean = tpe == classOf[Array[Byte]]

  override def isBlob(tpe: T): Boolean = tpe == classOf[Blob]

  override def isNClob(tpe: T): Boolean = tpe == classOf[NClob]

  override def isClob(tpe: T): Boolean = tpe == classOf[Clob]

  override def isSQLXML(tpe: T): Boolean = tpe == classOf[SQLXML]

  override def isGeneratedEntity(tpe: T): Boolean = false // Can not be determined

  override def isRuntimeEntity(tpe: T): Boolean = false // Can not be determined

  override def isGeneratedHolder(tpe: T): Boolean = false // Can not be determined

  override def isAnyValHolder(tpe: T): Boolean = false // Can not be determined

  override def isMap(tpe: T): Boolean = tpe == classOf[Map[String, Any]] || tpe == classOf[Map[String, AnyRef]] || tpe == classOf[Map[String, Object]]

  override def isOption(tpe: T): Boolean = tpe == classOf[Option[_]]

  override def isSeq(tpe: T): Boolean = tpe == classOf[Seq[_]]

  override def isIterable(tpe: T): Boolean = tpe == classOf[Iterable[_]]

  override def isFunction(tpe: T): Boolean =
    tpe == classOf[() => _] ||
    tpe == classOf[(_) => _] ||
    tpe == classOf[(_, _) => _] ||
    tpe == classOf[(_, _, _) => _] ||
    tpe == classOf[(_, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) => _] ||
    tpe == classOf[PartialFunction[_, _]]

}
