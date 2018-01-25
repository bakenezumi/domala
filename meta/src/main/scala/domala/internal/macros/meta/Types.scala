package domala.internal.macros.meta

import domala.internal.macros.meta.util.TypeUtil.toType

import scala.meta._

sealed trait Types

object Types {
  case object Map extends Types
  case class Seq(elementDomaType: Types, elementType: Type) extends Types
  case class Option(elementDomaType: Types, elementType: Type) extends Types
  case class Basic(originalType: Type, convertedType: Type, wrapperSupplier: Term.Function, isNumeric: Boolean = false) extends Types
  case class EntityOrHolderOrEmbeddable(tpe: Type) extends Types
  case object UnSupport extends Types

  def of(tpe: Type.Arg): Types = {
    //noinspection ScalaUnusedSymbol
    tpe match {
      case t"Map[String, Object]" => Types.Map
      case t"Map[String, Any]" => Types.Map
      case t"Map[String, AnyRef]" => Types.Map
      case t"Map[$_,$_]" => Types.UnSupport
      case t"Seq[$elementTpe]" => Types.Seq(of(elementTpe), elementTpe)
      case t"Option[$elementTpe]" => Types.Option(of(elementTpe), elementTpe)
      case t"Optional[$elementTpe]" => Types.Option(of(elementTpe), elementTpe)
      case t"BigDecimal" => Types.Basic(
        toType(tpe), toType(tpe),
        q"() => new domala.wrapper.BigDecimalWrapper(): org.seasar.doma.wrapper.Wrapper[BigDecimal]",
        isNumeric = true
      )
      case t"BigInt" => Types.Basic(
        toType(tpe), toType(tpe),
        q"() => new domala.wrapper.BigIntWrapper(): org.seasar.doma.wrapper.Wrapper[BigInt]",
        isNumeric = true
      )
      case t"Int" | t"Integer" | t"OptionalInt" => Types.Basic(
        toType(tpe), t"Integer",
        q"() => new org.seasar.doma.wrapper.IntegerWrapper(): org.seasar.doma.wrapper.Wrapper[Integer]",
        isNumeric = true
      )
      case t"Any" | t"AnyRef" | t"Object" => Types.Basic(
        toType(tpe), t"Object",
        q"() => new org.seasar.doma.wrapper.ObjectWrapper(): org.seasar.doma.wrapper.Wrapper[Object]",
        isNumeric = true
      )
      case t"Array[Byte]" => Types.Basic(
        toType(tpe), t"Array[Byte]",
        q"() => (new org.seasar.doma.wrapper.BytesWrapper()):(org.seasar.doma.wrapper.Wrapper[Array[Byte]])"
      )
      case t"Long" | t"OptionalLong" => Types.Basic(
        toType(tpe), t"java.lang.Long",
        q"() => new org.seasar.doma.wrapper.LongWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Long]",
        isNumeric = true
      )
      case t"Double" | t"OptionalDouble" => Types.Basic(
        toType(tpe), t"java.lang.Double",
        q"() => new org.seasar.doma.wrapper.DoubleWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Double]",
        isNumeric = true
      )
      case t"Boolean" => Types.Basic(
        toType(tpe), t"java.lang.Boolean",
        q"() => new org.seasar.doma.wrapper.BooleanWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Boolean]"
      )
      case t"Byte" => Types.Basic(
        toType(tpe), t"java.lang.Byte",
        q"() => new org.seasar.doma.wrapper.ByteWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Byte]"
      )
      case t"Short" => Types.Basic(
        toType(tpe), t"java.lang.Short",
        q"() => new org.seasar.doma.wrapper.ShortWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Short]"
      )
      case t"Float" => Types.Basic(
        toType(tpe), t"java.lang.Float",
        q"() => new org.seasar.doma.wrapper.FloatWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Float]",
        isNumeric = true
      )
      case t"String" => Types.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.StringWrapper(): org.seasar.doma.wrapper.Wrapper[String]"
      )
      case t"java.math.BigDecimal" => Types.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.BigDecimalWrapper(): org.seasar.doma.wrapper.Wrapper[java.math.BigDecimal]",
        isNumeric = true
      )
      case t"BigInteger" | t"java.math.BigInteger" => Types.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.BigIntegerWrapper(): org.seasar.doma.wrapper.Wrapper[java.math.BigInteger]",
        isNumeric = true
      )
      case t"LocalDate" | t"java.time.LocalDate" => Types.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.LocalDateWrapper(): org.seasar.doma.wrapper.Wrapper[java.time.LocalDate]"
      )
      case t"LocalTime" | t"java.time.LocalTime" => Types.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.LocalTimeWrapper(): org.seasar.doma.wrapper.Wrapper[java.time.LocalTime]"
      )
      case t"LocalDateTime" | t"java.time.LocalDateTime" => Types.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.LocalDateTimeWrapper(): org.seasar.doma.wrapper.Wrapper[java.time.LocalDateTime]"
      )
      case t"Date" | t"java.sql.Date" => Types.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.DateWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Date]"
      )
      case t"Time" | t"java.sql.Time" => Types.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.TimeWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Time]"
      )
      case t"Timestamp" | t"java.sql.Timestamp" => Types.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.TimestampWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Timestamp]"
      )
      case t"Blob" | t"java.sql.Blob" => Types.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.BlobWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Blob]"
      )
      case t"Clob" | t"java.sql.Clob" => Types.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.ClobWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Clob]"
      )
      case t"SQLXML" | t"java.sql.SQLXML" => Types.Basic(
        toType(tpe), toType(tpe),
        q"() => new org.seasar.doma.wrapper.SQLXMLWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.SQLXML]"
      )
      case _ => Types.EntityOrHolderOrEmbeddable(toType(tpe))
    }
  }

  def ofEntityProperty(tpe: Type.Arg): Types = Types.of(tpe) match {
    case option: Types.Option => option
    case basic: Types.Basic => basic
    case other: Types.EntityOrHolderOrEmbeddable => other
    case _ => Types.UnSupport
  }

}
