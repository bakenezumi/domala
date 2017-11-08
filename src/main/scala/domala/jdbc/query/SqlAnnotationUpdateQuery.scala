package domala.jdbc.query

import java.lang.reflect.Method

import domala.jdbc.SqlNodeRepository
import org.seasar.doma.internal.jdbc.entity.{AbstractPostUpdateContext, AbstractPreUpdateContext}
import org.seasar.doma.internal.jdbc.sql.SqlContext
import org.seasar.doma.internal.jdbc.sql.node.PopulateNode
import org.seasar.doma.internal.util.AssertionUtil.assertNotNull
import org.seasar.doma.jdbc.entity.{EntityPropertyType, EntityType}
import org.seasar.doma.jdbc.query.{UpdateQuery, UpdateQueryHelper}
import org.seasar.doma.jdbc.{Config, SqlKind}


class SqlAnnotationUpdateQuery[E](
  sqlString: String,
  nullExcluded: Boolean = false,
  versionIgnored: Boolean = false,
  optimisticLockExceptionSuppressed: Boolean = false,
  includedPropertyNames: Array[String] = new Array[String](0),
  excludedPropertyNames: Array[String] = new Array[String](0)
)(entityAndEntityType: Option[EntityAndEntityType[E]] = None)(implicit sqlNodeRepository: SqlNodeRepository)
  extends SqlAnnotationModifyQuery(SqlKind.UPDATE, sqlString)(sqlNodeRepository) with UpdateQuery {

  //noinspection VarCouldBeVal
  var targetPropertyTypes: java.util.List[EntityPropertyType[E, _]] = _
  val entityHandler: Option[EntityHandler] = entityAndEntityType.map(e => new this.EntityHandler(e.name, e.entity, e.entityType))

  override def prepare(): Unit = {
    super.prepare()
    assertNotNull(method, sqlString)
    initEntityHandler()
    preUpdate()
    prepareOptimisticLock()
    prepareOptions()
    prepareTargetPropertyTypes()
    prepareExecutable()
    prepareSql()
  }

  protected def initEntityHandler(): Unit = entityHandler.foreach(_.init())

  protected def preUpdate(): Unit = entityHandler.foreach(_.preUpdate())

  protected def prepareTargetPropertyTypes(): Unit = entityHandler.foreach(_.prepareTargetPropertyTypes())

  protected def prepareExecutable(): Unit = {
    if (entityHandler.isEmpty || entityHandler.get.hasTargetPropertyTypes) {
      setExecutable()
    }
  }

  override protected def populateValues(node: PopulateNode, context: SqlContext): Unit = {
    entityHandler.getOrElse(throw new UnsupportedOperationException).populateValues(context)
  }

  protected def prepareOptimisticLock(): Unit = entityHandler.foreach(_.prepareOptimisticLock())

  override def incrementVersion(): Unit = entityHandler.foreach(_.incrementVersion())

  protected def hasTargetPropertyTypes: Boolean = targetPropertyTypes != null && !targetPropertyTypes.isEmpty

  def getEntity: E = entityHandler.map(_.entity).getOrElse(null.asInstanceOf[E])

  override def complete(): Unit = {
    entityHandler.foreach(_.postUpdate())
  }

  protected class EntityHandler(name: String, var entity: E, entityType: EntityType[E]) {
    assertNotNull(name, entity, entityType)
    private val versionPropertyType = entityType.getVersionPropertyType
    protected var targetPropertyTypes: java.util.List[EntityPropertyType[E, _]] = _
    protected var helper: UpdateQueryHelper[E] = _

    import org.seasar.doma.jdbc.query.UpdateQueryHelper

    def init(): Unit = {
      helper = new UpdateQueryHelper[E](config, entityType, includedPropertyNames, excludedPropertyNames, nullExcluded, versionIgnored, optimisticLockExceptionSuppressed, false)
    }

    def preUpdate(): Unit = {
      val context = new SqlPreUpdateContext(entityType, method, config)
      entityType.preUpdate(entity, context)
      if (context.getNewEntity != null) {
        entity = context.getNewEntity
        addParameter(name, entityType.getEntityClass, entity)
      }
    }

    def prepareTargetPropertyTypes(): Unit = {
      targetPropertyTypes = helper.getTargetPropertyTypes(entity)
    }

    def hasTargetPropertyTypes: Boolean = targetPropertyTypes != null && !targetPropertyTypes.isEmpty

    def postUpdate(): Unit = {
      val context = new SqlPostUpdateContext(entityType, method, config)
      entityType.postUpdate(entity, context)
      if (context.getNewEntity != null) entity = context.getNewEntity
      entityType.saveCurrentStates(entity)
    }

    def prepareOptimisticLock(): Unit = {
      if (versionPropertyType != null && !versionIgnored) if (!optimisticLockExceptionSuppressed) optimisticLockCheckRequired = true
    }

    def incrementVersion(): Unit = {
      if (versionPropertyType != null && !versionIgnored) entity = versionPropertyType.increment(entityType, entity)
    }

    def populateValues(context: SqlContext): Unit = {
      helper.populateValues(entity, targetPropertyTypes, versionPropertyType, context)
    }
  }

  protected class SqlPreUpdateContext(entityType: EntityType[E], method: Method, config: Config) extends AbstractPreUpdateContext[E](entityType, method, config) {
    override def isEntityChanged: Boolean = true
    override def isPropertyChanged(propertyName: String): Boolean = {
      validatePropertyDefined(propertyName)
      true
    }
  }

  protected class SqlPostUpdateContext(entityType: EntityType[E], method: Method, config: Config) extends AbstractPostUpdateContext[E](entityType, method, config) {
    override def isPropertyChanged(propertyName: String): Boolean = {
      validatePropertyDefined(propertyName)
      true
    }
  }

}