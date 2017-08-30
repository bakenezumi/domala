package domala.internal.macros

import scala.meta._

object MacroUtil {
  // TODO: 他の型対応
  private val basicTypes = Set(
    "Int",
    "Integer",
    "OptionalInt",
    "Optional[Integer]",
    "Option[Int]",
    "String"
  )

  def isDomain(tpe: Type.Name) = {
    !basicTypes.contains(tpe.value)
  }

  // TODO: 他の型対応
  def convertType(tpe: Type.Name) = {
    if (isDomain(tpe)) {
      (
        q"${Term.Name(tpe.value)}.getSingletonInternal.getBasicClass()",
        q"${Term.Name(tpe.value)}.wrapperSupplier",
        q"${Term.Name(tpe.value)}.getSingletonInternal"
      )
    } else {
      tpe.value match {
        case "Int" | "Integer" | "OptionalInt" | "Optional[Integer]" | "Option[Int]" => (
          q"classOf[Integer]",
          q"""
          new java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[Integer]]() {
            def get = new org.seasar.doma.wrapper.IntegerWrapper()
          }
          """,
          q"null"
        )
        case "String" => (
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
