package domala.jdbc.query

import domala.jdbc.SqlNodeRepository
import org.seasar.doma.internal.jdbc.sql.SqlContext
import org.seasar.doma.internal.jdbc.sql.node.PopulateNode
import org.seasar.doma.internal.util.AssertionUtil.assertNotNull
import org.seasar.doma.jdbc.SqlKind
import org.seasar.doma.jdbc.query.UpdateQuery


class SqlAnnotationUpdateQuery[E](
  sqlString: String,
  nullExcluded: Boolean = false,
  versionIgnored: Boolean = false,
  optimisticLockExceptionSuppressed: Boolean = false,
  includedPropertyNames: Array[String] = new Array[String](0),
  excludedPropertyNames: Array[String] = new Array[String](0)
)(entityAndEntityDesc: Option[EntityAndEntityDesc[E]] = None)(implicit sqlNodeRepository: SqlNodeRepository)
  extends SqlAnnotationModifyQuery(SqlKind.UPDATE, sqlString)(sqlNodeRepository) with UpdateQuery {

  val entityHandler: Option[UpdateEntityHandler[E]] =
    entityAndEntityDesc.map { e =>
      new this.UpdateEntityHandler(
        e.name,
        e.entity,
        e.entityDesc,
        nullExcluded,
        versionIgnored,
        optimisticLockExceptionSuppressed)
    }

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

  def getEntity: E = entityHandler.map(_.entity).getOrElse(null.asInstanceOf[E])

  override def complete(): Unit = {
    entityHandler.foreach(_.postUpdate())
  }

}
