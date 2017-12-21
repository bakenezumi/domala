package domala.jdbc.query


import domala.internal.jdbc.sql.NodePreparedSqlBuilder
import org.seasar.doma.internal.util.AssertionUtil.assertNotNull
import org.seasar.doma.jdbc.{SelectOptionsAccessor, SqlKind, SqlNode}

class SqlSelectQuery extends AbstractSelectQuery {

  protected var sqlNode: SqlNode = _

  override def prepare(): Unit = {
    super.prepare()
    assertNotNull(sqlNode, "")
  }

  override def complete(): Unit = {
    if (SelectOptionsAccessor.isCount(options)) executeCount(sqlNode)
  }

  def setSqlNode(sqlNode: SqlNode): Unit = {
    this.sqlNode = sqlNode
  }

  override protected def prepareSql(): Unit = {
    val transformedSqlNode: SqlNode = getConfig.getDialect.transformSelectSqlNode(this.sqlNode, this.options)
    buildSqlDomala((evaluator, expander) => {
      val sqlBuilder = new NodePreparedSqlBuilder(this.config, SqlKind.SELECT, evaluator, this.sqlLogType, expander)
      sqlBuilder.build(transformedSqlNode, this.comment _)
    })
  }

}
