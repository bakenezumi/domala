package domala.internal.macros.reflect

import java.util.function.Supplier

import domala.internal.macros.reflect.util.ReflectionUtil.{extractionClassString, extractionQuotedString}
import domala.internal.macros.reflect.util.{ReflectionUtil, TypeUtil}
import domala.jdbc.entity.{AssignedIdPropertyType, DefaultPropertyType, GeneratedIdPropertyType, TenantIdPropertyType, VersionPropertyType}
import domala.jdbc.holder.{AbstractAnyValHolderDesc, AbstractHolderDesc}
import domala.message.Message
import org.seasar.doma.jdbc.entity._
import org.seasar.doma.jdbc.id.{IdGenerator, SequenceIdGenerator, TableIdGenerator}
import org.seasar.doma.wrapper.Wrapper

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

  def generatePropertyTypeImpl[T: c.WeakTypeTag,
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
  ): c.Expr[Object] = handle(c)(entityClass) {
    import c.universe._
    val Literal(Constant(isBasicLiteral: Boolean)) = isBasic.tree
    val Literal(Constant(isIdLiteral: Boolean)) = isId.tree
    val Literal(Constant(isIdGenerateActualLiteral: Boolean)) = isIdGenerate.tree
    val Literal(Constant(isVersionLiteral: Boolean)) = isVersion.tree
    val Literal(Constant(isTenantIdLiteral: Boolean)) = isTenantId.tree
    val tpe = weakTypeOf[T]
    val nakedTpe = weakTypeOf[N]
    if (TypeUtil.isEmbeddable(c)(tpe)) {
      if (isIdLiteral) {
        ReflectionUtil.abort(
          Message.DOMALA4302,
          extractionClassString(entityClass.toString),
          extractionQuotedString(paramName.toString())
        )
      }
      if (isIdGenerateActualLiteral) {
        ReflectionUtil.abort(
          Message.DOMALA4303,
          extractionClassString(entityClass.toString),
          extractionQuotedString(paramName.toString())
        )
      }
      if (isVersionLiteral) {
        ReflectionUtil.abort(
          Message.DOMALA4304,
          extractionClassString(entityClass.toString),
          extractionQuotedString(paramName.toString())
        )
      }
      if (isTenantIdLiteral) {
        ReflectionUtil.abort(
          Message.DOMALA4443,
          extractionClassString(entityClass.toString),
          extractionQuotedString(paramName.toString())
        )
      }      
      reify {
        val embeddable =
          ReflectionUtil.getEmbeddableCompanion(propertyClassTag.splice)
        val prop = new EmbeddedPropertyType[E, T](
          paramName.splice,
          entityClass.splice,
          embeddable.getEmbeddablePropertyTypes(paramName.splice,
                                                entityClass.splice,
                                                namingType.splice))
        collections.splice.putAll(prop)
        prop
      }
    } else if (TypeUtil.isHolder(c)(nakedTpe)) {
      val holder =
          reify(ReflectionUtil.getHolderCompanion(nakedClassTag.splice))
      if (isIdLiteral) {
        if (isIdGenerateActualLiteral) {
          if (!TypeUtil.isNumberHolder(c)(nakedTpe)) {
            ReflectionUtil.abort(
              Message.DOMALA4095,
              extractionClassString(entityClass.toString),
              extractionQuotedString(paramName.toString())
            )
          }
          reify {
            val prop = GeneratedIdPropertyType.ofHolder(
              entityClass.splice,
              propertyClassTag.splice.runtimeClass,
              holder.splice.asInstanceOf[AbstractHolderDesc[Number, _]],
              paramName.splice,
              columnName.splice,
              namingType.splice,
              columnQuote.splice,
              idGenerator.splice
            )
            collections.splice.putId(prop)
            collections.splice.put(paramName.splice, prop)
            prop
          }
        } else {
          reify {
            val prop = AssignedIdPropertyType.ofHolder(
              entityClass.splice,
              propertyClassTag.splice.runtimeClass,
              holder.splice,
              paramName.splice,
              columnName.splice,
              namingType.splice,
              columnQuote.splice
            )
            collections.splice.putId(prop)
            collections.splice.put(paramName.splice, prop)
            prop
          }
        }
      } else if (isVersionLiteral) {
        if (!TypeUtil.isNumberHolder(c)(nakedTpe)) {
          ReflectionUtil.abort(
            Message.DOMALA4093,
            extractionClassString(entityClass.toString),
            extractionQuotedString(paramName.toString())
          )
        }
        reify {
          val prop = VersionPropertyType.ofHolder(
            entityClass.splice,
            propertyClassTag.splice.runtimeClass,
            holder.splice.asInstanceOf[AbstractHolderDesc[Number, _]],
            paramName.splice,
            columnName.splice,
            namingType.splice,
            columnQuote.splice
          )
          collections.splice.put(paramName.splice, prop)
          prop
        }
      } else if (isTenantIdLiteral) {
        reify {
          val prop = TenantIdPropertyType.ofHolder(
            entityClass.splice,
            propertyClassTag.splice.runtimeClass,
            holder.splice,
            paramName.splice,
            columnName.splice,
            namingType.splice,
            columnQuote.splice
          )
          collections.splice.put(paramName.splice, prop)
          prop
        }
      } else {
        reify {
          val prop = DefaultPropertyType.ofHolder(
            entityClass.splice,
            propertyClassTag.splice.runtimeClass,
            holder.splice,
            paramName.splice,
            columnName.splice,
            namingType.splice,
            columnInsertable.splice,
            columnUpdatable.splice,
            columnQuote.splice
          )
          collections.splice.put(paramName.splice, prop)
          prop
        }
      }
    } else if (TypeUtil.isAnyVal(c)(nakedTpe)) {
      val (basicType, holder) = TypeUtil.newAnyValHolderDesc[blackbox.Context, N](c)(nakedTpe)
      if (!TypeUtil.isBasic(c)(basicType)) {
        ReflectionUtil.abort(Message.DOMALA6014,
          extractionClassString(entityClass.toString),
          extractionQuotedString(paramName.toString()))
      }
      if (holder.isEmpty) {
        ReflectionUtil.abort(Message.DOMALA6017,
          extractionClassString(entityClass.toString))
      }
      if (isIdLiteral) {
        if (isIdGenerateActualLiteral) {
          if (!TypeUtil.isNumber(c)(basicType)) {
            ReflectionUtil.abort(
              Message.DOMALA4095,
              extractionClassString(entityClass.toString),
              extractionQuotedString(paramName.toString())
            )
          }
          reify {
            val prop = GeneratedIdPropertyType.ofAnyVal(
              entityClass.splice,
              propertyClassTag.splice.runtimeClass,
              holder.get.splice.asInstanceOf[AbstractAnyValHolderDesc[Number, _]],
              paramName.splice,
              columnName.splice,
              namingType.splice,
              columnQuote.splice,
              idGenerator.splice
            )
            collections.splice.putId(prop)
            collections.splice.put(paramName.splice, prop)
            prop
          }
        } else {
          reify {
            val prop = AssignedIdPropertyType.ofAnyVal(
              entityClass.splice,
              propertyClassTag.splice.runtimeClass,
              holder.get.splice,
              paramName.splice,
              columnName.splice,
              namingType.splice,
              columnQuote.splice
            )
            collections.splice.putId(prop)
            collections.splice.put(paramName.splice, prop)
            prop
          }
        }
      } else if (isVersionLiteral) {
        if (!TypeUtil.isNumber(c)(basicType)) {
          ReflectionUtil.abort(
            Message.DOMALA4093,
            extractionClassString(entityClass.toString),
            extractionQuotedString(paramName.toString())
          )
        }
        reify {
          val prop = VersionPropertyType.ofAnyVal(
            entityClass.splice,
            propertyClassTag.splice.runtimeClass,
            holder.get.splice.asInstanceOf[AbstractAnyValHolderDesc[Number, _]],
            paramName.splice,
            columnName.splice,
            namingType.splice,
            columnQuote.splice
          )
          collections.splice.put(paramName.splice, prop)
          prop
        }
      } else if (isTenantIdLiteral) {
        reify {
          val prop = TenantIdPropertyType.ofAnyVal(
            entityClass.splice,
            propertyClassTag.splice.runtimeClass,
            holder.get.splice,
            paramName.splice,
            columnName.splice,
            namingType.splice,
            columnQuote.splice
          )
          collections.splice.put(paramName.splice, prop)
          prop
        }
      } else {
        reify {
          val prop = DefaultPropertyType.ofAnyVal(
            entityClass.splice,
            propertyClassTag.splice.runtimeClass,
            holder.get.splice,
            paramName.splice,
            columnName.splice,
            namingType.splice,
            columnInsertable.splice,
            columnUpdatable.splice,
            columnQuote.splice
          )
          collections.splice.put(paramName.splice, prop)
          prop
        }
      }
    } else {
      if (!isBasicLiteral) {
        ReflectionUtil.abort(
          Message.DOMALA4096,
          extractionClassString(propertyClassTag.toString()),
          extractionClassString(entityClass.toString),
          extractionQuotedString(paramName.toString())
        )
      }
      if (isIdLiteral) {
        if (isIdGenerateActualLiteral) {
          if (!TypeUtil.isNumber(c)(nakedTpe)) {
            ReflectionUtil.abort(
              Message.DOMALA4095,
              extractionClassString(entityClass.toString),
              extractionQuotedString(paramName.toString())
            )
          }
          reify {
            val prop = domala.jdbc.entity.GeneratedIdPropertyType.ofBasic(
              entityClass.splice,
              propertyClassTag.splice.runtimeClass,
              nakedClassTag.splice.runtimeClass.asInstanceOf[Class[Number]],
              wrapperSupplier.splice.asInstanceOf[Supplier[Wrapper[Number]]],
              paramName.splice,
              columnName.splice,
              namingType.splice,
              columnQuote.splice,
              idGenerator.splice
            )
            collections.splice.putId(prop)
            collections.splice.put(paramName.splice, prop)
            prop
          }
        } else {
          reify {
            val prop = AssignedIdPropertyType.ofBasic(
              entityClass.splice,
              propertyClassTag.splice.runtimeClass,
              nakedClassTag.splice.runtimeClass.asInstanceOf[Class[Any]],
              wrapperSupplier.splice.asInstanceOf[Supplier[Wrapper[Any]]],
              paramName.splice,
              columnName.splice,
              namingType.splice,
              columnQuote.splice
            )
            collections.splice.putId(prop)
            collections.splice.put(paramName.splice, prop)
            prop
          }
        }
      } else if (isVersionLiteral) {
        if (!TypeUtil.isNumber(c)(nakedTpe)) {
          ReflectionUtil.abort(
            Message.DOMALA4093,
            extractionClassString(entityClass.toString),
            extractionQuotedString(paramName.toString())
          )
        }
        reify {
          val prop = VersionPropertyType.ofBasic(
            entityClass.splice,
            propertyClassTag.splice.runtimeClass,
            nakedClassTag.splice.runtimeClass.asInstanceOf[Class[Number]],
            wrapperSupplier.splice.asInstanceOf[Supplier[Wrapper[Number]]],
            paramName.splice,
            columnName.splice,
            namingType.splice,
            columnQuote.splice
          )
          collections.splice.put(paramName.splice, prop)
          prop
        }
      } else if (isTenantIdLiteral) {
        reify {
          val prop = TenantIdPropertyType.ofBasic(
            entityClass.splice,
            propertyClassTag.splice.runtimeClass,
            nakedClassTag.splice.runtimeClass.asInstanceOf[Class[Number]],
            wrapperSupplier.splice.asInstanceOf[Supplier[Wrapper[Number]]],
            paramName.splice,
            columnName.splice,
            namingType.splice,
            columnQuote.splice
          )
          collections.splice.put(paramName.splice, prop)
          prop
        }
      } else {
        reify {
          val prop = DefaultPropertyType.ofBasic(
            entityClass.splice,
            propertyClassTag.splice.runtimeClass,
            nakedClassTag.splice.runtimeClass.asInstanceOf[Class[Any]],
            wrapperSupplier.splice.asInstanceOf[Supplier[Wrapper[Any]]],
            paramName.splice,
            columnName.splice,
            namingType.splice,
            columnInsertable.splice,
            columnUpdatable.splice,
            columnQuote.splice
          )
          collections.splice.put(paramName.splice, prop)
          prop
        }
      }
    }
  }

  def generatePropertyType[T, E, N](
      entityClass: Class[E],
      paramName: String,
      namingType: NamingType,
      isId: Boolean,
      isIdGenerate: Boolean,
      idGenerator: IdGenerator,
      isVersion: Boolean,
      isTenantId: Boolean,
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

  def readPropertyImpl[T: c.WeakTypeTag, E: c.WeakTypeTag](c: blackbox.Context)(
      entityClass: c.Expr[Class[E]],
      args: c.Expr[java.util.Map[String, Property[E, _]]],
      propertyName: c.Expr[String])(
      propertyClassTag: c.Expr[ClassTag[T]]): c.Expr[T] = handle(c)(entityClass) {
    import c.universe._
    val wtt = weakTypeOf[T]
    if (TypeUtil.isEmbeddable(c)(wtt)) {
      reify {
        val embeddable =
          ReflectionUtil.getEmbeddableCompanion(propertyClassTag.splice)
        embeddable
          .newEmbeddable[E](propertyName.splice, args.splice)
          .asInstanceOf[T]
      }
    } else {
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

case class EntityCollections[E](
    list: java.util.List[EntityPropertyType[E, _]] = null,
    map: java.util.Map[String, EntityPropertyType[E, _]] = null,
    idList: java.util.List[EntityPropertyType[E, _]] = null
) {

  def putId(propertyType: EntityPropertyType[E, _]): Unit = {
    if (idList == null) return
    idList.add(propertyType)
  }

  def put(propertyName: String,
          propertyType: EntityPropertyType[E, _]): Unit = {
    if (list == null) return
    list.add(propertyType)
    map.put(propertyName, propertyType)
  }

  def putAll[T](propertyType: EmbeddedPropertyType[E, T]): Unit = {
    if (list == null) return
    list.addAll(propertyType.getEmbeddablePropertyTypes)
    map.putAll(propertyType.getEmbeddablePropertyTypeMap)
  }

}
