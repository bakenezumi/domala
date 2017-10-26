package domala.jdbc.query

import java.lang.reflect.Method

import org.seasar.doma.internal.util.AssertionUtil.{assertEquals, assertNotNull}
import org.seasar.doma.internal.jdbc.entity.AbstractPostDeleteContext
import org.seasar.doma.internal.jdbc.entity.AbstractPreDeleteContext
import org.seasar.doma.jdbc.{Config, SqlKind}
import org.seasar.doma.jdbc.entity.EntityType
import org.seasar.doma.jdbc.query.BatchDeleteQuery

class SqlAnnotationBatchDeleteQuery[ELEMENT](
  elementClass: Class[ELEMENT],
  sql: String,
  versionIgnored: Boolean = false,
  optimisticLockExceptionSuppressed: Boolean = false
)(entityType: Option[_ >: EntityType[ELEMENT]] = None) extends SqlAnnotationBatchModifyQuery(elementClass, SqlKind.BATCH_INSERT, sql) with BatchDeleteQuery {

  val entityHandler: Option[EntityHandler] = entityType.map(e => new this.EntityHandler(e.asInstanceOf[EntityType[ELEMENT]]))

  override def prepare(): Unit = {
    super.prepare()
    val size = elements.size
    if (size == 0) return
    setExecutable()
    currentEntity = elements.get(0)
    preDelete()
    prepareOptions()
    prepareOptimisticLock()
    prepareSql()
    elements.set(0, currentEntity)
    val it = elements.listIterator(1)
    while ({it.hasNext}) {
      currentEntity = it.next
      preDelete()
      prepareSql()
      it.set(currentEntity)
    }
    assertEquals(size, sqls.size)
  }

  protected def preDelete(): Unit = {
    entityHandler.foreach(_.preDelete())
  }

  protected def prepareOptimisticLock(): Unit = {
    entityHandler.foreach(_.prepareOptimisticLock())
  }

  def complete(): Unit = {
    entityHandler.foreach { handler =>
      val it = elements.listIterator
      while ({it.hasNext}) {
        currentEntity = it.next
        handler.postDelete()
        it.set(currentEntity)
      }
    }
  }

  protected class EntityHandler(entityType: EntityType[ELEMENT]) {
    assertNotNull(entityType, "")

    private val versionPropertyType = entityType.getVersionPropertyType

    def preDelete(): Unit = {
      val context = new SqlBatchPreDeleteContext[ELEMENT](entityType, method, config)
      entityType.preDelete(currentEntity, context)
      if (context.getNewEntity != null) currentEntity = context.getNewEntity
    }

    def postDelete(): Unit = {
      val context = new SqlBatchPostDeleteContext[ELEMENT](entityType, method, config)
      entityType.postDelete(currentEntity, context)
      if (context.getNewEntity != null) currentEntity = context.getNewEntity
    }

    def prepareOptimisticLock(): Unit = {
      if (versionPropertyType != null && !versionIgnored) if (!optimisticLockExceptionSuppressed) optimisticLockCheckRequired = true
    }
  }

  protected class SqlBatchPreDeleteContext[E](entityType: EntityType[E], method: Method, config: Config) extends AbstractPreDeleteContext[E](entityType, method, config) {
  }

  protected class SqlBatchPostDeleteContext[E](entityType: EntityType[E], method: Method, config: Config) extends AbstractPostDeleteContext[E](entityType, method, config) {
  }
}
