package domala.jdbc.query

import domala.internal.expr.ExpressionEvaluator
import domala.internal.jdbc.sql.NodePreparedSqlBuilder

class SqlUpdateQuery  extends org.seasar.doma.jdbc.query.SqlUpdateQuery {
  override protected def prepareSql(): Unit = {
    val evaluator = new ExpressionEvaluator(parameters, config.getDialect.getExpressionFunctions, config.getClassHelper)
    val sqlBuilder = new NodePreparedSqlBuilder(config, kind, evaluator, sqlLogType)
    sql = sqlBuilder.build(sqlNode, this.comment _)
  }

}