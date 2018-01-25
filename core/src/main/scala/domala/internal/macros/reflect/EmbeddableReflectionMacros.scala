package domala.internal.macros.reflect

import java.util.function.Supplier

import domala.Column
import domala.internal.macros.reflect.util.{MacroTypeConverter, PropertyDescUtil}
import domala.internal.reflect.util.ReflectionUtil
import domala.jdbc.entity.{EntityPropertyDesc, NamingType}
import domala.message.Message
import org.seasar.doma.wrapper.Wrapper

import scala.language.experimental.macros
import scala.reflect.ClassTag
import scala.reflect.macros.blackbox
object EmbeddableReflectionMacros {

  private def handle[E: c.WeakTypeTag, R](c: blackbox.Context)(embeddableClass: c.Expr[Class[E]])(block: => R): R = try {
    block
  } catch {
    case e: ReflectAbortException =>
      import c.universe._
      c.abort(weakTypeOf[E].typeSymbol.pos, e.getLocalizedMessage.replace("Nothing", weakTypeOf[E].toString))
  }

  def generatePropertyDescImpl[
    EM: c.WeakTypeTag,
    T: c.WeakTypeTag,
    E: c.WeakTypeTag,
    N: c.WeakTypeTag](c: blackbox.Context)(
    embeddableClass: c.Expr[Class[EM]],
    propertyName: c.Expr[String],
    entityClass: c.Expr[Class[E]],
    paramName: c.Expr[String],
    namingType: c.Expr[NamingType],
    isBasic: c.Expr[Boolean],
    wrapperSupplier: c.Expr[Supplier[Wrapper[N]]],
    column: c.Expr[Column]
  )(
    propertyClassTag: c.Expr[ClassTag[T]],
    nakedClassTag: c.Expr[ClassTag[N]]
  ): c.Expr[Map[String, EntityPropertyDesc[E, _]]] = handle(c)(embeddableClass) {
    import c.universe._
    val tpe = weakTypeOf[T]
    if(MacroTypeConverter.of(c).toType(tpe).isEmbeddable) {
      val Literal(Constant(propertyNameLiteral: String)) = propertyName.tree
      ReflectionUtil.abort(
        Message.DOMALA4297,
        tpe, weakTypeOf[EM], propertyNameLiteral)
    }
    PropertyDescUtil.generatePropertyDescImpl[T, E, N](c)(
      entityClass,
      paramName,
      namingType,
      c.Expr(Literal(Constant(false))),
      c.Expr(Literal(Constant(false))),
      c.Expr(Literal(Constant(null))),
      c.Expr(Literal(Constant(false))),
      c.Expr(Literal(Constant(false))),
      isBasic,
      wrapperSupplier,
      column
    )(propertyClassTag, nakedClassTag)
  }

  def generatePropertyDesc[EM, T, E, N](
    embeddableClass: Class[EM],
    propertyName: String,
    entityClass: Class[E],
    paramName: String,
    namingType: NamingType,
    isBasic: Boolean,
    wrapperSupplier: Supplier[Wrapper[N]],
    column: Column,
  )(
    implicit propertyClassTag: ClassTag[T],
    nakedClassTag: ClassTag[N]
  ): Map[String, EntityPropertyDesc[E, _]] =  macro generatePropertyDescImpl[EM, T, E, N]

}
