package domala.jdbc.entity

import java.util
import java.util.function.Supplier

import domala.{Column, Table}
import domala.internal.jdbc.holder.AnyValHolderDescRepository
import domala.internal.macros.meta.util.MetaHelper
import domala.jdbc.`type`.Types
import domala.internal.reflect.util.{ReflectionUtil, RuntimeTypeConverter}
import domala.message.Message
import org.seasar.doma.DomaException
import org.seasar.doma.jdbc.ConfigSupport
import org.seasar.doma.jdbc.entity._
import org.seasar.doma.wrapper.Wrapper

import scala.collection.JavaConverters._
import scala.language.existentials
import scala.reflect._
import scala.reflect.runtime.{universe => ru}
import ru._

class RuntimeEntityDesc[ENTITY: TypeTag : ClassTag] extends AbstractEntityDesc[ENTITY] {
  private[this] val table: Table = {
    typeOf[ENTITY].typeSymbol.asClass.annotations.collectFirst {
      case a: ru.Annotation if a.tree.tpe =:= typeOf[domala.Table] =>
        Table.reflect(ru)(a)
    }.getOrElse(Table())
  }

  private[this] val entityClass: Class[ENTITY] = classTag[ENTITY].runtimeClass.asInstanceOf[Class[ENTITY]]
  private[this] val propertyDescMap: Map[String, Either[Type, EntityPropertyDesc[ENTITY, _]]] = RuntimeEntityDesc.generatePropertyDescMap[ENTITY](getNamingType)
  private[this] val validPropertyDescMap: Map[String, EntityPropertyDesc[ENTITY, _]] = propertyDescMap.collect{ case (k, Right(v)) => k -> v }.toMap
  private[this] val validPropertyDescList = validPropertyDescMap.values.toList.asJava
  private[this] val idPropertyDescList = validPropertyDescMap.values.collect {
    case p:AssignedIdPropertyDesc[ENTITY, ENTITY, _, _] => p: EntityPropertyDesc[ENTITY, _]
  }.toList.asJava

  val isValid: Boolean = propertyDescMap.size == validPropertyDescMap.size
  val invalidPropertyMap: Map[String, ru.Type] = propertyDescMap.collect{ case (k, Left(v)) => k -> v }

  override def getOriginalStates(entity: ENTITY): ENTITY = null.asInstanceOf[ENTITY]

  override def getName: String = classTag[ENTITY].runtimeClass.getSimpleName

  override def getSchemaName: String = table.schema

  override def getCatalogName: String = table.catalog

  override def getNamingType: NamingType = null

  override def isQuoteRequired: Boolean = table.quote

  override def getIdPropertyTypes: util.List[EntityPropertyDesc[ENTITY, _]] = idPropertyDescList

  override def isImmutable: Boolean = true

  override def getEntityPropertyType(__name: String): EntityPropertyDesc[ENTITY, _] = validPropertyDescMap(__name)

  override def getTenantIdPropertyType: TenantIdPropertyDesc[_ >: ENTITY, ENTITY, _, _] = null

  override def saveCurrentStates(entity: ENTITY): Unit = ()

  override def getVersionPropertyType: VersionPropertyDesc[_ >: ENTITY, ENTITY, _ <: Number, _] = null

  override def getEntityPropertyTypes: util.List[EntityPropertyDesc[ENTITY, _]] = validPropertyDescList

  override def newEntity(__args: util.Map[String, Property[ENTITY, _]]): ENTITY = RuntimeEntityDesc.fromMap[ENTITY](__args.asScala.toMap)

  override def getTableName: String = getTableName(org.seasar.doma.jdbc.Naming.DEFAULT.apply _)

  override def getTableName(namingFunction: java.util.function.BiFunction[NamingType, String, String]): String = {
    if (table.name.isEmpty) {
      namingFunction.apply(getNamingType, getName)
    } else {
      table.name
    }

  }

  override def getEntityClass: Class[ENTITY] = entityClass

  override def getGeneratedIdPropertyType: GeneratedIdPropertyDesc[_ >: ENTITY, ENTITY, _ <: Number, _] = null

  object ListenerHolder {
    val listener = new NullEntityListener[ENTITY]()
  }

  private[this] val listenerSupplier: java.util.function.Supplier[NullEntityListener[ENTITY]] = () => ListenerHolder.listener

  override def preInsert(entity: ENTITY, context: PreInsertContext[ENTITY]): Unit = {
    val listenerClass = classOf[NullEntityListener[ENTITY]]
    val listener = context.getConfig.getEntityListenerProvider.get[ENTITY, NullEntityListener[ENTITY]](listenerClass, listenerSupplier)
    listener.preInsert(entity, context)
  }

  override def preUpdate(entity: ENTITY, context: PreUpdateContext[ENTITY]): Unit = {
    val listenerClass = classOf[NullEntityListener[ENTITY]]
    val listener = context.getConfig.getEntityListenerProvider.get[ENTITY, NullEntityListener[ENTITY]](listenerClass, listenerSupplier)
    listener.preUpdate(entity, context)
  }

