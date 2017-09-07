package domala.internal.macros

import scala.meta._

object MacroUtil {

  private val basicTypes = Set[Type](
    t"Boolean",
    t"Byte",
    t"Short",
    t"Int", t"Integer",
    t"Long",
    t"Float",
    t"Double",
    t"Array[Byte]",
    t"String",
    t"AnyRef", t"Object",
    t"BigDecimal",
    t"java.math.BigDecimal",
    t"BigInt",
    t"java.math.BigInteger", t"BigInteger",
    t"LocalDate", t"java.time.LocalDate",
    t"LocalTime", t"java.time.LocalTime",
    t"LocalDateTime", t"java.time.LocalDateTime",
    t"Date", t"java.sql.Date",
    t"Time", t"java.sql.Time",
    t"Timestamp", t"java.sql.Timestamp",
    t"Blob", t"java.sql.Blob",
    t"Clob", t"java.sql.Clob",
    t"SQLXML", t"java.sql.SQLXML"
  )

  private val basicTypesAndOptions = Set[Option[Type]](
    None,
    Some(t"Option"),
    Some(t"Optional")).flatMap(container =>
    basicTypes.map(tp =>
      container match {
        case None => tp
        case Some(ctp) => t"$ctp[$tp]"
      }
    )) ++ Set(t"OptionalInt", t"OptionalLong", t"OptionalLong")

  private val basicTypeStrings: Set[String] = basicTypesAndOptions.map(_.toString)

  private def toType(arg: Type.Arg): Type = arg match {
    case Type.Arg.Repeated(tpe) => tpe
    case Type.Arg.ByName(tpe) => tpe
    case tpe: Type => tpe
  }

  def isBasic(tpe: Type.Arg): Boolean = {
    basicTypeStrings.contains(tpe.toString())
  }

  def convertType(tpe: Type.Arg): Option[(Type, Term.Function)] = {
    tpe match {
      case t"BigDecimal" => Some(
        toType(tpe),
        q"() => new domala.wrapper.BigDecimalWrapper(): org.seasar.doma.wrapper.Wrapper[BigDecimal]"
      )
      case t"BigInt" => Some(
        toType(tpe),
        q"() => new domala.wrapper.BigIntWrapper(): org.seasar.doma.wrapper.Wrapper[BigInt]"
      )
      case t"Int" | t"Integer" | t"OptionalInt" => Some(
        t"Integer",
        q"() => new org.seasar.doma.wrapper.IntegerWrapper(): org.seasar.doma.wrapper.Wrapper[Integer]"
      )
      case t"Any" | t"AnyRef" | t"Object" => Some(
        t"Object",
        q"() => new org.seasar.doma.wrapper.ObjectWrapper(): org.seasar.doma.wrapper.Wrapper[Object]"
      )
      case t"Array[Byte]" => Some(
        t"Array[Byte]",
        q"() => (new org.seasar.doma.wrapper.BytesWrapper()):(org.seasar.doma.wrapper.Wrapper[Array[Byte]])"
      )
      case t"Long" | t"OptionalLong" => Some(
        t"java.lang.Long",
        q"() => new org.seasar.doma.wrapper.LongWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Long]"
      )
      case t"Double" | t"OptionalDouble" => Some(
        t"java.lang.Double",
        q"() => new org.seasar.doma.wrapper.DoubleWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Double]"
      )
      case t"Boolean" => Some(
        t"java.lang.Boolean",
        q"() => new org.seasar.doma.wrapper.BooleanWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Boolean]"
      )
      case t"Byte" => Some(
        t"java.lang.Byte",
        q"() => new org.seasar.doma.wrapper.ByteWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Byte]"
      )
      case t"Short" => Some(
        t"java.lang.Short",
        q"() => new org.seasar.doma.wrapper.ShortWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Short]"
      )
      case t"Float" => Some(
        t"java.lang.Float",
        q"() => new org.seasar.doma.wrapper.FloatWrapper(): org.seasar.doma.wrapper.Wrapper[java.lang.Float]"
      )
      case t"String" => Some(
        toType(tpe),
        q"() => new org.seasar.doma.wrapper.StringWrapper(): org.seasar.doma.wrapper.Wrapper[String]"
      )
      case t"java.math.BigDecimal" => Some(
        toType(tpe),
        q"() => new org.seasar.doma.wrapper.BigDecimalWrapper(): org.seasar.doma.wrapper.Wrapper[java.math.BigDecimal]"
      )
      case t"BigInteger" | t"java.math.BigInteger" => Some(
        toType(tpe),
        q"() => new org.seasar.doma.wrapper.BigIntegerWrapper(): org.seasar.doma.wrapper.Wrapper[java.math.BigInteger]"
      )
      case t"LocalDate" | t"java.time.LocalDate" => Some(
        toType(tpe),
        q"() => new org.seasar.doma.wrapper.LocalDateWrapper(): org.seasar.doma.wrapper.Wrapper[java.time.LocalDate]"
      )
      case t"LocalTime" | t"java.time.LocalTime" => Some(
        toType(tpe),
        q"() => new org.seasar.doma.wrapper.LocalTimeWrapper(): org.seasar.doma.wrapper.Wrapper[java.time.LocalTime]"
      )
      case t"LocalDateTime" | t"java.time.LocalDateTime" => Some(
        toType(tpe),
        q"() => new org.seasar.doma.wrapper.LocalDateTimeWrapper(): org.seasar.doma.wrapper.Wrapper[java.time.LocalDateTime]"
      )
      case t"Date" | t"java.sql.Date" => Some(
        toType(tpe),
        q"() => new org.seasar.doma.wrapper.DateWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Date]"
      )
      case t"Time" | t"java.sql.Time" => Some(
        toType(tpe),
        q"() => new org.seasar.doma.wrapper.TimeWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Time]"
      )
      case t"Timestamp" | t"java.sql.Timestamp" => Some(
        toType(tpe),
        q"() => new org.seasar.doma.wrapper.TimestampWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Timestamp]"
      )
      case t"Blob" | t"java.sql.Blob" => Some(
        toType(tpe),
        q"() => new org.seasar.doma.wrapper.BlobWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Blob]"
      )
      case t"Clob" | t"java.sql.Clob" => Some(
        toType(tpe),
        q"() => new org.seasar.doma.wrapper.ClobWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.Clob]"
      )
      case t"SQLXML" | t"java.sql.SQLXML" => Some(
        toType(tpe),
        q"() => new org.seasar.doma.wrapper.SQLXMLWrapper(): org.seasar.doma.wrapper.Wrapper[java.sql.SQLXML]"
      )
      case _ => None
    }
  }

  def convertPropertyType(tpe: Type.Arg): (Term, Term, Term) = {
    if (!isBasic(tpe)) {
      val domainTpe = tpe match {
        case t"$_[$internalTpe]" => Term.Name(internalTpe.toString)
        case _  => Term.Name(tpe.toString)
      }
      (
        q"$domainTpe.getSingletonInternal.getBasicClass()",
        q"$domainTpe.wrapperSupplier",
        q"$domainTpe.getSingletonInternal"
      )
    } else {
      val targetTpe = tpe match {
        case t"Option[$inner]" => inner
        case t"Optional[$inner]" => inner
        case _ => tpe
      }
      val Some((javaTpe, wrapper)) = convertType(targetTpe)
      (q"classOf[$javaTpe]", wrapper, q"null")
    }
  }
}
