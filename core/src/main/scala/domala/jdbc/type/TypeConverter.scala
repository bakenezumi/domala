package domala.jdbc.`type`

abstract class TypeConverter {
  type T

  protected def hasParam(tpe: T): Boolean

  protected def headParamType(tpe: T): T

  def toType(tpe: T): Types = tpe match {
    case _ if isString(tpe) => Types.StringType
    case _ if isBoolean(tpe) => Types.BooleanType
    case _ if isByte(tpe) => Types.ByteType
    case _ if isShort(tpe) => Types.ShortType
    case _ if isInt(tpe) => Types.IntType
    case _ if isLong(tpe) => Types.LongType
    case _ if isFloat(tpe) => Types.FloatType
    case _ if isDouble(tpe) => Types.DoubleType
    case _ if isAny(tpe) => Types.AnyType
    case _ if isBigDecimal(tpe) => Types.BigDecimalType
    case _ if isJavaBigDecimal(tpe) => Types.JavaBigDecimalType
    case _ if isBigInt(tpe) => Types.BigIntType
    case _ if isBigInteger(tpe) => Types.BigIntegerType
    case _ if isTime(tpe) => Types.TimeType
    case _ if isTimestamp(tpe) => Types.TimestampType
    case _ if isDate(tpe) => Types.DateType
    case _ if isUtilDate(tpe) => Types.UtilDateType
    case _ if isLocalTime(tpe) => Types.LocalTimeType
    case _ if isLocalDateTime(tpe) => Types.LocalDateTimeType
    case _ if isLocalDate(tpe) => Types.LocalDateType
    case _ if isBytes(tpe) => Types.BytesType
    case _ if isBlob(tpe) => Types.BlobType
    case _ if isNClob(tpe) => Types.NClobType
    case _ if isClob(tpe) => Types.ClobType
    case _ if isSQLXML(tpe) => Types.SQLXMLType
    case _ if isGeneratedEntity(tpe) => Types.GeneratedEntityType
    case _ if isGeneratedHolder(tpe) => Types.GeneratedHolderType(toType(headParamType(tpe)).asInstanceOf[Types.Basic[_]])
    case _ if isAnyValHolder(tpe) => Types.AnyValHolderType(toType(headParamType(tpe)).asInstanceOf[Types.Basic[_]])
    case _ if isRuntimeEntity(tpe) => Types.MacroEntityType
    case _ if isMap(tpe) => Types.Map
    case _ if isOption(tpe) => Types.Option(toType(typeArgs(tpe).head))
    case _ if isSeq(tpe) => Types.Seq(toType(typeArgs(tpe).head))
    case _ if isIterable(tpe) => Types.Iterable(toType(typeArgs(tpe).head))
    case _ if isFunction(tpe) => Types.Function
    case _ => Types.Other
  }

  protected def typeArgs(tpe: T): List[T]

  protected def isString(tpe: T): Boolean
  protected def isBoolean(tpe: T): Boolean
  protected def isByte(tpe: T): Boolean
  protected def isShort(tpe: T): Boolean
  protected def isInt(tpe: T): Boolean
  protected def isLong(tpe: T): Boolean
  protected def isFloat(tpe: T): Boolean
  protected def isDouble(tpe: T): Boolean
  protected def isAny(tpe: T): Boolean
  protected def isBigDecimal(tpe: T): Boolean
  protected def isJavaBigDecimal(tpe: T): Boolean
  protected def isBigInt(tpe: T): Boolean
  protected def isBigInteger(tpe: T): Boolean
  protected def isTime(tpe: T): Boolean
  protected def isTimestamp(tpe: T): Boolean
  protected def isDate(tpe: T): Boolean
  protected def isUtilDate(tpe: T): Boolean
  protected def isLocalTime(tpe: T): Boolean
  protected def isLocalDateTime(tpe: T): Boolean
  protected def isLocalDate(tpe: T): Boolean
  protected def isBytes(tpe: T): Boolean
  protected def isBlob(tpe: T): Boolean
  protected def isNClob(tpe: T): Boolean
  protected def isClob(tpe: T): Boolean
  protected def isSQLXML(tpe: T): Boolean
  protected def isRuntimeEntity(tpe: T): Boolean
  protected def isGeneratedEntity(tpe: T): Boolean
  protected def isGeneratedHolder(tpe: T): Boolean
  protected def isAnyValHolder(tpe: T): Boolean
  protected def isMap(tpe: T): Boolean
  protected def isOption(tpe: T): Boolean
  protected def isSeq(tpe: T): Boolean
  protected def isIterable(tpe: T): Boolean
  protected def isFunction(tpe: T): Boolean

}