  override def preDelete(entity: ENTITY, context: PreDeleteContext[ENTITY]): Unit = {
    val listenerClass = classOf[NullEntityListener[ENTITY]]
    val listener = context.getConfig.getEntityListenerProvider.get[ENTITY, NullEntityListener[ENTITY]](listenerClass, listenerSupplier)
    listener.preDelete(entity, context)
  }

  override def postInsert(entity: ENTITY, context: PostInsertContext[ENTITY]): Unit = {
    val listenerClass = classOf[NullEntityListener[ENTITY]]
    val listener = context.getConfig.getEntityListenerProvider.get[ENTITY, NullEntityListener[ENTITY]](listenerClass, listenerSupplier)
    listener.postInsert(entity, context)
  }

  override def postUpdate(entity: ENTITY, context: PostUpdateContext[ENTITY]): Unit = {
    val listenerClass = classOf[NullEntityListener[ENTITY]]
    val listener = context.getConfig.getEntityListenerProvider.get[ENTITY, NullEntityListener[ENTITY]](listenerClass, listenerSupplier)
    listener.postUpdate(entity, context)
  }

  override def postDelete(entity: ENTITY, context: PostDeleteContext[ENTITY]): Unit = {
    val listenerClass = classOf[NullEntityListener[ENTITY]]
    val listener = context.getConfig.getEntityListenerProvider.get[ENTITY, NullEntityListener[ENTITY]](listenerClass, listenerSupplier)
    listener.postDelete(entity, context)
  }

}

object RuntimeEntityDesc {

  private[this] val entityDescCache = scala.collection.concurrent.TrieMap[String,  Either[Map[String, ru.Type], RuntimeEntityDesc[_]]]()
  private[this] val entityConstructorCache = scala.collection.concurrent.TrieMap[String, (ru.MethodMirror, List[(String, Option[RuntimeEmbeddableDesc[_]])])]()
  private[this] val mirror = ru.runtimeMirror(Thread.currentThread.getContextClassLoader)

  def of[E: TypeTag: ClassTag]: Either[Map[String, ru.Type], RuntimeEntityDesc[E]] = {
    entityDescCache.getOrElseUpdate(
      classTag[E].toString() + classTag[E].hashCode(),
    {
      val entityDesc = new RuntimeEntityDesc[E]
      if (entityDesc.isValid) Right(entityDesc) else Left(entityDesc.invalidPropertyMap)
    }).asInstanceOf[Either[Map[String, ru.Type], RuntimeEntityDesc[E]]]
  }

  def clear(): Unit = {
    entityDescCache.clear()
    entityConstructorCache.clear()
  }

