package domala.internal.macros.reflect.util

import java.util.function.Supplier

import domala.internal.reflect.util.ReflectionUtil
import domala.internal.reflect.util.ReflectionUtil.{extractionClassString, extractionQuotedString}
import domala.jdbc.entity._
import domala.jdbc.holder.{AbstractAnyValHolderDesc, AbstractHolderDesc, HolderDesc}
import domala.jdbc.id.IdGenerator
import domala.jdbc.`type`.Types
import domala.message.Message
import org.seasar.doma.wrapper.Wrapper

import scala.reflect.ClassTag
import scala.reflect.macros.blackbox

object PropertyDescUtil {

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
    isBasic: c.Expr[Boolean],
    wrapperSupplier: c.Expr[Supplier[Wrapper[N]]],
    columnName: c.Expr[String],
    columnInsertable: c.Expr[Boolean],
    columnUpdatable: c.Expr[Boolean],
    columnQuote: c.Expr[Boolean]
  )(
    propertyClassTag: c.Expr[ClassTag[T]],
    nakedClassTag: c.Expr[ClassTag[N]]): c.Expr[Map[String, EntityPropertyDesc[E, _]]] = {
    import c.universe._
    val Literal(Constant(isBasicLiteral: Boolean)) = isBasic.tree
    val Literal(Constant(isIdLiteral: Boolean)) = isId.tree
    val Literal(Constant(isIdGenerateActualLiteral: Boolean)) = isIdGenerate.tree
    val Literal(Constant(isVersionLiteral: Boolean)) = isVersion.tree
    val Literal(Constant(isTenantIdLiteral: Boolean)) = isTenantId.tree
    val tpe = weakTypeOf[T]
    val nakedTpe = weakTypeOf[N]
    val converter = MacroTypeConverter.of(c)
    converter.toType(tpe) match {
      case Types.GeneratedEmbeddableType =>
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
          import scala.collection.JavaConverters._
          new EmbeddedPropertyDesc[E, T](
            paramName.splice,
            entityClass.splice,
            embeddable.getEmbeddablePropertyTypes(
              paramName.splice,
              entityClass.splice,
              namingType.splice
            )).getEmbeddablePropertyTypeMap.asScala.toMap
        }
      case _ => converter.toType(nakedTpe) match {
        case Types.GeneratedHolderType(valueType) =>
          val holder =
            reify(ReflectionUtil.getHolderDesc(nakedClassTag.splice))
          if (isIdLiteral) {
            if (isIdGenerateActualLiteral) {
              if (!valueType.isNumber) {
                ReflectionUtil.abort(
                  Message.DOMALA4095,
                  extractionClassString(entityClass.toString),
                  extractionQuotedString(paramName.toString())
                )
              }
              reify {
                Map(paramName.splice -> GeneratedIdPropertyDesc.ofHolder(
                  entityClass.splice,
                  propertyClassTag.splice.runtimeClass,
                  holder.splice.asInstanceOf[AbstractHolderDesc[Number, _]],
                  paramName.splice,
                  columnName.splice,
                  namingType.splice,
                  columnQuote.splice,
                  idGenerator.splice
                ))
              }
            } else {
              reify {
                Map(paramName.splice -> AssignedIdPropertyDesc.ofHolder(
                  entityClass.splice,
                  propertyClassTag.splice.runtimeClass,
                  holder.splice,
                  paramName.splice,
                  columnName.splice,
                  namingType.splice,
                  columnQuote.splice
                ))
              }
            }
          } else if (isVersionLiteral) {
            if (!valueType.isNumber) {
              ReflectionUtil.abort(
                Message.DOMALA4093,
                extractionClassString(entityClass.toString),
                extractionQuotedString(paramName.toString())
              )
            }
            reify {
              Map(paramName.splice -> VersionPropertyDesc.ofHolder(
                entityClass.splice,
                propertyClassTag.splice.runtimeClass,
                holder.splice.asInstanceOf[HolderDesc[Number, _]],
                paramName.splice,
                columnName.splice,
                namingType.splice,
                columnQuote.splice
              ))
            }
          } else if (isTenantIdLiteral) {
            reify {
              Map(paramName.splice -> TenantIdPropertyDesc.ofHolder(
                entityClass.splice,
                propertyClassTag.splice.runtimeClass,
                holder.splice,
                paramName.splice,
                columnName.splice,
                namingType.splice,
                columnQuote.splice
              ))
            }
          } else {
            reify {
              Map(paramName.splice -> DefaultPropertyDesc.ofHolder(
                entityClass.splice,
                propertyClassTag.splice.runtimeClass,
                holder.splice,
                paramName.splice,
                columnName.splice,
                namingType.splice,
                columnInsertable.splice,
                columnUpdatable.splice,
                columnQuote.splice
              ))
            }
          }
        case Types.AnyValHolderType(valueType) =>
          val holder = AnyValHolderDescGenerator.get[blackbox.Context, N](c)(nakedTpe)
          if (holder.isEmpty) {
            ReflectionUtil.abort(Message.DOMALA6017,
              extractionClassString(entityClass.toString))
          }
          if (isIdLiteral) {
            if (isIdGenerateActualLiteral) {
              if (!valueType.isNumber) {
                ReflectionUtil.abort(
                  Message.DOMALA4095,
                  extractionClassString(entityClass.toString),
                  extractionQuotedString(paramName.toString())
                )
              }
              reify {
                Map(paramName.splice -> GeneratedIdPropertyDesc.ofHolder(
                  entityClass.splice,
                  propertyClassTag.splice.runtimeClass,
                  holder.get.splice.asInstanceOf[AbstractAnyValHolderDesc[Number, _]],
                  paramName.splice,
                  columnName.splice,
                  namingType.splice,
                  columnQuote.splice,
                  idGenerator.splice
                ))
              }
            } else {
              reify {
                Map(paramName.splice -> AssignedIdPropertyDesc.ofHolder(
                  entityClass.splice,
                  propertyClassTag.splice.runtimeClass,
                  holder.get.splice,
                  paramName.splice,
                  columnName.splice,
                  namingType.splice,
                  columnQuote.splice
                ))
              }
            }
          } else if (isVersionLiteral) {
            if (!valueType.isNumber) {
              ReflectionUtil.abort(
                Message.DOMALA4093,
                extractionClassString(entityClass.toString),
                extractionQuotedString(paramName.toString())
              )
            }
            reify {
              Map(paramName.splice -> VersionPropertyDesc.ofHolder(
                entityClass.splice,
                propertyClassTag.splice.runtimeClass,
                holder.get.splice.asInstanceOf[AbstractAnyValHolderDesc[Number, _]],
                paramName.splice,
                columnName.splice,
                namingType.splice,
                columnQuote.splice
              ))
            }
          } else if (isTenantIdLiteral) {
            reify {
              Map(paramName.splice -> TenantIdPropertyDesc.ofHolder(
                entityClass.splice,
                propertyClassTag.splice.runtimeClass,
                holder.get.splice,
                paramName.splice,
                columnName.splice,
                namingType.splice,
                columnQuote.splice
              ))
            }
          } else {
            reify {
              Map(paramName.splice -> DefaultPropertyDesc.ofHolder(
                entityClass.splice,
                propertyClassTag.splice.runtimeClass,
                holder.get.splice,
                paramName.splice,
                columnName.splice,
                namingType.splice,
                columnInsertable.splice,
                columnUpdatable.splice,
                columnQuote.splice
              ))
            }
          }
        case convertedType =>
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
              if (!convertedType.isNumber) {
                ReflectionUtil.abort(
                  Message.DOMALA4095,
                  extractionClassString(entityClass.toString),
                  extractionQuotedString(paramName.toString())
                )
              }
              reify {
                Map(paramName.splice -> domala.jdbc.entity.GeneratedIdPropertyDesc.ofBasic(
                  entityClass.splice,
                  propertyClassTag.splice.runtimeClass,
                  nakedClassTag.splice.runtimeClass.asInstanceOf[Class[Number]],
                  wrapperSupplier.splice.asInstanceOf[Supplier[Wrapper[Number]]],
                  paramName.splice,
                  columnName.splice,
                  namingType.splice,
                  columnQuote.splice,
                  idGenerator.splice
                ))
              }
            } else {
              reify {
                Map(paramName.splice -> AssignedIdPropertyDesc.ofBasic(
                  entityClass.splice,
                  propertyClassTag.splice.runtimeClass,
                  nakedClassTag.splice.runtimeClass.asInstanceOf[Class[Any]],
                  wrapperSupplier.splice.asInstanceOf[Supplier[Wrapper[Any]]],
                  paramName.splice,
                  columnName.splice,
                  namingType.splice,
                  columnQuote.splice
                ))
              }
            }
          } else if (isVersionLiteral) {
            if (!convertedType.isNumber) {
              ReflectionUtil.abort(
                Message.DOMALA4093,
                extractionClassString(entityClass.toString),
                extractionQuotedString(paramName.toString())
              )
            }
            reify {
              Map(paramName.splice -> VersionPropertyDesc.ofBasic(
                entityClass.splice,
                propertyClassTag.splice.runtimeClass,
                nakedClassTag.splice.runtimeClass.asInstanceOf[Class[Number]],
                wrapperSupplier.splice.asInstanceOf[Supplier[Wrapper[Number]]],
                paramName.splice,
                columnName.splice,
                namingType.splice,
                columnQuote.splice
              ))
            }
          } else if (isTenantIdLiteral) {
            reify {
              Map(paramName.splice -> TenantIdPropertyDesc.ofBasic(
                entityClass.splice,
                propertyClassTag.splice.runtimeClass,
                nakedClassTag.splice.runtimeClass.asInstanceOf[Class[Number]],
                wrapperSupplier.splice.asInstanceOf[Supplier[Wrapper[Number]]],
                paramName.splice,
                columnName.splice,
                namingType.splice,
                columnQuote.splice
              ))
            }
          } else {
            reify {
              Map(paramName.splice -> DefaultPropertyDesc.ofBasic(
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
              ))
            }
          }
      }
    }
  }

}
