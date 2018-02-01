package domala.jdbc.entity

import java.util

import domala.Table
import org.seasar.doma.jdbc.entity._

import scala.collection.JavaConverters._
import scala.reflect.{ClassTag, classTag}

abstract class AbstractEntityDesc[ENTITY: ClassTag] extends AbstractEntityType[ENTITY] {
  type ENTITY_LISTENER <: EntityListener[ENTITY]
  protected val table: Table
  protected val listener: ENTITY_LISTENER
  protected val propertyDescMap: Map[String, EntityPropertyDesc[ENTITY, _]]
  protected val idPropertyDescList: List[EntityPropertyDesc[ENTITY, _]]

  private[this] lazy val entityClass: Class[ENTITY] = classTag[ENTITY].runtimeClass.asInstanceOf[Class[ENTITY]]
  private[this] lazy val propertyDescList = propertyDescMap.values.toList.asJava
  private[this] lazy val _idPropertyDescList = idPropertyDescList.asJava
  private[this] lazy val simpleName = entityClass.getSimpleName

  override def getOriginalStates(entity: ENTITY): ENTITY = null.asInstanceOf[ENTITY]

  override def getName: String = simpleName

  override def getSchemaName: String = table.schema

  override def getCatalogName: String = table.catalog

  override def isQuoteRequired: Boolean = table.quote

  override def getIdPropertyTypes: util.List[EntityPropertyDesc[ENTITY, _]] = _idPropertyDescList

  override def isImmutable: Boolean = true

  override def getEntityPropertyType(__name: String): EntityPropertyDesc[ENTITY, _] = propertyDescMap(__name)

  override def saveCurrentStates(entity: ENTITY): Unit = ()

  override def getEntityPropertyTypes: util.List[EntityPropertyDesc[ENTITY, _]] = propertyDescList

  override def getTableName: String = getTableName(org.seasar.doma.jdbc.Naming.DEFAULT.apply _)

  override def getTableName(namingFunction: java.util.function.BiFunction[NamingType, String, String]): String = {
    if (table.name.isEmpty) {
      namingFunction.apply(getNamingType, getName)
    } else {
      table.name
    }
  }

  override def getEntityClass: Class[ENTITY] = entityClass
  lazy private[this] val listenerClass: Class[ENTITY_LISTENER] = listener.getClass.asInstanceOf[Class[ENTITY_LISTENER]]

  private[this] val listenerSupplier: java.util.function.Supplier[ENTITY_LISTENER] = () => listener

  override def preInsert(entity: ENTITY, context: PreInsertContext[ENTITY]): Unit = {
    val listener = context.getConfig.getEntityListenerProvider.get[ENTITY, ENTITY_LISTENER](listenerClass, listenerSupplier)
    listener.preInsert(entity, context)
  }

  override def preUpdate(entity: ENTITY, context: PreUpdateContext[ENTITY]): Unit = {
    val listener = context.getConfig.getEntityListenerProvider.get[ENTITY, ENTITY_LISTENER](listenerClass, listenerSupplier)
    listener.preUpdate(entity, context)
  }

  override def preDelete(entity: ENTITY, context: PreDeleteContext[ENTITY]): Unit = {
    val listener = context.getConfig.getEntityListenerProvider.get[ENTITY, ENTITY_LISTENER](listenerClass, listenerSupplier)
    listener.preDelete(entity, context)
  }

  override def postInsert(entity: ENTITY, context: PostInsertContext[ENTITY]): Unit = {
    val listener = context.getConfig.getEntityListenerProvider.get[ENTITY, ENTITY_LISTENER](listenerClass, listenerSupplier)
    listener.postInsert(entity, context)
  }

  override def postUpdate(entity: ENTITY, context: PostUpdateContext[ENTITY]): Unit = {
    val listener = context.getConfig.getEntityListenerProvider.get[ENTITY, ENTITY_LISTENER](listenerClass, listenerSupplier)
    listener.postUpdate(entity, context)
  }

  override def postDelete(entity: ENTITY, context: PostDeleteContext[ENTITY]): Unit = {
    val listener = context.getConfig.getEntityListenerProvider.get[ENTITY, ENTITY_LISTENER](listenerClass, listenerSupplier)
    listener.postDelete(entity, context)
  }

}
