package domala.jdbc.query

import domala.internal.jdbc.sql.NodePreparedSqlBuilder
import org.seasar.doma.internal.util.AssertionUtil.assertNotNull
import org.seasar.doma.jdbc._

class SqlFileSelectQuery(sqlFilePath: String) extends AbstractSelectQuery {
  protected var sqlFile: SqlFile = _

  override def prepare(): Unit = {
    super.prepare()
    assertNotNull(sqlFilePath, "")
  }

  override protected def prepareSql(): Unit = {
    sqlFile = config.getSqlFileRepository.getSqlFile(method, sqlFilePath, config.getDialect)
    val transformedSqlNode: SqlNode = getConfig.getDialect.transformSelectSqlNode(this.sqlFile.getSqlNode, this.options)
    buildSqlDomala((evaluator, expander) => {
      val sqlBuilder: NodePreparedSqlBuilder = new NodePreparedSqlBuilder(this.config, SqlKind.SELECT, evaluator, this.sqlLogType, expander, sqlFilePath)
      sqlBuilder.build(transformedSqlNode, this.comment _)
    })
  }

  override def complete(): Unit = {
    if (SelectOptionsAccessor.isCount(options)) executeCount(sqlFile.getSqlNode)
  }

}
