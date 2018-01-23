package domala.internal.jdbc.entity

import java.util
import java.util.function.Supplier

import domala.internal.jdbc.holder.AnyValHolderDescRepository
import domala.internal.macros.meta.util.MetaHelper
import domala.internal.reflect.util.{ReflectionUtil, RuntimeTypeConverter}
import domala.jdbc.`type`.Types
import domala.jdbc.entity._
import domala.jdbc.holder.HolderDesc
import domala.message.Message
import domala.{Column, Table}
import org.seasar.doma.DomaException
import org.seasar.doma.jdbc.ConfigSupport
import org.seasar.doma.jdbc.id.BuiltinIdentityIdGenerator
import org.seasar.doma.wrapper.Wrapper

import scala.collection.JavaConverters._
import scala.language.existentials
import scala.reflect._
import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe._

object RuntimeEntityDesc {

  private[this] val entityDescCache = scala.collection.concurrent.TrieMap[String,  AbstractEntityDesc[_]]()
  private[this] val entityConstructorCache = scala.collection.concurrent.TrieMap[String, (ru.MethodMirror, List[(String, Option[RuntimeEmbeddableDesc[_]])])]()
  private[this] val mirror = ru.runtimeMirror(Thread.currentThread.getContextClassLoader)

  def of[ENTITY: TypeTag: ClassTag]: AbstractEntityDesc[ENTITY] = {
    entityDescCache.getOrElseUpdate(
      classTag[ENTITY].toString() + classTag[ENTITY].hashCode(),
    {
      new AbstractEntityDesc[ENTITY] {
        override type ENTITY_LISTENER = NullEntityListener[ENTITY]

        override val listener = new NullEntityListener[ENTITY]()

        override val table: Table = {
          typeOf[ENTITY].typeSymbol.asClass.annotations.collectFirst {
            case a: ru.Annotation if a.tree.tpe =:= typeOf[domala.Table] =>
              Table.reflect(ru)(a)
          }.getOrElse(Table())
        }

        override protected val propertyDescMap: Map[String, EntityPropertyDesc[ENTITY, _]] = generatePropertyDescMap[ENTITY](getNamingType)

        override protected val idPropertyDescList: List[EntityPropertyDesc[ENTITY, _]] = propertyDescMap.values.collect {
          case p: AssignedIdPropertyDesc[_, _, _, _] => p: EntityPropertyDesc[ENTITY, _]
          case p: GeneratedIdPropertyDesc[_, _, _, _] => p: EntityPropertyDesc[ENTITY, _]
        }.toList

        override def getNamingType: NamingType = null

        override def getTenantIdPropertyType: TenantIdPropertyDesc[_ >: ENTITY, ENTITY, _, _] = null

        override def getVersionPropertyType: VersionPropertyDesc[_ >: ENTITY, ENTITY, _ <: Number, _] = propertyDescMap.values.collectFirst {
          case p: VersionPropertyDesc[_, _, _, _] => p
        }.orNull.asInstanceOf[VersionPropertyDesc[_ >: ENTITY, ENTITY, _ <: Number, _]]

        override def newEntity(__args: util.Map[String, Property[ENTITY, _]]): ENTITY = fromMap[ENTITY](__args.asScala.toMap)

        override def getGeneratedIdPropertyType: org.seasar.doma.jdbc.entity.GeneratedIdPropertyType[_ >: ENTITY, ENTITY, _ <: Number, _] = propertyDescMap.values.collectFirst {
          case p: org.seasar.doma.jdbc.entity.GeneratedIdPropertyType[_, _, _, _] => p
        }.orNull.asInstanceOf[GeneratedIdPropertyDesc[_ >: ENTITY, ENTITY, _ <: Number, _]]
      }
    }).asInstanceOf[AbstractEntityDesc[ENTITY]]
  }

  def clear(): Unit = {
    entityDescCache.clear()
    entityConstructorCache.clear()
  }

