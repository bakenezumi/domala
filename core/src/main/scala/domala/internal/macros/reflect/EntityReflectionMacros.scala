package domala.internal.macros.reflect

import domala.Column
import domala.internal.macros.reflect.util.{MacroTypeConverter, PropertyDescUtil}
import domala.internal.reflect.util.ReflectionUtil
import domala.internal.reflect.util.ReflectionUtil.extractionClassString
import domala.jdbc.`type`.Types
import domala.jdbc.entity.EntityPropertyDesc
import domala.message.Message
import org.seasar.doma.jdbc.entity._
import org.seasar.doma.jdbc.id.{IdGenerator, SequenceIdGenerator, TableIdGenerator}

import scala.language.experimental.macros
import scala.reflect.ClassTag
import scala.reflect.macros.blackbox

object EntityReflectionMacros {

  private def handle[E: c.WeakTypeTag, R](c: blackbox.Context)(entityClass: c.Expr[Class[E]])(block: => R): R = try {
    block
  } catch {
    case e: ReflectAbortException =>
      import c.universe._
      c.abort(weakTypeOf[E].typeSymbol.pos, e.getLocalizedMessage)
  }

  def generatePropertyDescImpl[
    T: c.WeakTypeTag,
    E: c.WeakTypeTag,
    N: c.WeakTypeTag](c: blackbox.Context)(
    entityClass: c.Expr[Class[E]],
    paramName: c.Expr[String],
    namingType: c.Expr[NamingType],
    isId: c.Expr[Boolean],
    isIdGenerate: c.Expr[Boolean],
    idGenerator: c.Expr[IdGenerator],
    isVersion: c.Expr[Boolean],
    isTenantId: c.Expr[Boolean],
    column: c.Expr[Column]
  )(
    propertyClassTag: c.Expr[ClassTag[T]],
    nakedClassTag: c.Expr[ClassTag[N]]
  ): c.Expr[Map[String, EntityPropertyDesc[E, _]]] = handle(c)(entityClass) {
    PropertyDescUtil.generatePropertyDescImpl[T, E, N](c)(
      entityClass,
      paramName,
      namingType,
      isId,
      isIdGenerate,
      idGenerator,
      isVersion,
      isTenantId,
      column
    )(propertyClassTag, nakedClassTag)
  }
  def generatePropertyDesc[T, E, N](
      entityClass: Class[E],
      paramName: String,
      namingType: NamingType,
      isId: Boolean,
      isIdGenerate: Boolean,
      idGenerator: IdGenerator,
      isVersion: Boolean,
      isTenantId: Boolean,
      column: Column
  )(
      implicit propertyClassTag: ClassTag[T],
      nakedClassTag: ClassTag[N]
  ): Map[String, EntityPropertyDesc[E, _]] =  macro generatePropertyDescImpl[T, E, N]

  def readPropertyImpl[T: c.WeakTypeTag, E: c.WeakTypeTag](c: blackbox.Context)(
      entityClass: c.Expr[Class[E]],
      args: c.Expr[java.util.Map[String, Property[E, _]]],
      propertyName: c.Expr[String])(
      propertyClassTag: c.Expr[ClassTag[T]]): c.Expr[T] = handle(c)(entityClass) {
    import c.universe._
    val wtt = weakTypeOf[T]
    MacroTypeConverter.of(c).toType(wtt) match {
      case Types.RuntimeEntityType =>
        c.Expr[T] {
          q"""
            import scala.collection.JavaConverters._
            domala.internal.macros.reflect.EmbeddableReflectionMacros.generateEmbeddableDesc[$wtt](classOf[$wtt]).newEmbeddable($propertyName, $args.asScala.toMap, $entityClass)
          """
        }
      case _ =>
        reify {
          (if (args.splice.get(propertyName.splice) != null)
            args.splice.get(propertyName.splice).get
          else null).asInstanceOf[T]
        }
    }
  }

  def readProperty[T, E](
      entityClass: Class[E],
      args: java.util.Map[String, Property[E, _]],
      propertyName: String)(implicit propertyClassTag: ClassTag[T]): T = macro readPropertyImpl[T, E]


  def validateListenerImpl[E: c.WeakTypeTag, T: c.WeakTypeTag](c: blackbox.Context)(
    entityClass: c.Expr[Class[E]],
    listenerClass: c.Expr[Class[T]]): c.Expr[Unit] = handle(c)(entityClass) {
    import c.universe._
    val tpe = weakTypeOf[T]
    if(tpe.typeSymbol.isAbstract)
      ReflectionUtil.abort(
        Message.DOMALA4166,
        extractionClassString(tpe.toString))
    val ctor = tpe.typeSymbol.asClass.primaryConstructor
    if(!ctor.isPublic || ctor.asMethod.paramLists.flatten.nonEmpty)
      ReflectionUtil.abort(
        Message.DOMALA4167,
        extractionClassString(tpe.toString))
    val entityType =  tpe.baseType(typeOf[EntityListener[_]].typeSymbol.asClass).typeArgs.head
    // TODO: 互換性がある場合は通す
    if(!(weakTypeOf[E] =:= entityType))
      ReflectionUtil.abort(
        Message.DOMALA4229,
        typeOf[EntityListener[_]].typeSymbol.typeSignature.typeParams.head.name,
        entityType.toString,
        weakTypeOf[E].toString
      )
    reify(())
  }
  def validateListener[E, T <: EntityListener[_]](entityClass: Class[E], listenerClass: Class[T]): Unit = macro validateListenerImpl[E, T]

  def validateTableIdGeneratorImpl[E: c.WeakTypeTag, G: c.WeakTypeTag](c: blackbox.Context)(
    entityClass: c.Expr[Class[E]],
    generatorClass: c.Expr[Class[G]]): c.Expr[Unit] = handle(c)(entityClass) {
    import c.universe._
    val tpe = weakTypeOf[G]
    if(tpe.typeSymbol.isAbstract)
      ReflectionUtil.abort(
        Message.DOMALA4168,
        extractionClassString(tpe.toString))
    val ctor = tpe.typeSymbol.asClass.primaryConstructor
    if(!ctor.isPublic || ctor.asMethod.paramLists.flatten.nonEmpty)
      ReflectionUtil.abort(
        Message.DOMALA4169,
        extractionClassString(tpe.toString))
    reify(())
  }
  def validateTableIdGenerator[E, G <: TableIdGenerator](entityClass: Class[E], generatorClass: Class[G]): Unit = macro validateTableIdGeneratorImpl[E, G]

  def validateSequenceIdGeneratorImpl[E: c.WeakTypeTag, G: c.WeakTypeTag](c: blackbox.Context)(
    entityClass: c.Expr[Class[E]],
    generatorClass: c.Expr[Class[G]]): c.Expr[Unit] = handle(c)(entityClass) {
    import c.universe._
    val tpe = weakTypeOf[G]
    if(tpe.typeSymbol.isAbstract)
      ReflectionUtil.abort(
        Message.DOMALA4170,
        extractionClassString(tpe.toString))
    val ctor = tpe.typeSymbol.asClass.primaryConstructor
    if(!ctor.isPublic || ctor.asMethod.paramLists.flatten.nonEmpty)
      ReflectionUtil.abort(
        Message.DOMALA4171,
        extractionClassString(tpe.toString))
    reify(())
  }
  def validateSequenceIdGenerator[E, G <: SequenceIdGenerator](entityClass: Class[E], generatorClass: Class[G]): Unit = macro validateSequenceIdGeneratorImpl[E, G]

}
