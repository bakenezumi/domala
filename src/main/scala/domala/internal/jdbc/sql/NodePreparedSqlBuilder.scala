package domala.internal.jdbc.sql

import org.seasar.doma.internal.jdbc.sql.SqlContext
import org.seasar.doma.internal.jdbc.sql.node.{ExpandNode, PopulateNode, SqlLocation}
import org.seasar.doma.jdbc._
import java.util.function.{BiConsumer, Function, Supplier}

import domala.internal.expr.ExpressionEvaluator
import domala.internal.jdbc.scalar.Scalars
import org.seasar.doma.internal.jdbc.scalar.{Scalar, ScalarException}
import org.seasar.doma.message.Message

//独自のExpressionEvaluator, Scalarsを利用するため拡張
class NodePreparedSqlBuilder(
    config: Config,
    kind: SqlKind,
    evaluator: ExpressionEvaluator,
    sqlLogType: SqlLogType = SqlLogType.FORMATTED,
    columnsExpander: Function[ExpandNode, java.util.List[String]] =
      (_: ExpandNode) => throw new UnsupportedOperationException,
    valuesPopulater: BiConsumer[PopulateNode, SqlContext] =
      (_: PopulateNode, _: SqlContext) => {
        throw new UnsupportedOperationException
      })
    extends org.seasar.doma.internal.jdbc.sql.NodePreparedSqlBuilder(
      config,
      kind,
      null,
      evaluator,
      sqlLogType,
      columnsExpander,
      valuesPopulater) {

  def this(config: Config, kind: SqlKind) {
    this(
      config,
      kind,
      new ExpressionEvaluator(expressionFunctions =
                                config.getDialect.getExpressionFunctions,
                              classHelper = config.getClassHelper),
      SqlLogType.FORMATTED
    )
  }

  def this(config: Config, kind: SqlKind, evaluator: ExpressionEvaluator) {
    this(
      config,
      kind,
      evaluator,
      SqlLogType.FORMATTED
    )
  }

  def this(config: Config, kind: SqlKind, evaluator: ExpressionEvaluator, sqlLogType: SqlLogType, columnsExpander: Function[ExpandNode, java.util.List[String]]) {
    this(
      config,
      kind,
      evaluator,
      sqlLogType,
      columnsExpander,
      (_: PopulateNode, _: SqlContext) => {
        throw new UnsupportedOperationException
      }
    )
  }

  override protected def wrap(location: SqlLocation, bindVariableText: String, value: Object, valueClass: Class[_]): Supplier[Scalar[_, _]] = try
    Scalars.wrap(value, valueClass, optional = false, this.config.getClassHelper)
  catch {
    case e: ScalarException =>
      throw new JdbcException(Message.DOMA2118, e, Array[AnyRef](location.getSql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition), bindVariableText, e))
  }

}