  def fromMap[ENTITY: TypeTag: ClassTag](propertyMap: Map[String, Property[ENTITY, _]]): ENTITY = {
    val (constructorMirror, constructorParams) = entityConstructorCache.getOrElseUpdate(
      classTag[ENTITY].toString() + classTag[ENTITY].hashCode(),
      {
        val classTest = typeOf[ENTITY].typeSymbol.asClass
        val classMirror = mirror.reflectClass(classTest)
        val constructor = typeOf[ENTITY].decl(termNames.CONSTRUCTOR).asMethod
        val cMirror = classMirror.reflectConstructor(constructor)

        val cParams: List[(String, Option[RuntimeEmbeddableDesc[_]])] =
          cMirror.symbol.paramLists.flatten.map((param: Symbol) => {
            val paramName = param.name.toString
            RuntimeTypeConverter.toType(param.typeSignature) match {
              case t if t.isRuntimeEmbeddable =>
                val embeddableTypeTag = toTypeTag(param.typeSignature)
                val embeddableDesc =
                  RuntimeEmbeddableDesc.of(embeddableTypeTag)
                (paramName, Some(embeddableDesc))
              case _ => (paramName, None)
            }
          })
        (cMirror, cParams)
      })

    val constructorArgs = constructorParams.map {
      case (paramName, Some(embeddableDesc)) => embeddableDesc.newEmbeddable[ENTITY](paramName, propertyMap)
      case (paramName, None) => propertyMap.get(paramName).map(_.get).getOrElse(
            throw new DomaException(Message.DOMALA6024, typeOf[ENTITY], paramName, paramName))
    }
    constructorMirror(constructorArgs:_*).asInstanceOf[ENTITY]
  }

  def generatePropertyDescMap[ENTITY: TypeTag: ClassTag](namingType: NamingType): Map[String, EntityPropertyDesc[ENTITY, _]] = {
    val tpe = typeOf[ENTITY]
    val constructor = tpe.decl(termNames.CONSTRUCTOR).asMethod

    constructor.paramLists.flatten.flatMap { (p: Symbol) =>
      val annotations = p.annotations
      val isId = annotations.exists { a: ru.Annotation =>
        a.tree.tpe =:= typeOf[domala.Id]
      }
      val isVersion = annotations.exists { a: ru.Annotation =>
        a.tree.tpe =:= typeOf[domala.Version]
      }
      val column: Column = annotations.collectFirst {
        case a: ru.Annotation if a.tree.tpe =:= typeOf[domala.Column] =>
          Column.reflect(ru)(a)
      }.getOrElse(Column())
      if (isId) {
        if (!column.insertable)
          MetaHelper.abort(Message.DOMALA4088, tpe.typeSymbol.name.toString, p.name.toString)
        if (!column.updatable)
          MetaHelper.abort(Message.DOMALA4089, tpe.typeSymbol.name.toString, p.name.toString)
        generateIdPropertyDesc[ENTITY](p.name.toString, p.typeSignature, column, namingType, p)
      } else if (isVersion) {
        generateVersionPropertyDesc[ENTITY](p.name.toString, p.typeSignature, column, namingType)
      } else
        generateDefaultPropertyDesc[ENTITY](p.name.toString, p.typeSignature, column, namingType)
    }.toMap
  }

  def generateDefaultPropertyDesc[ENTITY](
    paramName: String,
    propertyType: Type,
    column: Column,
    namingType: NamingType
  )(
    implicit
      entityTypeTag: TypeTag[ENTITY],
      entityClassTag: ClassTag[ENTITY],
  ): Map[String, EntityPropertyDesc[ENTITY, _]] = {
    val propertyClass = mirror.runtimeClass(propertyType)

    def basicPropertyDesc(tpe: Types.Basic[_], nakedClass: Class[Any]) = {
      val wrapperSupplier = tpe.wrapperSupplier
      DefaultPropertyDesc[ENTITY, Any, Any](
        EntityPropertyDescParam(
          entityClassTag.runtimeClass.asInstanceOf[Class[ENTITY]],
          propertyClass,
          BasicTypeDesc(
            nakedClass,
            wrapperSupplier.asInstanceOf[Supplier[Wrapper[Any]]]),
          paramName,
          column,
          namingType
        )
      ).asInstanceOf[EntityPropertyDesc[ENTITY, _]]
    }

    def holderPropertyDesc(tpe: Types.Holder[_, _], nakedClass: Class[Any]) = {
      val holderDesc =
        if (!tpe.isAnyValHolder)
          ReflectionUtil.getHolderDesc(nakedClass)
        else
          AnyValHolderDescRepository.getByClass(nakedClass, ConfigSupport.defaultClassHelper)
      DefaultPropertyDesc(
        EntityPropertyDescParam(
          entityClassTag.runtimeClass.asInstanceOf[Class[ENTITY]],
          propertyClass,
          holderDesc,
          paramName,
          column,
          namingType
        )
      )
    }

    def embeddedPropertyDesc(tpe: Types): Map[String, EntityPropertyDesc[ENTITY, _]] = {
      val embeddableTypeTag = toTypeTag(propertyType)
      val embeddableDesc =
        RuntimeEmbeddableDesc.of(embeddableTypeTag)
      new EmbeddedPropertyDesc[ENTITY, Any](
        paramName,
        entityClassTag.runtimeClass.asInstanceOf[Class[ENTITY]],
        embeddableDesc.getEmbeddablePropertyTypes[ENTITY](
          paramName,
          namingType
        )).getEmbeddablePropertyTypeMap.asScala.toMap
    }

    RuntimeTypeConverter.toType(propertyType) match {
      case tpe: Types.Basic[_] =>
        Map(paramName -> basicPropertyDesc(tpe, propertyClass.asInstanceOf[Class[Any]]))
      case Types.Option(tpe: Types.Basic[_]) =>
        val nakedType = propertyType.typeArgs.head
        val nakedClass = mirror.runtimeClass(nakedType)
        Map(paramName -> basicPropertyDesc(tpe, nakedClass.asInstanceOf[Class[Any]]))
      case tpe: Types.Holder[_, _] =>
        Map(paramName -> holderPropertyDesc(tpe, propertyClass.asInstanceOf[Class[Any]]))
      case Types.Option(tpe: Types.Holder[_, _]) =>
        val nakedType = propertyType.typeArgs.head
        val nakedClass = mirror.runtimeClass(nakedType)
        Map(paramName -> holderPropertyDesc(tpe, nakedClass.asInstanceOf[Class[Any]]))
      case t if t.isRuntimeEmbeddable =>
        embeddedPropertyDesc(t)
      case _ =>
        throw new DomaException(Message.DOMALA4096, propertyType, entityClassTag.runtimeClass.getName, paramName)
    }
  }

