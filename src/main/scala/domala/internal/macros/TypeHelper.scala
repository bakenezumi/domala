package domala.internal.macros

import scala.meta._

object TypeHelper {

  def convertToDomaType(tpe: Type.Arg): DomaType = {
    tpe match {
      case t"Map[String, $_]" => DomaType.Map
      case t"Map[$_,$_]" => DomaType.UnSupport
      case t"Seq[$elementTpe]" => DomaType.Seq(convertToDomaType(elementTpe), elementTpe)
      case t"Option[$elementTpe]" => DomaType.Option(convertToDomaType(elementTpe), elementTpe)
      case t"Optional[$elementTpe]" => DomaType.Option(convertToDomaType(elementTpe), elementTpe)
      case t"BigDecimal" => DomaType.Basic(
        toType(tpe), toType(tpe),
        q"() => new domala.wrapper.BigDecimalWrapper(): org.seasar.doma.wrapper.Wrapper[BigDecimal]"
      )
      case t"BigInt" => DomaType.Basic(
        toType(tpe), toType(tpe),
        q"() => new domala.wrapper.BigIntWrapper(): org.seasar.doma.wrapper.Wrapper[BigInt]"
      )
      case t"Int" | t"Integer" | t"OptionalInt" => DomaType.Basic(
        toType(tpe), t"Integer",
        q"() => new org.seasar.doma.wrapper.IntegerWrapper(): org.seasar.doma.wrapper.Wrapper[Integer]"
      )
      case t"Any" | t"AnyRef" | t"Object" => DomaType.Basic(
        toType(tpe), t"Object",
        q"() => new org.seasar.doma.wrapper.ObjectWrapper(): org.seasar.doma.wrapper.Wrapper[Object]"
      )
      case t"Array[Byte]" => DomaType.Basic(
        toType(tpe), t"Array[Byte]",
        q"() => (new org.seasar.doma.wrapper.BytesWrapper()):(org.seasar.doma.wrapper.Wrapper[Array[Byte]])"
      )
      case t"Long" | t"OptionalLong" => DomaType.Basic(
        toType(tpe), t"java.lang.Long",
        q"() => new org.seasar.doma.wrapper.LongWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Long]"
      )
      case t"Double" | t"OptionalDouble" => DomaType.Basic(
        toType(tpe), t"java.lang.Double",
        q"() => new org.seasar.doma.wrapper.DoubleWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Double]"
      )
      case t"Boolean" => DomaType.Basic(
        toType(tpe), t"java.lang.Boolean",
        q"() => new org.seasar.doma.wrapper.BooleanWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Boolean]"
      )
      case t"Byte" => DomaType.Basic(
        toType(tpe), t"java.lang.Byte",
        q"() => new org.seasar.doma.wrapper.ByteWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Byte]"
      )
      case t"Short" => DomaType.Basic(
        toType(tpe), t"java.lang.Short",
        q"() => new org.seasar.doma.wrapper.ShortWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Short]"
      )
      case t"Float" => DomaType.Basic(
        toType(tpe), t"java.lang.Float",
        q"() => new org.seasar.doma.wrapper.FloatWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Float]"
      )
      case t"String" => DomaType.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.StringWrapper(): org.seasar.doma.wrapper.Wrapper[String]"
      )
      case t"java.math.BigDecimal" => DomaType.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.BigDecimalWrapper(): org.seasar.doma.wrapper.Wrapper[java.math.BigDecimal]"
      )
      case t"BigInteger" | t"java.math.BigInteger" => DomaType.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.BigIntegerWrapper(): org.seasar.doma.wrapper.Wrapper[java.math.BigInteger]"
      )
      case t"LocalDate" | t"java.time.LocalDate" => DomaType.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.LocalDateWrapper(): org.seasar.doma.wrapper.Wrapper[java.time.LocalDate]"
      )
      case t"LocalTime" | t"java.time.LocalTime" => DomaType.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.LocalTimeWrapper(): org.seasar.doma.wrapper.Wrapper[java.time.LocalTime]"
      )
      case t"LocalDateTime" | t"java.time.LocalDateTime" => DomaType.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.LocalDateTimeWrapper(): org.seasar.doma.wrapper.Wrapper[java.time.LocalDateTime]"
      )
      case t"Date" | t"java.sql.Date" => DomaType.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.DateWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Date]"
      )
      case t"Time" | t"java.sql.Time" => DomaType.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.TimeWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Time]"
      )
      case t"Timestamp" | t"java.sql.Timestamp" => DomaType.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.TimestampWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Timestamp]"
      )
      case t"Blob" | t"java.sql.Blob" => DomaType.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.BlobWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Blob]"
      )
      case t"Clob" | t"java.sql.Clob" => DomaType.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.ClobWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Clob]"
      )
      case t"SQLXML" | t"java.sql.SQLXML" => DomaType.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.SQLXMLWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.SQLXML]"
      )
      case _ => DomaType.EntityOrHolderOrEmbeddable(toType(tpe))
    }
  }

  def convertToEntityDomaType(tpe: Type.Arg): DomaType = {
    convertToDomaType(tpe) match {
      case option: DomaType.Option => option
      case basic: DomaType.Basic => basic
      case other: DomaType.EntityOrHolderOrEmbeddable => other
      case _ => DomaType.UnSupport
    }
  }

  def toType(arg: Type.Arg): Type = arg match {
    case Type.Arg.Repeated(tpe) => tpe
    case Type.Arg.ByName(tpe) => tpe
    case tpe: Type => tpe
  }
}
