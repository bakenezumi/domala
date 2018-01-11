package domala.jdbc.query

import domala.jdbc.SqlNodeRepository
import domala.jdbc.entity.EntityDesc
import org.seasar.doma.internal.util.AssertionUtil.assertEquals
import org.seasar.doma.jdbc.SqlKind
import org.seasar.doma.jdbc.query.BatchDeleteQuery

class SqlFileBatchDeleteQuery[ELEMENT](
  elementClass: Class[ELEMENT],
  sqlFilePath: String,
  versionIgnored: Boolean = false,
  optimisticLockExceptionSuppressed: Boolean = false
)(entityDesc: Option[_ >: EntityDesc[ELEMENT]] = None)(implicit sqlNodeRepository: SqlNodeRepository)
  extends SqlFileBatchModifyQuery(elementClass, SqlKind.BATCH_INSERT, sqlFilePath) with BatchDeleteQuery {

  val entityHandler: Option[BatchDeleteEntityHandler] =
    entityDesc.map(e => new this.BatchDeleteEntityHandler(e.asInstanceOf[EntityDesc[ELEMENT]], versionIgnored, optimisticLockExceptionSuppressed))

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
