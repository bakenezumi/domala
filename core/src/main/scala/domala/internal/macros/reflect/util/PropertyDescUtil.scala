package domala.internal.macros.reflect.util

import java.util
import java.util.function.Supplier

import domala.Column
import domala.internal.reflect.util.ReflectionUtil
import domala.internal.reflect.util.ReflectionUtil.{extractionClassString, extractionQuotedString}
import domala.jdbc.entity._
import domala.jdbc.id.IdGenerator
import domala.jdbc.`type`.Types
import domala.message.Message
import org.seasar.doma.wrapper.Wrapper

import scala.reflect.ClassTag
import scala.reflect.macros.blackbox

object PropertyDescUtil {

  def abortPropertyDescUtil(entityClassName: String, paramName: String)(message: Message): Unit =
    ReflectionUtil.abort(
      message,
      entityClassName,
      paramName
    )

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
    nakedClassTag: c.Expr[ClassTag[N]]): c.Expr[Map[String, EntityPropertyDesc[E, _]]] = {
    import c.universe._

    val Literal(Constant(isIdLiteral: Boolean)) = isId.tree
    val Literal(Constant(isIdGenerateActualLiteral: Boolean)) = isIdGenerate.tree
    val Literal(Constant(isVersionLiteral: Boolean)) = isVersion.tree
    val Literal(Constant(isTenantIdLiteral: Boolean)) = isTenantId.tree
    val tpe = weakTypeOf[T]
    val nakedTpe = weakTypeOf[N]
    val converter = MacroTypeConverter.of(c)

    def abort = abortPropertyDescUtil(extractionClassString(entityClass.toString),extractionQuotedString(paramName.toString())) _

    def validateForEmbeddable(): Unit = {
      if (isIdLiteral) abort(Message.DOMALA4302)
      if (isIdGenerateActualLiteral) abort(Message.DOMALA4303)
      if (isVersionLiteral) abort(Message.DOMALA4304)
      if (isTenantIdLiteral) abort(Message.DOMALA4443)
    }

    converter.toType(tpe) match {
      case Types.MacroEntityType =>
        validateForEmbeddable()
        val embeddableDesc = c.Expr[util.List[EntityPropertyDesc[E, _]]] {
          q"domala.internal.macros.reflect.EmbeddableReflectionMacros.generateEmbeddableDesc[$tpe](classOf[$tpe]).getEmbeddablePropertyTypes($paramName, $namingType, $entityClass)"
        }
        reify {
          import scala.collection.JavaConverters._
          new EmbeddedPropertyDesc[E, T](
            paramName.splice,
            entityClass.splice,
            embeddableDesc.splice).getEmbeddablePropertyTypeMap.asScala.toMap
        }
      case _ =>
        val convertedType = converter.toType(nakedTpe)
        val entityPropertyDescParam = convertedType match {
          case _ if convertedType.isHolder =>
            val holderDesc = convertedType match {
              case Types.GeneratedHolderType(_) =>
                reify(ReflectionUtil.getHolderDesc(nakedClassTag.splice))
              case _ =>
                AnyValHolderDescGenerator.get[blackbox.Context, N](c)(nakedTpe).getOrElse(
                  ReflectionUtil.abort(Message.DOMALA6017,
                    extractionClassString(entityClass.toString))
                )
            }
            reify(
              EntityPropertyDescParam(
                entityClass.splice,
                propertyClassTag.splice.runtimeClass,
                holderDesc.splice,
                paramName.splice,
                column.splice,
                namingType.splice
              ))
          case _: Types.Basic[_] =>
            val wrapperSupplier = MacroUtil.generateWrapperSupplier(c)(nakedTpe)
            reify(
              EntityPropertyDescParam(
                entityClass.splice,
                propertyClassTag.splice.runtimeClass,
                BasicTypeDesc(
                  nakedClassTag.splice.runtimeClass.asInstanceOf[Class[N]],
                  wrapperSupplier.splice.asInstanceOf[Supplier[Wrapper[N]]]),
                paramName.splice,
                column.splice,
                namingType.splice
              ))
          case _ => ReflectionUtil.abort(Message.DOMALA4096,nakedTpe.typeSymbol.fullName.toString,
            extractionClassString(entityClass.toString), extractionQuotedString(paramName.toString()))
        }
        val entityPropertyDesc: c.Expr[EntityPropertyDesc[E, _]] =
          if (isIdLiteral) {
            if (isIdGenerateActualLiteral) {
              if (!convertedType.isNumber) abort(Message.DOMALA4095)
              reify(GeneratedIdPropertyDesc(idGenerator.splice)
                (entityPropertyDescParam.splice.asInstanceOf[EntityPropertyDescParam[E, Number, N]]))
            } else {
              reify(AssignedIdPropertyDesc(entityPropertyDescParam.splice))
            }
          } else if (isVersionLiteral) {
            if (!convertedType.isNumber) abort(Message.DOMALA4093)
            reify(VersionPropertyDesc(entityPropertyDescParam.splice.asInstanceOf[EntityPropertyDescParam[E, Number, N]]))
          } else if (isTenantIdLiteral) {
            reify(TenantIdPropertyDesc(entityPropertyDescParam.splice))
          } else {
            reify(DefaultPropertyDesc(entityPropertyDescParam.splice))
          }

        reify {
          Map(paramName.splice -> entityPropertyDesc.splice)
        }

    }
  }

}
