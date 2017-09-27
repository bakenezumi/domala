package domala.internal.macros.reflect

import java.util.function.Supplier

import domala.jdbc.entity.{AssignedIdPropertyType, DefaultPropertyType, GeneratedIdPropertyType, VersionPropertyType}
import domala.jdbc.holder.AbstractHolderDesc
import org.seasar.doma.jdbc.entity._
import org.seasar.doma.jdbc.id.IdGenerator
import org.seasar.doma.wrapper.Wrapper

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object EntityReflectionMacros {

  def extractionClassString(str: String): String = {
    val r = ".*classOf\\[(.*)\\].*".r
    str match {
      case r(x) => x
      case _ => str
    }
  }
  def extractionQuotedString(str: String): String = {
    val r = """.*"(.*)".*""".r
    str match {
      case r(x) => x
      case _ => str
    }
  }

  def generatePropertyTypeImpl[T: c.WeakTypeTag, E: c.WeakTypeTag, N: c.WeakTypeTag](c: blackbox.Context)(
    propertyClass: c.Expr[Class[T]],
    entityClass: c.Expr[Class[E]],
    nakedClass: c.Expr[Class[N]],
    paramName: c.Expr[String],
    namingType: c.Expr[NamingType],
    isId: c.Expr[Boolean],
    isIdGenerate: c.Expr[Boolean],
    idGenerator: c.Expr[IdGenerator],
    isVersion: c.Expr[Boolean],
    isBasic: c.Expr[Boolean],
    wrapperSupplier: c.Expr[Supplier[Wrapper[N]]],
    columnName: c.Expr[String],
    columnInsertable: c.Expr[Boolean],
    columnUpdatable: c.Expr[Boolean],
    columnQuote: c.Expr[Boolean],
    collections: c.Expr[EntityCollections[E]]
  ): c.Expr[Object] = {
    import c.universe._
    val Literal(Constant(isBasicActual: Boolean)) = isBasic.tree
    val Literal(Constant(isIdActual: Boolean)) = isId.tree
    val Literal(Constant(isIdGenerateActual: Boolean)) = isIdGenerate.tree
    val Literal(Constant(isVersionActual: Boolean)) = isVersion.tree
    val tpe = weakTypeOf[T]
    val nakedTpe = weakTypeOf[N]
    if (tpe.companion <:< typeOf[EmbeddableType[_]]) {
      if(isIdActual) {
        c.abort(c.enclosingPosition, org.seasar.doma.message.Message.DOMA4302.getMessage(extractionClassString(entityClass.toString), extractionQuotedString(paramName.toString())))
      }
      if(isIdGenerateActual) {
        c.abort(c.enclosingPosition, org.seasar.doma.message.Message.DOMA4303.getMessage(extractionClassString(entityClass.toString), extractionQuotedString(paramName.toString())))
      }
      if(isVersionActual) {
        c.abort(c.enclosingPosition, org.seasar.doma.message.Message.DOMA4304.getMessage(extractionClassString(entityClass.toString), extractionQuotedString(paramName.toString())))
      }
      val embeddable = ReflectionUtil.getCompanion[EmbeddableType[_]](c)(propertyClass)
      reify {
        val prop = new EmbeddedPropertyType[E, T](
          paramName.splice,
          entityClass.splice,
          embeddable.splice.getEmbeddablePropertyTypes(
            paramName.splice,
            entityClass.splice,
            namingType.splice))
        collections.splice.putAll(prop)
        prop
      }
    } else if(nakedTpe.companion <:< typeOf[AbstractHolderDesc[_, _]]) {
      val domain = ReflectionUtil.getCompanion[AbstractHolderDesc[_, _]](c)(nakedClass)
      if(isIdActual) {
        if (isIdGenerateActual) {
          if(!(nakedTpe.companion <:< typeOf[AbstractHolderDesc[_ <: Number, _]])) {
            c.abort(c.enclosingPosition, org.seasar.doma.message.Message.DOMA4095.getMessage(extractionClassString(entityClass.toString), extractionQuotedString(paramName.toString())))
          }
          reify {
            val prop = GeneratedIdPropertyType.ofDomain(
              entityClass.splice,
              propertyClass.splice,
              domain.splice.asInstanceOf[AbstractHolderDesc[Number, _]],
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
            val prop = AssignedIdPropertyType.ofDomain(
              entityClass.splice,
              propertyClass.splice,
              domain.splice,
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
      } else if(isVersionActual) {
        if(!(nakedTpe.companion <:< typeOf[AbstractHolderDesc[_ <: Number, _]])) {
          c.abort(c.enclosingPosition, org.seasar.doma.message.Message.DOMA4093.getMessage(extractionClassString(entityClass.toString), extractionQuotedString(paramName.toString())))
        }
        reify {
          val prop = VersionPropertyType.ofDomain(
            entityClass.splice,
            propertyClass.splice,
            domain.splice.asInstanceOf[AbstractHolderDesc[Number, _]],
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
          val prop = DefaultPropertyType.ofDomain(
            entityClass.splice,
            propertyClass.splice,
            domain.splice,
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
      if(!isBasicActual) {
        c.abort(c.enclosingPosition, domala.message.Message.DOMALA4096.getMessage(extractionClassString(propertyClass.toString()), extractionClassString(entityClass.toString), extractionQuotedString(paramName.toString())))
      }
      if(isIdActual) {
        if(isIdGenerateActual) {
          if(!(nakedTpe <:< typeOf[Number])) {
            c.abort(c.enclosingPosition, org.seasar.doma.message.Message.DOMA4095.getMessage(extractionClassString(entityClass.toString), extractionQuotedString(paramName.toString())))
          }
          reify {
            val prop = new domala.jdbc.entity.GeneratedIdPropertyType(
              entityClass.splice,
              propertyClass.splice,
              nakedClass.splice.asInstanceOf[Class[Number]],
              wrapperSupplier.splice.asInstanceOf[Supplier[Wrapper[Number]]],
              null,
              null,
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
            val prop = new domala.jdbc.entity.AssignedIdPropertyType(
              entityClass.splice,
              propertyClass.splice,
              nakedClass.splice,
              wrapperSupplier.splice,
              null,
              null,
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
      } else if(isVersionActual) {
        if(!(nakedTpe <:< typeOf[Number])) {
          c.abort(c.enclosingPosition, org.seasar.doma.message.Message.DOMA4093.getMessage(extractionClassString(entityClass.toString), extractionQuotedString(paramName.toString())))
        }
        reify {
          val prop = new VersionPropertyType(
            entityClass.splice,
            propertyClass.splice,
            nakedClass.splice.asInstanceOf[Class[Number]],
            wrapperSupplier.splice.asInstanceOf[Supplier[Wrapper[Number]]],
            null,
            null,
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
          val prop = new DefaultPropertyType(
            entityClass.splice,
            propertyClass.splice,
            nakedClass.splice,
            wrapperSupplier.splice,
            null,
            null,
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
    propertyClass: Class[T],
    entityClass: Class[E],
    nakedClass: Class[N],
    paramName: String,
    namingType: NamingType,
    isId: Boolean,
    isIdGenerate: Boolean,
    idGenerator: IdGenerator,
    isVersion: Boolean,
    isBasic: Boolean,
    wrapperSupplier: Supplier[Wrapper[N]],
    columnName: String,
    columnInsertable: Boolean,
    columnUpdatable: Boolean,
    columnQuote: Boolean,
    collections: EntityCollections[E]
  ): Object = macro generatePropertyTypeImpl[T, E, N]

  def readPropertyImpl[T: c.WeakTypeTag, E: c.WeakTypeTag](c: blackbox.Context)(
    entityClass: c.Expr[Class[E]],
    args:  c.Expr[java.util.Map[String, Property[E, _]]],
    propertyName: c.Expr[String],
    propertyClass:  c.Expr[Class[T]]): c.Expr[T] = {
    import c.universe._
    val wtt = weakTypeOf[T]
    if(wtt.companion <:< typeOf[EmbeddableType[_]]) {
      val embeddable = ReflectionUtil.getCompanion[EmbeddableType[_]](c)(propertyClass)
      reify {
        embeddable.splice.newEmbeddable[E](propertyName.splice, args.splice).asInstanceOf[T]
      }
    } else {
      reify {
        (if (args.splice.get(propertyName.splice) != null) args.splice.get(propertyName.splice).get else null).asInstanceOf[T]
      }
    }
  }

  def readProperty[T, E](
    entityClass: Class[E],
    args: java.util.Map[String, Property[E, _]],
    propertyName: String,
    propertyClass: Class[T]): T = macro readPropertyImpl[T, E]

}

case class EntityCollections[E](
  list: java.util.List[EntityPropertyType[E, _]] = null,
  map: java.util.Map[String, EntityPropertyType[E, _]] = null,
  idList: java.util.List[EntityPropertyType[E, _]]= null
) {

  def putId(propertyType: EntityPropertyType[E, _]): Unit = {
    if (idList == null) return
    idList.add(propertyType)
  }

  def put(propertyName: String, propertyType: EntityPropertyType[E, _]): Unit = {
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