  def generateIdPropertyDesc[ENTITY](
    paramName: String,
    propertyType: Type,
    column: Column,
    namingType: NamingType,
    paramSymbol: Symbol
  )(
    implicit
    entityTypeTag: TypeTag[ENTITY],
    entityClassTag: ClassTag[ENTITY],
  ): Map[String, EntityPropertyDesc[ENTITY, _]] = {
    val propertyClass: Class[_] = mirror.runtimeClass(propertyType)
    val idGenerator = paramSymbol.annotations.collectFirst {
      case a: ru.Annotation if a.tree.tpe =:= typeOf[domala.GeneratedValue] =>
        a.tree.children.tail.head match {
          case Select(_, TermName("IDENTITY")) => new BuiltinIdentityIdGenerator()
          case x => throw new DomaException(Message.DOMALA6026, entityClassTag.runtimeClass.getName, paramName, x)
        }
    }
    def basicPropertyDesc(tpe: Types.Basic[_], nakedClass: Class[_]) = {
      val wrapperSupplier = tpe.wrapperSupplier
      idGenerator.map(g =>
        GeneratedIdPropertyDesc(g)(
          EntityPropertyDescParam(
            entityClassTag.runtimeClass.asInstanceOf[Class[ENTITY]],
            propertyClass,
            BasicTypeDesc[Number](nakedClass.asInstanceOf[Class[Number]],
            wrapperSupplier.asInstanceOf[Supplier[Wrapper[Number]]]),
            paramName,
            column,
            namingType
          ))
      ).getOrElse(
        AssignedIdPropertyDesc(
          EntityPropertyDescParam(
            entityClassTag.runtimeClass.asInstanceOf[Class[ENTITY]],
            propertyClass,
            BasicTypeDesc(
              nakedClass.asInstanceOf[Class[Any]],
              wrapperSupplier.asInstanceOf[Supplier[Wrapper[Any]]]),
            paramName,
            column,
            namingType
          )
        )
      ).asInstanceOf[EntityPropertyDesc[ENTITY, _]]
    }

    def holderPropertyDesc(tpe: Types.Holder[_, _], nakedClass: Class[_]) = {
      val holderDesc =
        if (!tpe.isAnyValHolder)
          ReflectionUtil.getHolderDesc(nakedClass)
        else
          AnyValHolderDescRepository.getByClass(nakedClass, ConfigSupport.defaultClassHelper)
      idGenerator.map(g =>
        GeneratedIdPropertyDesc(g)(
          EntityPropertyDescParam(
            entityClassTag.runtimeClass.asInstanceOf[Class[ENTITY]],
            propertyClass,
            holderDesc.asInstanceOf[HolderDesc[Number, _]],
            paramName,
            column,
            namingType
          ))
      ).getOrElse(
        AssignedIdPropertyDesc(
          EntityPropertyDescParam(
            entityClassTag.runtimeClass.asInstanceOf[Class[ENTITY]],
            propertyClass,
            holderDesc.asInstanceOf[HolderDesc[Any, Any]],
            paramName,
            column,
            namingType
          )
        )
      )
    }

    Map(paramName ->
      (RuntimeTypeConverter.toType(propertyType) match {
        case tpe: Types.Basic[_] =>
          basicPropertyDesc(tpe, propertyClass)
        case Types.Option(tpe: Types.Basic[_]) =>
          val nakedType = propertyType.typeArgs.head
          val nakedClass = mirror.runtimeClass(nakedType).asInstanceOf[Class[Any]]
          basicPropertyDesc(tpe, nakedClass)
        case tpe: Types.Holder[_, _] =>
          holderPropertyDesc(tpe, propertyClass)
        case Types.Option(tpe: Types.Holder[_, _]) =>
          val nakedType = propertyType.typeArgs.head
          val nakedClass = mirror.runtimeClass(nakedType).asInstanceOf[Class[Any]]
          holderPropertyDesc(tpe, nakedClass)
        case _ =>
          throw new DomaException(Message.DOMALA4096, propertyType, entityClassTag.runtimeClass.getName, paramName)
     })
    )
  }

