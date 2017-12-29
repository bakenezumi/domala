package domala.internal.macros.reflect.util

import java.util.function.Supplier

import domala.internal.macros.reflect.EntityCollections
import domala.internal.macros.reflect.util.ReflectionUtil.{extractionClassString, extractionQuotedString}
import domala.jdbc.entity._
import domala.jdbc.holder.{AbstractAnyValHolderDesc, AbstractHolderDesc, HolderDesc}
import domala.message.Message
import org.seasar.doma.jdbc.entity.{EmbeddedPropertyType, NamingType}
import org.seasar.doma.jdbc.id.IdGenerator
import org.seasar.doma.wrapper.Wrapper

import scala.reflect.ClassTag
import scala.reflect.macros.blackbox

object PropertyTypeUtil {

  def generatePropertyTypeImpl[
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
    isBasic: c.Expr[Boolean],
    wrapperSupplier: c.Expr[Supplier[Wrapper[N]]],
    columnName: c.Expr[String],
    columnInsertable: c.Expr[Boolean],
    columnUpdatable: c.Expr[Boolean],
    columnQuote: c.Expr[Boolean],
    collections: c.Expr[EntityCollections[E]]
  )(
    propertyClassTag: c.Expr[ClassTag[T]],
    nakedClassTag: c.Expr[ClassTag[N]]): c.Expr[Object] = {
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
          ReflectionUtil.getEmbeddableDesc(propertyClassTag.splice)
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
        reify(ReflectionUtil.getHolderDesc(nakedClassTag.splice))
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
            val prop = GeneratedIdPropertyDesc.ofHolder(
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
            val prop = AssignedIdPropertyDesc.ofHolder(
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
          val prop = VersionPropertyDesc.ofHolder(
            entityClass.splice,
            propertyClassTag.splice.runtimeClass,
            holder.splice.asInstanceOf[HolderDesc[Number, _]],
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
          val prop = TenantIdPropertyDesc.ofHolder(
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
          val prop = DefaultPropertyDesc.ofHolder(
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
            val prop = GeneratedIdPropertyDesc.ofHolder(
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
            val prop = AssignedIdPropertyDesc.ofHolder(
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
          val prop = VersionPropertyDesc.ofHolder(
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
          val prop = TenantIdPropertyDesc.ofHolder(
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
          val prop = DefaultPropertyDesc.ofHolder(
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
            val prop = domala.jdbc.entity.GeneratedIdPropertyDesc.ofBasic(
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
            val prop = AssignedIdPropertyDesc.ofBasic(
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
          val prop = VersionPropertyDesc.ofBasic(
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
          val prop = TenantIdPropertyDesc.ofBasic(
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
          val prop = DefaultPropertyDesc.ofBasic(
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

}
