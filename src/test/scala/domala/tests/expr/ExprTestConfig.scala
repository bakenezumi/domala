package domala.tests.expr

import domala.jdbc.LocalTransactionConfig
import org.seasar.doma.jdbc.Naming
import org.seasar.doma.jdbc.dialect.H2Dialect
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource

//noinspection SpellCheckingInspection
object ExprTestConfig extends LocalTransactionConfig(
  dataSource =  new LocalTransactionDataSource(
    "jdbc:h2:mem:exprtest;DB_CLOSE_DELAY=-1", "sa", null),
  dialect = new H2Dialect,
  naming = Naming.SNAKE_LOWER_CASE
) {
  Class.forName("org.h2.Driver")
}
