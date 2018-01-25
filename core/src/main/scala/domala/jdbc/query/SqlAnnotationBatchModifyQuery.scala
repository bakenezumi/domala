package domala.jdbc.query

import java.util.Collections

import domala.internal.expr.ExpressionEvaluator
import domala.internal.jdbc.sql.NodePreparedSqlBuilder
import domala.jdbc.SqlNodeRepository
import org.seasar.doma.internal.expr.Value
import org.seasar.doma.jdbc._
import org.seasar.doma.jdbc.query.BatchModifyQuery

abstract class SqlAnnotationBatchModifyQuery[ELEMENT](
  elementClass: Class[ELEMENT],
  kind: SqlKind,
  sqlString: String)(sqlNodeRepository: SqlNodeRepository)
  extends AbstractSqlBatchModifyQuery(elementClass, kind) with BatchModifyQuery {

  protected val sqlNode: SqlNode = sqlNodeRepository.get(sqlString)

  protected def prepareSql(): Unit = {
    val value = new Value(elementClass, currentEntity)
    val evaluator = new ExpressionEvaluator(Collections.singletonMap(parameterName, value), this.config.getDialect.getExpressionFunctions, this.config.getClassHelper)
    val sqlBuilder = new NodePreparedSqlBuilder(this.config, this.kind, evaluator, this.sqlLogType, this.expandColumns _, this.populateValues _, null)
    val sql = sqlBuilder.build(this.sqlNode, this.comment _)
    sqls.add(sql)
  }

}
