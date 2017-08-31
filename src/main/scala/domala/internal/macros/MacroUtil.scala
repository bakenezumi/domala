package domala.internal.macros

import scala.meta._

object MacroUtil {
  // TODO: 他の型対応
  private val basicTypes = Set(
    "Int",
    "Integer",
    "Option[Int]",
    "Optional[Integer]",
    "OptionalInt",
    "String",
    "Option[String]",
    "Optional[String]"
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
        case "Int" | "Integer" | "OptionalInt" | "Optional[Integer]" | "Option[Int]" => (
          q"classOf[Integer]",
          q"""
          new java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[Integer]]() {
            def get = new org.seasar.doma.wrapper.IntegerWrapper()
          }
          """,
          q"null"
        )
        case "String" | "Optional[String]"  | "Option[String]"=> (
          q"classOf[String]",
          q"""
          new java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[String]]() {
            def get = new org.seasar.doma.wrapper.StringWrapper()
          }
          """,
          q"null"
        )
      }
    }
  }
}
