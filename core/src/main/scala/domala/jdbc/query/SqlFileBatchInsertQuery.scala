package domala.jdbc.query

import java.sql.Statement

import domala.jdbc.entity.EntityDesc
import org.seasar.doma.internal.util.AssertionUtil.assertEquals
import org.seasar.doma.jdbc.SqlKind
import org.seasar.doma.jdbc.query.BatchInsertQuery

class SqlFileBatchInsertQuery[ELEMENT](
  elementClass: Class[ELEMENT],
  sqlFilePath: String,
)(entityDesc: Option[_ >: EntityDesc[ELEMENT]] = None)
  extends SqlFileBatchModifyQuery(elementClass, SqlKind.BATCH_INSERT, sqlFilePath) with BatchInsertQuery{

  val entityHandler: Option[BatchInsertEntityHandler] = entityDesc.map(e => new this.BatchInsertEntityHandler(e.asInstanceOf[EntityDesc[ELEMENT]]))

  override def prepare(): Unit = {
    super.prepare()
    val size = elements.size
    if (size == 0) return
    setExecutable()
    currentEntity = elements.get(0)
    preInsert()
    prepareOptions()
    prepareSql()
    elements.set(0, currentEntity)
    val it = elements.listIterator(1)
    while ( {it.hasNext}) {
      currentEntity = it.next
      preInsert()
      prepareSql()
      it.set(currentEntity)
    }
    assertEquals(size, sqls.size)
  }

  protected def preInsert(): Unit = {
    entityHandler.foreach(_.preInsert())
  }

  def generateId(statement: Statement, index: Int): Unit = {
  }

  def complete(): Unit = {
    entityHandler.foreach { handler =>
      val it = elements.listIterator
      while ( {it.hasNext}) {
        currentEntity = it.next
        handler.postInsert()
        it.set(currentEntity)
      }
    }
  }

  def isBatchSupported = true

}
