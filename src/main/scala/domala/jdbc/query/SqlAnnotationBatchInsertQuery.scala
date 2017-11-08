package domala.jdbc.query

import java.lang.reflect.Method
import java.sql.Statement

import domala.jdbc.SqlNodeRepository
import org.seasar.doma.internal.jdbc.entity.{AbstractPostInsertContext, AbstractPreInsertContext}
import org.seasar.doma.internal.util.AssertionUtil.{assertEquals, assertNotNull}
import org.seasar.doma.jdbc.entity.EntityType
import org.seasar.doma.jdbc.query.BatchInsertQuery
import org.seasar.doma.jdbc.{Config, SqlKind}

class SqlAnnotationBatchInsertQuery[ELEMENT](
  elementClass: Class[ELEMENT],
  sql: String,
)(entityType: Option[_ >: EntityType[ELEMENT]] = None)(implicit sqlNodeRepository: SqlNodeRepository)
  extends SqlAnnotationBatchModifyQuery(elementClass, SqlKind.BATCH_INSERT, sql)(sqlNodeRepository) with BatchInsertQuery{

  val entityHandler: Option[EntityHandler] = entityType.map(e => new this.EntityHandler(e.asInstanceOf[EntityType[ELEMENT]]))

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

  protected class EntityHandler(var entityType: EntityType[ELEMENT]) {
    assertNotNull(entityType, "")

    def preInsert(): Unit = {
      val context = new SqlBatchPreInsertContext[ELEMENT](entityType, method, config)
      entityType.preInsert(currentEntity, context)
      if (context.getNewEntity != null) currentEntity = context.getNewEntity
    }

    def postInsert(): Unit = {
      val context = new SqlBatchPostInsertContext[ELEMENT](entityType, method, config)
      entityType.postInsert(currentEntity, context)
      if (context.getNewEntity != null) currentEntity = context.getNewEntity
    }
  }

  protected class SqlBatchPreInsertContext[E](entityType: EntityType[E], method: Method, config: Config) extends AbstractPreInsertContext[E](entityType, method, config) {
  }

  protected class SqlBatchPostInsertContext[E](entityType: EntityType[E], method: Method, config: Config) extends AbstractPostInsertContext[E](entityType, method, config) {
  }
}
