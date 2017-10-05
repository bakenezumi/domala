package domala.jdbc.query

import java.lang.reflect.Method

import org.seasar.doma.internal.jdbc.entity.{AbstractPostDeleteContext, AbstractPreDeleteContext}
import org.seasar.doma.internal.util.AssertionUtil.assertNotNull
import org.seasar.doma.jdbc.{Config, SqlKind}
import org.seasar.doma.jdbc.entity.EntityType
import org.seasar.doma.jdbc.query.DeleteQuery

class SqlAnnotationDeleteQuery[E](
  sqlString: String,
  nullExcluded: Boolean = false,
  versionIgnored: Boolean = false,
  optimisticLockExceptionSuppressed: Boolean = false)(
  entityAndEntityType: Option[EntityAndEntityType[E]] = None) extends SqlAnnotationModifyQuery(SqlKind.UPDATE, sqlString) with DeleteQuery {

  val entityHandler: Option[EntityHandler] = entityAndEntityType.map(e => new this.EntityHandler(e.name, e.entity, e.entityType))

  override def prepare(): Unit = {
    super.prepare()
    assertNotNull(method, sqlString)
    preDelete()
    prepareOptions()
    prepareOptimisticLock()
    prepareSql()
  }

  protected def preDelete(): Unit = {
    entityHandler.foreach(_.preDelete())
  }

  protected def prepareOptimisticLock(): Unit = entityHandler.foreach(_.prepareOptimisticLock())

  def getEntity: E = entityHandler.map(_.entity).getOrElse(null.asInstanceOf[E])

  override def complete(): Unit = {
    entityHandler.foreach(_.postDelete())
  }

  protected class EntityHandler(name: String, var entity: E, entityType: EntityType[E]) {
    assertNotNull(name, entity, entityType)
    private val versionPropertyType = entityType.getVersionPropertyType
    def preDelete(): Unit = {
      val context = new SqlPreDeleteContext(entityType, method, config)
      entityType.preDelete(entity, context)
      if (context.getNewEntity != null) {
        entity = context.getNewEntity
        addParameter(name, entityType.getEntityClass, entity)
      }
    }

    def postDelete(): Unit = {
      val context = new SqlPostDeleteContext(entityType, method, config)
      entityType.postDelete(entity, context)
      if (context.getNewEntity != null) entity = context.getNewEntity
    }

    def prepareOptimisticLock(): Unit = {
      if (versionPropertyType != null && !versionIgnored) if (!optimisticLockExceptionSuppressed) optimisticLockCheckRequired = true
    }
  }

  protected class SqlPreDeleteContext(entityType: EntityType[E], method: Method, config: Config) extends AbstractPreDeleteContext[E](entityType, method, config)

  protected class SqlPostDeleteContext(entityType: EntityType[E], method: Method, config: Config) extends AbstractPostDeleteContext[E](entityType, method, config)

}