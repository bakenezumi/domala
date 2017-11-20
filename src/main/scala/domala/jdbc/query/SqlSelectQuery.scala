package domala.jdbc.query

import java.util
import java.util.function.{BiFunction, Function}

import domala.internal.expr.ExpressionEvaluator
import domala.internal.jdbc.sql.NodePreparedSqlBuilder
import org.seasar.doma.internal.jdbc.sql.node.ExpandNode
import org.seasar.doma.jdbc.{PreparedSql, SqlKind, SqlNode}

class SqlSelectQuery extends org.seasar.doma.jdbc.query.SqlSelectQuery {
  private def buildSqlDomala(sqlBuilder: BiFunction[ExpressionEvaluator, Function[ExpandNode, util.List[String]], PreparedSql]): Unit = {
    val evaluator: ExpressionEvaluator = new ExpressionEvaluator(this.parameters, this.config.getDialect.getExpressionFunctions, this.config.getClassHelper)
    this.sql = sqlBuilder.apply(evaluator, this.expandColumns _)
  }

  override protected def prepareSql(): Unit = {
    val transformedSqlNode: SqlNode = getConfig.getDialect.transformSelectSqlNode(this.sqlNode, this.options)
    buildSqlDomala((evaluator, expander) => {
      val sqlBuilder: NodePreparedSqlBuilder = new NodePreparedSqlBuilder(this.config, SqlKind.SELECT, evaluator, this.sqlLogType, expander)
      sqlBuilder.build(transformedSqlNode, this.comment _)
    })
  }
}
