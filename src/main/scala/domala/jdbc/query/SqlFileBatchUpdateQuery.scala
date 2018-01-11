package domala.jdbc.query

import java.sql.Statement

import domala.jdbc.SqlNodeRepository
import domala.jdbc.entity.EntityDesc
import org.seasar.doma.internal.jdbc.sql.SqlContext
import org.seasar.doma.internal.jdbc.sql.node.PopulateNode
import org.seasar.doma.internal.util.AssertionUtil.assertEquals
import org.seasar.doma.jdbc.SqlKind
import org.seasar.doma.jdbc.query.BatchUpdateQuery

class SqlFileBatchUpdateQuery[ELEMENT](
  elementClass: Class[ELEMENT],
  sqlFilePath: String,
  versionIgnored: Boolean = false,
  optimisticLockExceptionSuppressed: Boolean = false
)(entityDesc: Option[_ >: EntityDesc[ELEMENT]] = None)(implicit sqlNodeRepository: SqlNodeRepository)
  extends SqlFileBatchModifyQuery(elementClass, SqlKind.BATCH_INSERT, sqlFilePath) with BatchUpdateQuery {

  val entityHandler: Option[BatchUpdateEntityHandler] =
    entityDesc.map(e => new this.BatchUpdateEntityHandler(e.asInstanceOf[EntityDesc[ELEMENT]], versionIgnored, optimisticLockExceptionSuppressed))

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

}
