package domala.jdbc.query;

import domala.internal.expr.ExpressionEvaluator;
import org.seasar.doma.internal.jdbc.sql.node.ExpandNode;
import org.seasar.doma.jdbc.PreparedSql;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SqlSelectQuery extends org.seasar.doma.jdbc.query.SqlSelectQuery {
    @Override
    protected void buildSql(BiFunction<org.seasar.doma.internal.expr.ExpressionEvaluator, Function<ExpandNode, List<String>>, PreparedSql> sqlBuilder) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(this.parameters, this.config.getDialect().getExpressionFunctions(), this.config.getClassHelper());
        this.sql = sqlBuilder.apply(evaluator, this::expandColumns);
    }
}
