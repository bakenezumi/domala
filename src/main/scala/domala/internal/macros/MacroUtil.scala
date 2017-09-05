package domala.internal.macros

import scala.meta._

object MacroUtil {
  // TODO: 他の型対応
  private val basicTypes = Set[String](
    "Boolean", "Option[Boolean]", "Optional[Boolean]",
    "Byte", "Option[Byte]", "Optional[Byte]",
    "Short", "Option[Short]", "Optional[Short]",
    "Int", "Integer", "Option[Int]", "Optional[Integer]", "OptionalInt",
    "Long", "Option[Long]", "Optional[Long]", "OptionalLong",
    "Float", "Option[Float]", "Optional[Float]",
    "Double", "Option[Double]", "Optional[Double]", "OptionalDouble",
    "Array[Byte]", "Option[Array[Byte]]", "Optional[Array[Byte]]",
    "String", "Option[String]", "Optional[String]",
    "Object", "AnyRef", "Option[Object]", "Option[AnyRef]", "Optional[Object]", "Optional[AnyRef]",
    "BigDecimal", "Option[BigDecimal]", "Optional[BigDecimal]",
    "java.math.BigDecimal", "Option[java.math.BigDecimal]", "Optional[java.math.BigDecimal]",
    "BigInt", "Option[BigInt]", "Optional[BigInt]",
    "java.math.BigInteger", "Option[java.math.BigInteger]", "Optional[java.math.BigInteger]", "BigInteger", "Option[BigInteger]", "Optional[[BigInteger]",
    "LocalDate", "java.time.LocalDate",  "Option[LocalDate]",  "Option[java.time.LocalDate]",  "Optional[java.time.LocalDate]",
    "LocalTime", "java.time.LocalTime",  "Option[LocalTime]",  "Option[java.time.LocalTime]",  "Optional[java.time.LocalTime]",
    "LocalDateTime", "java.time.LocalDateTime",  "Option[LocalDateTime]",  "Option[java.time.LocalDateTime]",  "Optional[java.time.LocalDateTime]",
    "Date", "java.sql.Date",  "Option[Date]",  "Option[java.sql.Date]",  "Optional[java.sql.Date]",
    "Time", "java.sql.Time",  "Option[Time]",  "Option[java.sql.Time]",  "Optional[java.sql.Time]",
    "Timestamp", "java.sql.Timestamp",  "Option[Timestamp]",  "Option[java.sql.Timestamp]",  "Optional[java.sql.Timestamp]",
    "Blob", "java.sql.Blob",  "Option[Blob]",  "Option[java.sql.Blob]",  "Optional[java.sql.Blob]",
    "Clob", "java.sql.Clob",  "Option[Clob]",  "Option[java.sql.Clob]",  "Optional[java.sql.Clob]"
  )

  def isDomain(tpe: Type.Arg) = {
    !basicTypes.contains(tpe.toString)
  }

