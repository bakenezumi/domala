package domala.jdbc.query

import java.lang.reflect.Method

import org.seasar.doma.internal.expr.ExpressionEvaluator
import org.seasar.doma.internal.jdbc.entity.{AbstractPostUpdateContext, AbstractPreUpdateContext}
import org.seasar.doma.internal.jdbc.sql.node.PopulateNode
import org.seasar.doma.internal.jdbc.sql.{NodePreparedSqlBuilder, SqlContext, SqlParser}
import org.seasar.doma.internal.util.AssertionUtil.assertNotNull
import org.seasar.doma.jdbc.Config
import org.seasar.doma.jdbc.entity.{EntityPropertyType, EntityType}
import org.seasar.doma.jdbc.query.UpdateQueryHelper
import org.seasar.doma.jdbc.SqlExecutionSkipCause


class SqlUpdateQuery[E](
  sqlString: String,
  nullExcluded: Boolean = false,
  versionIgnored: Boolean = false,
  optimisticLockExceptionSuppressed: Boolean = false,
  includedPropertyNames: Array[String] = new Array[String](0),
  excludedPropertyNames: Array[String] = new Array[String](0))(
  entityAndEntityType: Option[EntityAndEntityType[E]] = None) extends org.seasar.doma.jdbc.query.SqlUpdateQuery {
  // TODO: キャッシュ
  setSqlNode(new SqlParser(sqlString).parse())

  var targetPropertyTypes: java.util.List[EntityPropertyType[E, _]] = _
  val entityHandler: Option[EntityHandler] = entityAndEntityType.map(e => new this.EntityHandler(e.name, e.entity, e.entityType))
  var executable = false
  var sqlExecutionSkipCause = SqlExecutionSkipCause.STATE_UNCHANGED

  override def prepare(): Unit = {
    assertNotNull(method, sqlString)
    initEntityHandler()
    prepareTargetPropertyTypes()
    super.prepare()
    preUpdate()
    prepareOptimisticLock()
    prepareOptions()
    prepareExecutable()
    prepareSql()
  }

  protected def initEntityHandler(): Unit = entityHandler.foreach(_.init())

  protected def preUpdate(): Unit = entityHandler.foreach(_.preUpdate())

  protected def prepareTargetPropertyTypes(): Unit = entityHandler.foreach(_.prepareTargetPropertyTypes())

  protected def prepareExecutable(): Unit = {
    if (entityHandler.isEmpty || entityHandler.get.hasTargetPropertyTypes) {
      executable = true
      sqlExecutionSkipCause = null
    }
  }

  protected def populateValues(node: PopulateNode, context: SqlContext): Unit = {
    entityHandler.getOrElse(throw new UnsupportedOperationException).populateValues(context)
  }

  protected def prepareOptimisticLock(): Unit = entityHandler.foreach(_.prepareOptimisticLock())

  override def incrementVersion(): Unit = entityHandler.foreach(_.incrementVersion())

  protected def hasTargetPropertyTypes: Boolean = targetPropertyTypes != null && !targetPropertyTypes.isEmpty

  def getEntity: E = entityHandler.map(_.entity).getOrElse(null.asInstanceOf[E])

  override def complete(): Unit = {
    entityHandler.foreach(_.postUpdate())
  }

  override def isExecutable: Boolean = executable

  override def getSqlExecutionSkipCause: SqlExecutionSkipCause = sqlExecutionSkipCause

  import org.seasar.doma.internal.jdbc.sql.node.ExpandNode

  protected def expandColumns(node: ExpandNode) = throw new UnsupportedOperationException

  override protected def prepareSql(): Unit = {
    val evaluator = new ExpressionEvaluator(this.parameters, this.config.getDialect.getExpressionFunctions, this.config.getClassHelper)
    val sqlBuilder = new NodePreparedSqlBuilder(this.config, this.kind, null.asInstanceOf[String], evaluator, this.sqlLogType, this.expandColumns _, this.populateValues _)
    this.sql = sqlBuilder.build(this.sqlNode, this.comment _)
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

    import org.seasar.doma.internal.jdbc.sql.SqlContext

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