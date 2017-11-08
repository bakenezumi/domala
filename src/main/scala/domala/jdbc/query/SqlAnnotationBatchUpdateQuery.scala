package domala.jdbc.query

import java.lang.reflect.Method
import java.sql.Statement

import domala.jdbc.SqlNodeRepository
import org.seasar.doma.internal.jdbc.entity.{AbstractPostUpdateContext, AbstractPreUpdateContext}
import org.seasar.doma.internal.jdbc.sql.SqlContext
import org.seasar.doma.internal.jdbc.sql.node.PopulateNode
import org.seasar.doma.internal.util.AssertionUtil.{assertEquals, assertNotNull}
import org.seasar.doma.jdbc.entity.EntityType
import org.seasar.doma.jdbc.query.BatchUpdateQuery
import org.seasar.doma.jdbc.{Config, SqlKind}

class SqlAnnotationBatchUpdateQuery[ELEMENT](
  elementClass: Class[ELEMENT],
  sql: String,
  versionIgnored: Boolean = false,
  optimisticLockExceptionSuppressed: Boolean = false
)(entityType: Option[_ >: EntityType[ELEMENT]] = None)(implicit sqlNodeRepository: SqlNodeRepository)
  extends SqlAnnotationBatchModifyQuery(elementClass, SqlKind.BATCH_INSERT, sql)(sqlNodeRepository) with BatchUpdateQuery {

  val entityHandler: Option[EntityHandler] = entityType.map(e => new this.EntityHandler(e.asInstanceOf[EntityType[ELEMENT]]))

  override def prepare(): Unit = {
    super.prepare()
    val size = elements.size
    if (size == 0) return
    setExecutable()
    currentEntity = elements.get(0)
    initEntityHandler()
    preUpdate()
    prepareOptions()
    prepareOptimisticLock()
    prepareTargetPropertyTypes()
    prepareSql()
    elements.set(0, currentEntity)
    val it = elements.listIterator(1)
    while ({it.hasNext}) {
      currentEntity = it.next
      preUpdate()
      prepareSql()
      it.set(currentEntity)
    }
    assertEquals(size, sqls.size)
  }

  protected def initEntityHandler(): Unit = {
    entityHandler.foreach(_.init())
  }

  protected def preUpdate(): Unit = {
    entityHandler.foreach(_.preUpdate())
  }

  protected def prepareTargetPropertyTypes(): Unit = {
    entityHandler.foreach(_.prepareTargetPropertyTypes())
  }

  override def populateValues(node: PopulateNode, context: SqlContext): Unit = {
    entityHandler.getOrElse(throw new UnsupportedOperationException).populateValues(context)
  }

  protected def prepareOptimisticLock(): Unit = {
    entityHandler.foreach(_.prepareOptimisticLock())
  }

  override def incrementVersions(): Unit = {
    entityHandler.foreach(_.incrementVersions())
  }

  def generateId(statement: Statement, index: Int): Unit = {
  }

  def complete(): Unit = {
    entityHandler.foreach { handler =>
      val it = elements.listIterator
      while ({it.hasNext}) {
        currentEntity = it.next
        handler.postUpdate()
        it.set(currentEntity)
      }
    }
  }

  protected class EntityHandler(entityType: EntityType[ELEMENT]) {
    import org.seasar.doma.jdbc.entity.EntityPropertyType
    import org.seasar.doma.jdbc.query.BatchUpdateQueryHelper

    assertNotNull(entityType, "")

    private val versionPropertyType = entityType.getVersionPropertyType
    protected var targetPropertyTypes: java.util.List[EntityPropertyType[ELEMENT, _]] = _
    protected var helper: BatchUpdateQueryHelper[ELEMENT] = _

    def init(): Unit = {
      helper = new BatchUpdateQueryHelper[ELEMENT](config, entityType, includedPropertyNames, excludedPropertyNames, versionIgnored, optimisticLockExceptionSuppressed)
    }

    def preUpdate(): Unit = {
      val context = new SqlBatchPreUpdateContext[ELEMENT](entityType, method, config)
      entityType.preUpdate(currentEntity, context)
      if (context.getNewEntity != null) currentEntity = context.getNewEntity
    }

    def prepareTargetPropertyTypes(): Unit = {
      targetPropertyTypes = helper.getTargetPropertyTypes
    }

    def postUpdate(): Unit = {
      val context = new SqlBatchPostUpdateContext[ELEMENT](entityType, method, config)
      entityType.postUpdate(currentEntity, context)
      if (context.getNewEntity != null) currentEntity = context.getNewEntity
    }

    import org.seasar.doma.internal.jdbc.sql.SqlContext

    def prepareOptimisticLock(): Unit = {
      if (versionPropertyType != null && !versionIgnored) if (!optimisticLockExceptionSuppressed) optimisticLockCheckRequired = true
    }

    def incrementVersions(): Unit = {
      if (versionPropertyType != null && !versionIgnored) {
        val it = elements.listIterator
        while ( {it.hasNext}) {
          val newEntity = versionPropertyType.increment(entityType, it.next)
          it.set(newEntity)
        }
      }
    }

    def populateValues(context: SqlContext): Unit = {
      helper.populateValues(currentEntity, targetPropertyTypes, versionPropertyType, context)
    }
  }

  protected class SqlBatchPreUpdateContext[E](entityType: EntityType[E], method: Method, config: Config) extends AbstractPreUpdateContext[E](entityType, method, config) {
    override def isEntityChanged: Boolean = true
    override def isPropertyChanged(propertyName: String): Boolean = {
      validatePropertyDefined(propertyName)
      true
    }
  }

  protected class SqlBatchPostUpdateContext[E](entityType: EntityType[E], method: Method, config: Config) extends AbstractPostUpdateContext[E](entityType, method, config) {
    override def isPropertyChanged(propertyName: String): Boolean = {
      validatePropertyDefined(propertyName)
      true
    }
  }
}
