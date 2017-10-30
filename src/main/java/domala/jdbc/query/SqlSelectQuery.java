package domala.jdbc.query;

import domala.internal.expr.ExpressionEvaluator;
import domala.internal.jdbc.sql.NodePreparedSqlBuilder;
import org.seasar.doma.internal.jdbc.sql.node.ExpandNode;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.SqlKind;
import org.seasar.doma.jdbc.SqlNode;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

// 独自のExpressionEvaluator, NodePreparedSqlBuilderを利用するために拡張。
// 親のフィールド(sql)を変更する必要があるためJavaで実装。
public class SqlSelectQuery extends org.seasar.doma.jdbc.query.SqlSelectQuery {

    private void buildSqlDomala(BiFunction<ExpressionEvaluator, Function<ExpandNode, List<String>>, PreparedSql> sqlBuilder) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(this.parameters, this.config.getDialect().getExpressionFunctions(), this.config.getClassHelper());
        this.sql = sqlBuilder.apply(evaluator, this::expandColumns);
    }

    @Override
    protected void prepareSql() {
        SqlNode transformedSqlNode = this.config.getDialect().transformSelectSqlNode(this.sqlNode, this.options);
        buildSqlDomala((evaluator, expander) -> {
            NodePreparedSqlBuilder sqlBuilder = new NodePreparedSqlBuilder(this.config, SqlKind.SELECT, evaluator, this.sqlLogType, expander);
            return sqlBuilder.build(transformedSqlNode, this::comment);
        });
    }
}
