package domala.internal.macros.reflect

import java.util.function.Supplier

import domala.internal.macros.reflect.util.ReflectionUtil.extractionClassString
import domala.internal.macros.reflect.util.TypeUtil
import domala.message.Message
import org.seasar.doma.jdbc.entity.NamingType
import org.seasar.doma.wrapper.Wrapper

import scala.language.experimental.macros
import scala.reflect.ClassTag
import scala.reflect.macros.blackbox
object EmbeddableReflectionMacros {

  def generatePropertyTypeImpl[
    T: c.WeakTypeTag,
    E: c.WeakTypeTag,
    N: c.WeakTypeTag](c: blackbox.Context)(
    embeddableClass: c.Expr[Class[E]],
    paramName: c.Expr[String],
    namingType: c.Expr[NamingType],
    isBasic: c.Expr[Boolean],
    wrapperSupplier: c.Expr[Supplier[Wrapper[N]]],
    columnName: c.Expr[String],
    columnInsertable: c.Expr[Boolean],
    columnUpdatable: c.Expr[Boolean],
    columnQuote: c.Expr[Boolean],
    collections: c.Expr[EntityCollections[E]]
  )(
    propertyClassTag: c.Expr[ClassTag[T]],
    nakedClassTag: c.Expr[ClassTag[N]]
  ): c.Expr[Object] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if(TypeUtil.isEmbeddable(c)(tpe)) {
      c.abort(
        c.enclosingPosition,
        Message.DOMALA4297.getMessage(
          extractionClassString(tpe.toString)))
    }
    EntityReflectionMacros.generatePropertyTypeImpl[T, E, N](c)(
      embeddableClass,
      paramName,
      namingType,
      c.Expr(Literal(Constant(false))),
      c.Expr(Literal(Constant(false))),
      c.Expr(Literal(Constant(null))),
      c.Expr(Literal(Constant(false))),
      c.Expr(Literal(Constant(false))),
      isBasic,
      wrapperSupplier,
      columnName,
      columnInsertable,
      columnUpdatable,
      columnQuote,
      collections
    )(propertyClassTag, nakedClassTag)

  }

  def generatePropertyType[T, E, N](
    embeddableClass: Class[E],
    paramName: String,
    namingType: NamingType,
    isBasic: Boolean,
    wrapperSupplier: Supplier[Wrapper[N]],
    columnName: String,
    columnInsertable: Boolean,
    columnUpdatable: Boolean,
    columnQuote: Boolean,
    collections: EntityCollections[E]
  )(
    implicit propertyClassTag: ClassTag[T],
    nakedClassTag: ClassTag[N]
  ): Object =  macro generatePropertyTypeImpl[T, E, N]

}
