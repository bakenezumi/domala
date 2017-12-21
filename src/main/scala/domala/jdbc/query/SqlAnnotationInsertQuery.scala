package domala.jdbc.query

import java.lang.reflect.Method
import java.sql.Statement

import domala.jdbc.SqlNodeRepository
import org.seasar.doma.internal.jdbc.entity.{AbstractPostInsertContext, AbstractPreInsertContext}
import org.seasar.doma.internal.util.AssertionUtil.assertNotNull
import org.seasar.doma.jdbc.entity.EntityType
import org.seasar.doma.jdbc.query.InsertQuery
import org.seasar.doma.jdbc.{Config, SqlKind}

class SqlAnnotationInsertQuery[E](sqlString: String)(entityAndEntityType: Option[EntityAndEntityType[E]] = None)
  (implicit sqlNodeRepository: SqlNodeRepository)
  extends SqlAnnotationModifyQuery(SqlKind.INSERT, sqlString)(sqlNodeRepository) with InsertQuery {

  val entityHandler: Option[InsertEntityHandler[E]] = entityAndEntityType.map(e => new InsertEntityHandler[E](e.name, e.entity, e.entityType))

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
