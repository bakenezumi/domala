package domala.jdbc.query

import domala.jdbc.SqlNodeRepository
import org.seasar.doma.internal.util.AssertionUtil.assertEquals
import org.seasar.doma.jdbc.SqlKind
import org.seasar.doma.jdbc.entity.EntityType
import org.seasar.doma.jdbc.query.BatchDeleteQuery

class SqlAnnotationBatchDeleteQuery[ELEMENT](
  elementClass: Class[ELEMENT],
  sql: String,
  versionIgnored: Boolean = false,
  optimisticLockExceptionSuppressed: Boolean = false
)(entityType: Option[_ >: EntityType[ELEMENT]] = None)(implicit sqlNodeRepository: SqlNodeRepository)
  extends SqlAnnotationBatchModifyQuery(elementClass, SqlKind.BATCH_INSERT, sql)(sqlNodeRepository) with BatchDeleteQuery {

  val entityHandler: Option[BatchDeleteEntityHandler] =
    entityType.map(e => new this.BatchDeleteEntityHandler(e.asInstanceOf[EntityType[ELEMENT]], versionIgnored, optimisticLockExceptionSuppressed))

  setConfig(config)

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

}
