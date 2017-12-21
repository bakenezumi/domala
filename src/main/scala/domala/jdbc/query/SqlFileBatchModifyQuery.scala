package domala.jdbc.query

import java.util.Collections

import domala.internal.expr.ExpressionEvaluator
import domala.internal.jdbc.sql.NodePreparedSqlBuilder
import org.seasar.doma.internal.expr.Value
import org.seasar.doma.jdbc._
import org.seasar.doma.jdbc.query.BatchModifyQuery

abstract class SqlFileBatchModifyQuery[ELEMENT](
  elementClass: Class[ELEMENT],
  kind: SqlKind,
  sqlFilePath: String)
  extends AbstractSqlBatchModifyQuery(elementClass, kind) with BatchModifyQuery {

  protected var sqlFile: SqlFile = _

  protected def prepareSql(): Unit = {
    sqlFile = config.getSqlFileRepository.getSqlFile(method, sqlFilePath, config.getDialect)
    val value = new Value(elementClass, currentEntity)
    val evaluator = new ExpressionEvaluator(Collections.singletonMap(parameterName, value), this.config.getDialect.getExpressionFunctions, this.config.getClassHelper)
    val sqlBuilder = new NodePreparedSqlBuilder(this.config, this.kind, evaluator, this.sqlLogType, this.expandColumns _, this.populateValues _, sqlFilePath)
    val sql = sqlBuilder.build(this.sqlFile.getSqlNode, this.comment _)
    sqls.add(sql)
  }

}