  // TODO: 他の型対応
  def convertType(tpe: Type.Arg): (Term, Term, Term) = {
    if (isDomain(tpe)) {
      val domainTpe = tpe match {
        case t"$containerTpe[$internalTpe]" => Term.Name(internalTpe.toString)
        case _  => Term.Name(tpe.toString)
      }
      (
        q"$domainTpe.getSingletonInternal.getBasicClass()",
        q"$domainTpe.wrapperSupplier",
        q"$domainTpe.getSingletonInternal"
      )
    } else {
      tpe match {
        case t"Boolean" | t"Option[Boolean]" | t"Optional[Boolean]" => (
          q"classOf[java.lang.Boolean]",
          q"() => new org.seasar.doma.wrapper.BooleanWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Boolean]",
          q"null"
        )
        case t"Byte" | t"Option[Byte]" | t"Optional[Byte]" => (
          q"classOf[java.lang.Byte]",
          q"() => new org.seasar.doma.wrapper.ByteWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Byte]",
          q"null"
        )
        case t"Short" | t"Option[Short]" | t"Optional[Short]" => (
          q"classOf[java.lang.Short]",
          q"() => new org.seasar.doma.wrapper.ShortWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Short]",
          q"null"
        )
        case t"Int" | t"Integer"  | t"Option[Int]" | t"Optional[Integer]" | t"OptionalInt" => (
          q"classOf[Integer]",
          q"() => new org.seasar.doma.wrapper.IntegerWrapper(): org.seasar.doma.wrapper.Wrapper[Integer]",
          q"null"
        )
        case t"Long"  | t"Option[Long]" | t"Optional[Long]" | t"OptionalLong" => (
          q"classOf[java.lang.Long]",
          q"() => new org.seasar.doma.wrapper.LongWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Long]",
          q"null"
        )
        case t"Float"  | t"Option[Float]" | t"Optional[Float]" => (
          q"classOf[java.lang.Float]",
          q"() => new org.seasar.doma.wrapper.FloatWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Float]",
          q"null"
        )
        case t"Double"  | t"Option[Double]" | t"Optional[Double]" | t"OptionalDouble" => (
          q"classOf[java.lang.Double]",
          q"() => new org.seasar.doma.wrapper.DoubleWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Double]",
          q"null"
        )
        case t"Array[Byte]" | t"Option[Array[Byte]]" | t"Optional[Array[Byte]]" => (
          q"classOf[Array[Byte]]",
          q"() => (new org.seasar.doma.wrapper.BytesWrapper()):(org.seasar.doma.wrapper.Wrapper[Array[Byte]])",
          q"null"
        )
        case t"String" | t"Option[String]" | t"Optional[String]" => (
          q"classOf[String]",
          q"() => new org.seasar.doma.wrapper.StringWrapper(): org.seasar.doma.wrapper.Wrapper[String]",
          q"null"
        )
        case t"Object" | t"Option[Object]" | t"Optional[Object]" | t"AnyRef" | t"Option[AnyRef]" | t"Optional[AnyRef]" => (
          q"classOf[Object]",
          q"() => new org.seasar.doma.wrapper.ObjectWrapper(): org.seasar.doma.wrapper.Wrapper[Object]",
          q"null"
        )
        case t"BigDecimal" | t"Option[BigDecimal]" | t"Optional[BigDecimal]" => (
          q"classOf[BigDecimal]",
          q"() => new domala.wrapper.BigDecimalWrapper(): org.seasar.doma.wrapper.Wrapper[BigDecimal]",
          q"null"
        )
        case t"java.math.BigDecimal"  | t"Option[java.math.BigDecimal]" | t"Optional[java.math.BigDecimal]" => (
          q"classOf[java.math.BigDecimal]",
          q"() => new org.seasar.doma.wrapper.BigDecimalWrapper(): org.seasar.doma.wrapper.Wrapper[java.math.BigDecimal]",
          q"null"
        )
        case t"BigInt" | t"Option[BigInt]" | t"Optional[BigInt]" => (
          q"classOf[BigInt]",
          q"() => new domala.wrapper.BigIntWrapper(): org.seasar.doma.wrapper.Wrapper[BigInt]",
          q"null"
        )
        case t"BigInteger"  | t"Option[BigInteger]" | t"Optional[BigInteger]" | t"java.math.BigInteger"  | t"Option[java.math.BigInteger]" | t"Optional[java.math.BigInteger]" => (
          q"classOf[java.math.BigInteger]",
          q"() => new org.seasar.doma.wrapper.BigIntegerWrapper(): org.seasar.doma.wrapper.Wrapper[java.math.BigInteger]",
          q"null"
        )
        case t"LocalDate" | t"java.time.LocalDate" | t"Option[LocalDate]" | t"Option[java.time.LocalDate]" | t"Optional[java.time.LocalDate]" => (
          q"classOf[java.time.LocalDate]",
          q"() => new org.seasar.doma.wrapper.LocalDateWrapper(): org.seasar.doma.wrapper.Wrapper[java.time.LocalDate]",
          q"null"
        )
        case t"LocalTime" | t"java.time.LocalTime" | t"Option[LocalTime]" | t"Option[java.time.LocalTime]" | t"Optional[java.time.LocalTime]" => (
          q"classOf[java.time.LocalTime]",
          q"() => new org.seasar.doma.wrapper.LocalTimeWrapper(): org.seasar.doma.wrapper.Wrapper[java.time.LocalTime]",
          q"null"
        )
        case t"LocalDateTime" | t"java.time.LocalDateTime" | t"Option[LocalDateTime]" | t"Option[java.time.LocalDateTime]" | t"Optional[java.time.LocalDateTime]" => (
          q"classOf[java.time.LocalDateTime]",
          q"() => new org.seasar.doma.wrapper.LocalDateTimeWrapper(): org.seasar.doma.wrapper.Wrapper[java.time.LocalDateTime]",
          q"null"
        )
        case t"Date" | t"java.sql.Date" | t"Option[Date]" | t"Option[java.sql.Date]" | t"Optional[java.sql.Date]" => (
          q"classOf[java.sql.Date]",
          q"() => new org.seasar.doma.wrapper.DateWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Date]",
          q"null"
        )
        case t"Time" | t"java.sql.Time" | t"Option[Time]" | t"Option[java.sql.Time]" | t"Optional[java.sql.Time]" => (
          q"classOf[java.sql.Time]",
          q"() => new org.seasar.doma.wrapper.TimeWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Time]",
          q"null"
        )
        case t"Timestamp" | t"java.sql.Timestamp" | t"Option[Timestamp]" | t"Option[java.sql.Timestamp]" | t"Optional[java.sql.Timestamp]" => (
          q"classOf[java.sql.Timestamp]",
          q"() => new org.seasar.doma.wrapper.TimestampWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Timestamp]",
          q"null"
        )
        case t"Blob" | t"java.sql.Blob" | t"Option[Blob]" | t"Option[java.sql.Blob]" | t"Optional[java.sql.Blob]" => (
          q"classOf[java.sql.Blob]",
          q"() => new org.seasar.doma.wrapper.BlobWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Blob]",
          q"null"
        )
        case t"Clob" | t"java.sql.Clob" | t"Option[Clob]" | t"Option[java.sql.Clob]" | t"Optional[java.sql.Clob]" => (
          q"classOf[java.sql.Clob]",
          q"() => new org.seasar.doma.wrapper.ClobWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Clob]",
          q"null"
        )
      }
    }
  }
}
