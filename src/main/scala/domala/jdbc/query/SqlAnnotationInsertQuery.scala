package domala.jdbc.query

import java.sql.Statement

import domala.jdbc.SqlNodeRepository
import org.seasar.doma.internal.util.AssertionUtil.assertNotNull
import org.seasar.doma.jdbc.SqlKind
import org.seasar.doma.jdbc.query.InsertQuery

class SqlAnnotationInsertQuery[E](sqlString: String)(entityAndEntityDesc: Option[EntityAndEntityDesc[E]] = None)
  (implicit sqlNodeRepository: SqlNodeRepository)
  extends SqlAnnotationModifyQuery(SqlKind.INSERT, sqlString)(sqlNodeRepository) with InsertQuery {

  val entityHandler: Option[InsertEntityHandler[E]] = entityAndEntityDesc.map(e => new InsertEntityHandler[E](e.name, e.entity, e.entityDesc))

  override def prepare(): Unit = {
    super.prepare()
    assertNotNull(method, sqlString)
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
