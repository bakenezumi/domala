package domala.jdbc.query

import domala.internal.expr.ExpressionEvaluator
import domala.internal.jdbc.sql.NodePreparedSqlBuilder
import domala.jdbc.SqlNodeRepository
import org.seasar.doma.internal.expr.Value
import org.seasar.doma.internal.jdbc.sql.SqlContext
import org.seasar.doma.internal.jdbc.sql.node.{ExpandNode, PopulateNode}
import org.seasar.doma.internal.util.AssertionUtil
import org.seasar.doma.jdbc._
import org.seasar.doma.jdbc.query.{AbstractQuery, ModifyQuery}

class SqlFileModifyQuery(kind: SqlKind, sqlFilePath: String) extends AbstractSqlModifyQuery(kind)  {

  protected var sqlFile: SqlFile = _

  protected def prepareSql(): Unit = {
    sqlFile = config.getSqlFileRepository.getSqlFile(method, sqlFilePath, config.getDialect)
    val evaluator = new ExpressionEvaluator(this.parameters, this.config.getDialect.getExpressionFunctions, this.config.getClassHelper)
    val sqlBuilder = new NodePreparedSqlBuilder(this.config, this.kind, evaluator, this.sqlLogType, this.expandColumns _, this.populateValues _, sqlFilePath)
    this.sql = sqlBuilder.build(this.sqlFile.getSqlNode, this.comment _)
  }

}
