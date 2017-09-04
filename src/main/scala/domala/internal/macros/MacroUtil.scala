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
    "String", "Option[String]", "Optional[String]"
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
      }
    }
  }
}
