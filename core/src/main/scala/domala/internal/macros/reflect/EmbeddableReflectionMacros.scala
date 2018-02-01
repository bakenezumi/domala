package domala.internal.macros.reflect

import domala.Column
import domala.internal.jdbc.entity.MacroEmbeddableDesc
import domala.internal.macros.reflect.util.{MacroTypeConverter, PropertyDescUtil}
import domala.internal.reflect.util.ReflectionUtil
import domala.jdbc.`type`.Types
import domala.jdbc.entity.{EntityPropertyDesc, NamingType}
import domala.message.Message

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
    column: c.Expr[Column]
  )(
    propertyClassTag: c.Expr[ClassTag[T]],
    nakedClassTag: c.Expr[ClassTag[N]]
  ): c.Expr[Map[String, EntityPropertyDesc[E, _]]] = handle(c)(embeddableClass) {
    import c.universe._
    PropertyDescUtil.generatePropertyDescImpl[T, E, N](c)(
      entityClass,
      paramName,
      namingType,
      c.Expr(Literal(Constant(false))),
      c.Expr(Literal(Constant(false))),
      c.Expr(Literal(Constant(null))),
      c.Expr(Literal(Constant(false))),
      c.Expr(Literal(Constant(false))),
      column
    )(propertyClassTag, nakedClassTag)
  }
  def generatePropertyDesc[EM, T, E, N](
    embeddableClass: Class[EM],
    propertyName: String,
    entityClass: Class[E],
    paramName: String,
    namingType: NamingType,
    column: Column,
  )(
    implicit propertyClassTag: ClassTag[T],
    nakedClassTag: ClassTag[N]
  ): Map[String, EntityPropertyDesc[E, _]] = macro generatePropertyDescImpl[EM, T, E, N]

  def generatePropertyDescMapImpl[EM: c.WeakTypeTag, E: c.WeakTypeTag](c: blackbox.Context)(
    embeddableClass: c.Expr[Class[E]],
    entityClass: c.Expr[Class[E]],
    propertyName: c.Expr[String],
    namingType: c.Expr[NamingType]
  ): c.Expr[Map[String, EntityPropertyDesc[E, _]]] = handle(c)(embeddableClass) {
    import c.universe._
    val embeddableType = weakTypeOf[EM]
    val entityType = weakTypeOf[E]

    val propertyDescList = embeddableType.typeSymbol.asClass.primaryConstructor.asMethod.paramLists.flatten.map((param: Symbol) => {

      param.annotations.collect {
        case a: Annotation if a.tree.tpe =:= typeOf[domala.Id] =>
          ReflectionUtil.abort(Message.DOMALA4289, embeddableType.typeSymbol.fullName.toString, param.name.toString)
        case a: Annotation if a.tree.tpe =:= typeOf[domala.GeneratedValue] =>
          ReflectionUtil.abort(Message.DOMALA4291, embeddableType.typeSymbol.fullName.toString, param.name.toString)
        case a: Annotation if a.tree.tpe =:= typeOf[domala.Version] =>
          ReflectionUtil.abort(Message.DOMALA4290, embeddableType.typeSymbol.fullName.toString, param.name.toString)
        case a: Annotation if a.tree.tpe =:= typeOf[domala.TenantId] =>
          ReflectionUtil.abort(Message.DOMALA4443, embeddableType.typeSymbol.fullName.toString, param.name.toString)
      }

      val propertyType = param.typeSignature
      val column = param.annotations.collectFirst {
        case a: Annotation if a.tree.tpe =:= typeOf[domala.Column] =>
          c.Expr[Column](q"domala.Column(..${a.tree.children.tail})")
      }.getOrElse {
        c.Expr[Column](
          q"""
            if($entityClass.getDeclaredConstructors.head.getParameters.exists(_.getName == ${param.name.toString}))
              domala.Column(name = $propertyName + "." + ${param.name.toString})
            else domala.Column()
           """
        )
      }

      val nakedTpe = MacroTypeConverter.of(c).toType(propertyType) match {
        case _: Types.Basic[_] => propertyType
        case _: Types.Option => propertyType.typeArgs.head
        case t if t.isHolder => propertyType
        case Types.MacroEntityType => propertyType
        case _ => ReflectionUtil.abort(Message.DOMALA4096, propertyType, embeddableType.typeSymbol.fullName, param.name)
      }
      c.Expr[Map[String, EntityPropertyDesc[E, _]]] {
        q"""
        domala.internal.macros.reflect.EmbeddableReflectionMacros.generatePropertyDesc[$embeddableType, $propertyType, $entityType, $nakedTpe](classOf[$embeddableType], ${param.name.toString}, $entityClass, $propertyName + "." + ${param.name.toString}, $namingType, $column)
        """
      }
    })

    c.Expr[Map[String, EntityPropertyDesc[E, _]]] {
      q"Seq(..$propertyDescList).flatten.toMap"
    }
  }
  def generatePropertyDescMap[EM, E](embeddableClass: Class[EM], entityClass: Class[E], propertyName: String, namingType: NamingType): Map[String, EntityPropertyDesc[E, _]] = macro generatePropertyDescMapImpl[EM, E]



  def generateEmbeddableDescImpl[EM: c.WeakTypeTag](c: blackbox.Context)(
    embeddableClass: c.Expr[Class[EM]]
  ): c.Expr[MacroEmbeddableDesc[EM]] = handle(c)(embeddableClass) {
    import c.universe._
    val embeddableType = weakTypeOf[EM]
    val companion = embeddableType.companion.typeSymbol
    val apply = companion.typeSignature.member(TermName("apply")).asMethod
    val applyParams: Seq[Tree] = apply.paramLists.head.map { p =>
      val parameterType = p.typeSignature
      MacroTypeConverter.of(c).toType(parameterType) match {
        case Types.MacroEntityType =>
          q"""
          val propertyName = embeddedPropertyName + "." + ${p.name.toString}
          val desc = domala.internal.macros.reflect.EmbeddableReflectionMacros.generateEmbeddableDesc(classOf[$parameterType])
          desc.newEmbeddable[ENTITY](propertyName, map, entityClass)
          """
        case _ =>
          q"""
          val paramName = ${p.name.toString}
          val propertyName = embeddedPropertyName + "." + paramName
          map.get(propertyName).map(_.asInstanceOf[domala.jdbc.entity.Property[ENTITY, _]].get().asInstanceOf[$parameterType]).getOrElse(
          throw new org.seasar.doma.DomaException(domala.message.Message.DOMALA6024, entityClass.getName(), propertyName, paramName))
          """
      }
    }

    c.Expr[MacroEmbeddableDesc[EM]] {
      q"""{
        domala.internal.jdbc.entity.MacroEmbeddableDesc.of($embeddableClass, {
          import scala.collection.JavaConverters._
          new domala.internal.jdbc.entity.MacroEmbeddableDesc[$embeddableType] {
            type EMBEDDABLE = $embeddableType
            override def getEmbeddablePropertyTypes[ENTITY](embeddedPropertyName: String, namingType: domala.jdbc.entity.NamingType, entityClass: Class[ENTITY]) =
              domala.internal.macros.reflect.EmbeddableReflectionMacros.generatePropertyDescMap(
                classOf[EMBEDDABLE],
                entityClass,
                embeddedPropertyName,
                namingType).values.toList.asJava
            override def newEmbeddable[ENTITY](embeddedPropertyName: String, map: Map[String, domala.jdbc.entity.Property[ENTITY, _]], entityClass: Class[ENTITY]): EMBEDDABLE = ${apply.asTerm}(..$applyParams)
          }
        })
      }"""

    }
  }
  def generateEmbeddableDesc[EM](embeddableClass: Class[EM]): MacroEmbeddableDesc[EM] = macro generateEmbeddableDescImpl[EM]

}
