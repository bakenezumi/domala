package domala.jdbc.query

import java.sql.Statement

import org.seasar.doma.internal.util.AssertionUtil.assertNotNull
import org.seasar.doma.jdbc.query.InsertQuery
import org.seasar.doma.jdbc.SqlKind

class SqlFileInsertQuery[E](sqlFilePath: String)(entityAndEntityDesc: Option[EntityAndEntityDesc[E]] = None)
  extends SqlFileModifyQuery(SqlKind.INSERT, sqlFilePath) with InsertQuery {

  val entityHandler: Option[InsertEntityHandler[E]] = entityAndEntityDesc.map(e => new this.InsertEntityHandler(e.name, e.entity, e.entityDesc))

  override def prepare(): Unit = {
    super.prepare()
    assertNotNull(method, sqlFilePath)
    preInsert()
    prepareOptions()
    prepareExecutable()
    prepareSql()
  }

  protected def preInsert(): Unit = {
    entityHandler.foreach(_.preInsert())
  }

  protected def prepareExecutable(): Unit = {
    setExecutable()
  }

  def getEntity: E = entityHandler.map(_.entity).getOrElse(null.asInstanceOf[E])

  override def complete(): Unit = {
    entityHandler.foreach(_.postInsert())
  }

  override def generateId(statement: Statement): Unit = ()

}
