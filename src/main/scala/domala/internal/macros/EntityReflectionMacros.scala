package domala.internal.macros

import java.util
import java.util.function.Supplier

import domala.jdbc.entity.{DefaultPropertyType, VersionPropertyType}
import domala.jdbc.holder.AbstractHolderDesc
import org.seasar.doma.jdbc.entity._
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

  def generatePropertyTypeImpl[T: c.WeakTypeTag, E: c.WeakTypeTag, B: c.WeakTypeTag](c: blackbox.Context)(
    propertyClass: c.Expr[Class[T]],
    entityClass: c.Expr[Class[E]],
    paramName: c.Expr[String],
    namingType: c.Expr[NamingType],
    isId: c.Expr[Boolean],
    idGenerator: c.Expr[IdGenerator],
    isVersion: c.Expr[Boolean],
    basicClass: c.Expr[Class[B]],
    wrapperSupplier: c.Expr[Supplier[Wrapper[B]]],
    columnName: c.Expr[String],
    columnInsertable: c.Expr[Boolean],
    columnUpdatable: c.Expr[Boolean],
    columnQuote: c.Expr[Boolean],
    list: c.Expr[java.util.ArrayList[_ <: EntityPropertyType[E, _]]],
    map: c.Expr[ java.util.HashMap[String, _ <: EntityPropertyType[E, _]]],
    idList: c.Expr[java.util.ArrayList[_ <: EntityPropertyType[E, _]]],
  ): c.universe.Expr[Object] = {
    import c.universe._
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
        val __list = list.splice.asInstanceOf[util.ArrayList[EntityPropertyType[E, _]]]
        __list.addAll(prop.getEmbeddablePropertyTypes)
        val __map = map.splice.asInstanceOf[util.HashMap[String, EntityPropertyType[E, _]]]
        __map.putAll(prop.getEmbeddablePropertyTypeMap)
        prop
      }
    } else {
      val Literal(Constant(isIdActual: Boolean)) = isId.tree
      val Literal(Constant(isVersionActual: Boolean)) = isVersion.tree
      if(wtt.companion <:< typeOf[AbstractHolderDesc[_, _]] || (wtt <:< typeOf[Option[_]] && wtt.typeArgs.head.companion <:< typeOf[AbstractHolderDesc[_, _]])) {
        val domain = if (wtt.companion <:< typeOf[AbstractHolderDesc[_, _]]) {
          getCompanion(c)(propertyClass)
        } else {
          getCompanion(c)(basicClass)
        }
        reify {
          val prop = DefaultPropertyType.ofDomain(
            entityClass.splice,
            propertyClass.splice,
            domain.splice.asInstanceOf[AbstractHolderDesc[_, T]],
            paramName.splice,
            columnName.splice,
            namingType.splice,
            columnInsertable.splice,
            columnUpdatable.splice,
            columnQuote.splice
          )
          val __list = list.splice.asInstanceOf[util.ArrayList[EntityPropertyType[E, _]]]
          __list.add(prop.asInstanceOf[EntityPropertyType[E, _]])
          val __map = map.splice.asInstanceOf[util.HashMap[String, EntityPropertyType[E, _]]]
          __map.put(paramName.splice ,prop.asInstanceOf[EntityPropertyType[E, _]])
          prop
        }
      } else if(isIdActual) {
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
          val __idList = idList.splice.asInstanceOf[util.ArrayList[EntityPropertyType[E, _]]]
          __idList.add(prop.asInstanceOf[EntityPropertyType[E, _]])
          val __list = list.splice.asInstanceOf[util.ArrayList[EntityPropertyType[E, _]]]
          __list.add(prop.asInstanceOf[EntityPropertyType[E, _]])
          val __map = map.splice.asInstanceOf[util.HashMap[String, EntityPropertyType[E, _]]]
          __map.put(paramName.splice ,prop.asInstanceOf[EntityPropertyType[E, _]])
          prop
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
          val __list = list.splice.asInstanceOf[util.ArrayList[EntityPropertyType[E, _]]]
          __list.add(prop.asInstanceOf[EntityPropertyType[E, _]])
          val __map = map.splice.asInstanceOf[util.HashMap[String, EntityPropertyType[E, _]]]
          __map.put(paramName.splice ,prop.asInstanceOf[EntityPropertyType[E, _]])
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
          val __list = list.splice.asInstanceOf[util.ArrayList[EntityPropertyType[E, _]]]
          __list.add(prop.asInstanceOf[EntityPropertyType[E, _]])
          val __map = map.splice.asInstanceOf[util.HashMap[String, EntityPropertyType[E, _]]]
          __map.put(paramName.splice ,prop.asInstanceOf[EntityPropertyType[E, _]])
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
    idGenerator: IdGenerator,
    isVersion: Boolean,
    basicClass: Class[B],
    wrapperSupplier: Supplier[Wrapper[B]],
    columnName: String,
    columnInsertable: Boolean,
    columnUpdatable: Boolean,
    columnQuote: Boolean,
    list: java.util.ArrayList[_ <: EntityPropertyType[E, _]],
    map: java.util.HashMap[String, _ <: EntityPropertyType[E, _]],
    idList: java.util.ArrayList[_ <: EntityPropertyType[E, _]],
  ) = macro generatePropertyTypeImpl[T, E, B]


  def readPropertyImpl[T: c.WeakTypeTag, E: c.WeakTypeTag](c: blackbox.Context)(
    args:  c.Expr[java.util.Map[String, _ <: Property[E, _]]],
    propertyName: c.Expr[String],
    propertyClass:  c.Expr[Class[T]]): c.universe.Expr[T] = {
    import c.universe._
    val wtt = weakTypeOf[T]
    if(wtt.companion <:< typeOf[EmbeddableType[_]]) {
      val embeddable = getCompanion(c)(propertyClass)
      reify {
        embeddable.splice.asInstanceOf[EmbeddableType[_]].newEmbeddable[E](propertyName.splice, args.splice.asInstanceOf[java.util.Map[String, Property[E, _]]]).asInstanceOf[T]
      }
    } else {
      reify {
        (if (args.splice.get(propertyName.splice) != null) args.splice.get(propertyName.splice).get else null).asInstanceOf[T]
      }
    }
  }

  def readProperty[T, E](
    args: java.util.Map[String, _ <: Property[E, _]],
    propertyName: String,
    propertyClass: Class[T]) = macro readPropertyImpl[T, E]

}
