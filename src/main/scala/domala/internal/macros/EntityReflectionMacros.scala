package domala.internal.macros

import java.util.function.Supplier

import domala.jdbc.entity.{AssignedIdPropertyType, DefaultPropertyType, GeneratedIdPropertyType, VersionPropertyType}
import domala.jdbc.holder.AbstractHolderDesc
import org.seasar.doma.jdbc.entity.{EmbeddedPropertyType, _}
import org.seasar.doma.jdbc.id.IdGenerator
import org.seasar.doma.wrapper.Wrapper

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object EntityReflectionMacros {

  def getCompanion(c: blackbox.Context)(param: c.Expr[Class[_]]): c.universe.Expr[Any] = {
    import c.universe._
    reify {
      Class.forName(param.splice.getName + "$").getField("MODULE$").get(null)
    }
  }

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

  def generatePropertyTypeImpl[T: c.WeakTypeTag, E: c.WeakTypeTag, B: c.WeakTypeTag](c: blackbox.Context)(
    propertyClass: c.Expr[Class[T]],
    entityClass: c.Expr[Class[E]],
    paramName: c.Expr[String],
    namingType: c.Expr[NamingType],
    isId: c.Expr[Boolean],
    isIdGenerate: c.Expr[Boolean],
    idGenerator: c.Expr[IdGenerator],
    isVersion: c.Expr[Boolean],
    isBasic: c.Expr[Boolean],
    basicClass: c.Expr[Class[B]],
    wrapperSupplier: c.Expr[Supplier[Wrapper[B]]],
    columnName: c.Expr[String],
    columnInsertable: c.Expr[Boolean],
    columnUpdatable: c.Expr[Boolean],
    columnQuote: c.Expr[Boolean],
    collections: c.Expr[EntityCollections[E]]
  ): c.universe.Expr[Object] = {
    import c.universe._
    val Literal(Constant(isBasicActual: Boolean)) = isBasic.tree
    val Literal(Constant(isIdActual: Boolean)) = isId.tree
    val Literal(Constant(isIdGenerateActual: Boolean)) = isIdGenerate.tree
    val Literal(Constant(isVersionActual: Boolean)) = isVersion.tree
    val wtt = weakTypeOf[T]
    if (wtt.companion <:< typeOf[EmbeddableType[_]]) {
      val embeddable = getCompanion(c)(propertyClass)
      reify {
        val prop = new EmbeddedPropertyType[E, T](
          paramName.splice,
          entityClass.splice,
          embeddable.splice.asInstanceOf[EmbeddableType[_]].getEmbeddablePropertyTypes(
            paramName.splice,
            entityClass.splice,
            namingType.splice))
        collections.splice.putAll(prop)
        prop
      }
    } else if(wtt.companion <:< typeOf[AbstractHolderDesc[_, _]] ||
      (wtt <:< typeOf[Option[_]] && wtt.typeArgs.head.companion <:< typeOf[AbstractHolderDesc[_, _]])) {
      val domain = if (wtt.companion <:< typeOf[AbstractHolderDesc[_, _]]) {
        getCompanion(c)(propertyClass)
      } else {
        getCompanion(c)(basicClass)
      }
      if(isIdActual) {
        if (isIdGenerateActual) {
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
              domain.splice.asInstanceOf[AbstractHolderDesc[Number, _]],
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
            domain.splice.asInstanceOf[AbstractHolderDesc[_, _]],
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
          reify {
            val prop = new domala.jdbc.entity.GeneratedIdPropertyType(
              entityClass.splice,
              propertyClass.splice,
              basicClass.splice.asInstanceOf[Class[Number]],
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
              basicClass.splice.asInstanceOf[Class[Number]],
              wrapperSupplier.splice.asInstanceOf[Supplier[Wrapper[Number]]],
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
        reify {
          val prop = new VersionPropertyType(
            entityClass.splice,
            propertyClass.splice,
            basicClass.splice.asInstanceOf[Class[Number]],
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
            basicClass.splice,
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

  def generatePropertyType[T, E, B](
    propertyClass: Class[T],
    entityClass: Class[E],
    paramName: String,
    namingType: NamingType,
    isId: Boolean,
    isIdGenerate: Boolean,
    idGenerator: IdGenerator,
    isVersion: Boolean,
    isBasic: Boolean,
    basicClass: Class[B],
    wrapperSupplier: Supplier[Wrapper[B]],
    columnName: String,
    columnInsertable: Boolean,
    columnUpdatable: Boolean,
    columnQuote: Boolean,
    collections: EntityCollections[E]
  ) = macro generatePropertyTypeImpl[T, E, B]

  def readPropertyImpl[T: c.WeakTypeTag, E: c.WeakTypeTag](c: blackbox.Context)(
    entityClass: c.Expr[Class[E]],
    args:  c.Expr[java.util.Map[String, Property[E, _]]],
    propertyName: c.Expr[String],
    propertyClass:  c.Expr[Class[T]]): c.universe.Expr[T] = {
    import c.universe._
    val wtt = weakTypeOf[T]
    if(wtt.companion <:< typeOf[EmbeddableType[_]]) {
      val embeddable = getCompanion(c)(propertyClass)
      reify {
        embeddable.splice.asInstanceOf[EmbeddableType[_]].newEmbeddable[E](propertyName.splice, args.splice).asInstanceOf[T]
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
