package domala.jdbc.query

import domala.jdbc.SqlNodeRepository
import org.seasar.doma.internal.util.AssertionUtil.assertNotNull
import org.seasar.doma.jdbc.SqlKind
import org.seasar.doma.jdbc.query.DeleteQuery

class SqlAnnotationDeleteQuery[E](
  sqlString: String,
  versionIgnored: Boolean = false,
  optimisticLockExceptionSuppressed: Boolean = false
)(entityAndEntityDesc: Option[EntityAndEntityDesc[E]] = None)(implicit sqlNodeRepository: SqlNodeRepository)
  extends SqlAnnotationModifyQuery(SqlKind.UPDATE, sqlString)(sqlNodeRepository) with DeleteQuery {

  val entityHandler: Option[EntityHandler[E]] = entityAndEntityDesc.map(e => new this.EntityHandler(e.name, e.entity, e.entityDesc, versionIgnored, optimisticLockExceptionSuppressed))

  override def prepare(): Unit = {
    super.prepare()
    assertNotNull(method, sqlString)
    preDelete()
    prepareOptions()
    prepareOptimisticLock()
    prepareExecutable()
    prepareSql()
  }

  protected def preDelete(): Unit = {
    entityHandler.foreach(_.preDelete())
  }

  protected def prepareOptimisticLock(): Unit = entityHandler.foreach(_.prepareOptimisticLock())

  protected def prepareExecutable(): Unit = {
    setExecutable()
  }

  def getEntity: E = entityHandler.map(_.entity).getOrElse(null.asInstanceOf[E])

  override def complete(): Unit = {
    entityHandler.foreach(_.postDelete())
  }

}
