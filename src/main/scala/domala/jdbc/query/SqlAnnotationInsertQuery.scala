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

  val entityHandler: Option[EntityHandler] = entityAndEntityType.map(e => new this.EntityHandler(e.name, e.entity, e.entityType))

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

  protected class EntityHandler(name: String, var entity: E, entityType: EntityType[E]) {
    assertNotNull(name, entity, entityType)

    def preInsert(): Unit = {
      val context = new SqlPreInsertContext(entityType, method, config)
      entityType.preInsert(entity, context)
      if (context.getNewEntity != null) {
        entity = context.getNewEntity
        addParameter(name, entityType.getEntityClass, entity)
      }
    }

    def postInsert(): Unit = {
      val context = new SqlPostInsertContext(entityType, method, config)
      entityType.postInsert(entity, context)
      if (context.getNewEntity != null) entity = context.getNewEntity
    }
  }

  protected class SqlPreInsertContext(entityType: EntityType[E], method: Method, config: Config) extends AbstractPreInsertContext[E](entityType, method, config)

  protected class SqlPostInsertContext(entityType: EntityType[E], method: Method, config: Config) extends AbstractPostInsertContext[E](entityType, method, config)

  override def generateId(statement: Statement): Unit = ()

}
