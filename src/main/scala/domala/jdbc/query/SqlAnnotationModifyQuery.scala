package domala.jdbc.query

import domala.internal.expr.ExpressionEvaluator
import domala.internal.jdbc.sql.NodePreparedSqlBuilder
import domala.jdbc.SqlNodeRepository
import org.seasar.doma.jdbc._

class SqlAnnotationModifyQuery(kind: SqlKind, sqlString: String)
  (sqlNodeRepository: SqlNodeRepository) extends AbstractSqlModifyQuery(kind) {

  protected val sqlNode: SqlNode = sqlNodeRepository.get(sqlString)

  protected def prepareSql(): Unit = {
    val evaluator = new ExpressionEvaluator(this.parameters, this.config.getDialect.getExpressionFunctions, this.config.getClassHelper)
    val sqlBuilder = new NodePreparedSqlBuilder(this.config, this.kind, evaluator, this.sqlLogType, this.expandColumns _, this.populateValues _, null)
    this.sql = sqlBuilder.build(this.sqlNode, this.comment _)
  }

}
