package domala.internal.macros

import scala.meta._

object MacroUtil {
  // TODO: 他の型対応
  private val basicTypes = Set(
    "Int", "Integer", "Option[Int]", "Optional[Integer]", "OptionalInt",
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
      tpe.toString match {
        case "Int" | "Integer"  | "Option[Int]" | "Optional[Integer]" | "OptionalInt" => (
          q"classOf[Integer]",
          q"() => new org.seasar.doma.wrapper.IntegerWrapper(): org.seasar.doma.wrapper.Wrapper[Integer]",
          q"null"
        )
        case "String" | "Option[String]" | "Optional[String]" => (
          q"classOf[String]",
          q"() => new org.seasar.doma.wrapper.StringWrapper(): org.seasar.doma.wrapper.Wrapper[String]",
          q"null"
        )
      }
    }
  }
}