  def fromMap[E: TypeTag: ClassTag](propertyMap: Map[String, Property[E, _]]): E = {
    val (constructorMirror, constructorParams) = entityConstructorCache.getOrElseUpdate(
      classTag[E].toString() + classTag[E].hashCode(),
      {
        val classTest = typeOf[E].typeSymbol.asClass
        val classMirror = mirror.reflectClass(classTest)
        val constructor = typeOf[E].decl(termNames.CONSTRUCTOR).asMethod
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
      case (paramName, Some(embeddableDesc)) => embeddableDesc.newEmbeddable[E](paramName, propertyMap)
      case (paramName, None) => propertyMap.get(paramName).map(_.get).getOrElse(
            throw new DomaException(Message.DOMALA6024, typeOf[E], paramName, paramName))
    }
    constructorMirror(constructorArgs:_*).asInstanceOf[E]
  }

  def generatePropertyDescMap[E: TypeTag: ClassTag](namingType: NamingType): Map[String, Either[Type, EntityPropertyDesc[E, _]]] = {
    val tpe = typeOf[E]
    val constructor = tpe.decl(termNames.CONSTRUCTOR).asMethod

    constructor.paramLists.flatten.flatMap { (p: Symbol) =>
      val annotations = p.annotations
      val isId = annotations.exists { a: ru.Annotation =>
        a.tree.tpe =:= typeOf[domala.Id]
      }
      val column: Column = annotations.collectFirst {
        case a: ru.Annotation if a.tree.tpe =:= typeOf[domala.Column] =>
          Column.reflect(ru)(a)
      }.getOrElse(Column())
        if (isId) {
          if(!column.insertable)
            MetaHelper.abort(Message.DOMALA4088, tpe.typeSymbol.name.toString, p.name.toString)
          if(!column.updatable)
            MetaHelper.abort(Message.DOMALA4089, tpe.typeSymbol.name.toString, p.name.toString)
          generateIdPropertyDesc[E](p.name.toString, p.typeSignature, column, namingType)
        }
        else
          generateDefaultPropertyDesc[E](p.name.toString, p.typeSignature, column, namingType)
    }.toMap
  }

  def generateDefaultPropertyDesc[E](
    paramName: String,
    propertyType: Type,
    column: Column,
    namingType: NamingType
  )(
    implicit
      entityTypeTag: TypeTag[E],
      entityClassTag: ClassTag[E],
  ): Map[String, Either[Type, EntityPropertyDesc[E, _]]] = {
    val propertyClass = mirror.runtimeClass(propertyType)

    def basicPropertyDesc(tpe: Types.Basic[_], nakedClass: Class[Any]) = {
      val wrapperSupplier = tpe.wrapperSupplier
      Right(DefaultPropertyDesc.ofBasic[E, Any, Any](
        entityClassTag.runtimeClass.asInstanceOf[Class[E]],
        propertyClass,
        nakedClass,
        wrapperSupplier.asInstanceOf[Supplier[Wrapper[Any]]],
        paramName,
        column.name,
        namingType,
        insertable = column.insertable,
        updatable = column.updatable,
        quoteRequired = column.quote
      ).asInstanceOf[EntityPropertyDesc[E, _]])
    }

    def holderPropertyDesc(tpe: Types.Holder[_, _], nakedClass: Class[Any]) = {
      val holderDesc =
        if (!tpe.isAnyValHolder)
          ReflectionUtil.getHolderDesc(nakedClass)
        else
          AnyValHolderDescRepository.getByClass(nakedClass, ConfigSupport.defaultClassHelper)
      Right(DefaultPropertyDesc.ofHolder(
        entityClassTag.runtimeClass.asInstanceOf[Class[E]],
        propertyClass,
        holderDesc,
        paramName,
        column.name,
        namingType,
        insertable = column.insertable,
        updatable = column.updatable,
        quoteRequired = column.quote
      ))
    }

    def embeddedPropertyDesc(tpe: Types): Map[String, Right[Type, EntityPropertyDesc[E, _]]] = {
      val embeddableTypeTag = toTypeTag(propertyType)
      val embeddableDesc =
        RuntimeEmbeddableDesc.of(embeddableTypeTag)
      new EmbeddedPropertyDesc[E, Any](
        paramName,
        entityClassTag.runtimeClass.asInstanceOf[Class[E]],
        embeddableDesc.getEmbeddablePropertyTypes[E](
          paramName,
          namingType
        )).getEmbeddablePropertyTypeMap.asScala.map { case (k, v) => k -> Right(v) }.toMap
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
        Map(paramName -> Left(propertyType))
    }
  }

  def generateIdPropertyDesc[E](
    paramName: String,
    propertyType: Type,
    column: Column,
    namingType: NamingType
  )(
    implicit
    entityTypeTag: TypeTag[E],
    entityClassTag: ClassTag[E],
  ): Map[String, Either[Type, EntityPropertyDesc[E, _]]] = {
    val propertyClass = mirror.runtimeClass(propertyType)

    def basicPropertyDesc(tpe: Types.Basic[_], nakedClass: Class[Any]) = {
      val wrapperSupplier = tpe.wrapperSupplier
      Right(AssignedIdPropertyDesc.ofBasic[E, Any, Any](
        entityClassTag.runtimeClass.asInstanceOf[Class[E]],
        propertyClass,
        nakedClass,
        wrapperSupplier.asInstanceOf[Supplier[Wrapper[Any]]],
        paramName,
        column.name,
        namingType,
        quoteRequired = column.quote
      ).asInstanceOf[EntityPropertyDesc[E, _]])
    }

    def holderPropertyDesc(tpe: Types.Holder[_, _], nakedClass: Class[Any]) = {
      val holderDesc =
        if (!tpe.isAnyValHolder)
          ReflectionUtil.getHolderDesc(nakedClass)
        else
          AnyValHolderDescRepository.getByClass(nakedClass, ConfigSupport.defaultClassHelper)
      Right(AssignedIdPropertyDesc.ofHolder(
        entityClassTag.runtimeClass.asInstanceOf[Class[E]],
        propertyClass,
        holderDesc,
        paramName,
        column.name,
        namingType,
        quoteRequired = column.quote
      ))
    }

    Map(paramName ->
      (RuntimeTypeConverter.toType(propertyType) match {
        case tpe: Types.Basic[_] =>
          basicPropertyDesc(tpe, propertyClass.asInstanceOf[Class[Any]])
        case Types.Option(tpe: Types.Basic[_]) =>
          val nakedType = propertyType.typeArgs.head
          val nakedClass = mirror.runtimeClass(nakedType)
          basicPropertyDesc(tpe, nakedClass.asInstanceOf[Class[Any]])
        case tpe: Types.Holder[_, _] =>
          holderPropertyDesc(tpe, propertyClass.asInstanceOf[Class[Any]])
        case Types.Option(tpe: Types.Holder[_, _]) =>
          val nakedType = propertyType.typeArgs.head
          val nakedClass = mirror.runtimeClass(nakedType)
          holderPropertyDesc(tpe, nakedClass.asInstanceOf[Class[Any]])
        case _ =>
          Left(propertyType)
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