  def generateVersionPropertyDesc[ENTITY](
    paramName: String,
    propertyType: Type,
    column: Column,
    namingType: NamingType
  )(
    implicit
    entityTypeTag: TypeTag[ENTITY],
    entityClassTag: ClassTag[ENTITY],
  ): Map[String, EntityPropertyDesc[ENTITY, _]] = {
    val propertyClass: Class[_] = mirror.runtimeClass(propertyType)

    def basicPropertyDesc(tpe: Types.Basic[_], nakedClass: Class[Number]) = {
      val wrapperSupplier = tpe.wrapperSupplier
      VersionPropertyDesc[ENTITY, Number, Any](
        EntityPropertyDescParam(
          entityClassTag.runtimeClass.asInstanceOf[Class[ENTITY]],
          propertyClass,
          BasicTypeDesc(
            nakedClass,
            wrapperSupplier.asInstanceOf[Supplier[Wrapper[Number]]]),
          paramName,
          column,
          namingType
         )
      ).asInstanceOf[EntityPropertyDesc[ENTITY, _]]
    }

    def holderPropertyDesc(tpe: Types.Holder[_, _], nakedClass: Class[_]) = {
      val holderDesc =
        if (!tpe.isAnyValHolder)
          ReflectionUtil.getHolderDesc(nakedClass)
        else
          AnyValHolderDescRepository.getByClass(nakedClass, ConfigSupport.defaultClassHelper)
      VersionPropertyDesc(
        EntityPropertyDescParam(
          entityClassTag.runtimeClass.asInstanceOf[Class[ENTITY]],
          propertyClass,
          holderDesc.asInstanceOf[HolderDesc[Number, _]],
          paramName,
          column,
          namingType
        )
      )
    }

    Map(paramName ->
      (RuntimeTypeConverter.toType(propertyType) match {
        case tpe: Types.Basic[_] if tpe.isNumber =>
          basicPropertyDesc(tpe, propertyClass.asInstanceOf[Class[Number]])
        case Types.Option(tpe: Types.Basic[_]) if tpe.isNumber =>
          val nakedType = propertyType.typeArgs.head
          val nakedClass = mirror.runtimeClass(nakedType)
          basicPropertyDesc(tpe, nakedClass.asInstanceOf[Class[Number]])
        case tpe: Types.Holder[_, _]  if tpe.isNumber =>
          holderPropertyDesc(tpe, propertyClass)
        case Types.Option(tpe: Types.Holder[_, _]) if tpe.isNumber =>
          val nakedType = propertyType.typeArgs.head
          val nakedClass = mirror.runtimeClass(nakedType)
          holderPropertyDesc(tpe, nakedClass)
        case _ =>
          throw new DomaException(Message.DOMALA4096, propertyType, entityClassTag.runtimeClass.getName, paramName)
      })
    )
  }

  def toTypeTag(tpe: Type): TypeTag[_] =
    TypeTag(mirror, new api.TypeCreator {
      def apply[U <: api.Universe with Singleton](m: api.Mirror[U]): U # Type =
        if (m eq mirror) tpe.asInstanceOf[U # Type]
        else throw new IllegalArgumentException(s"Type tag defined in $mirror cannot be migrated to other mirrors.")
    })

}
