package domala.tests.expr

import domala.jdbc.{Config, LocalTransactionConfig}
import org.seasar.doma.jdbc.Naming
import org.seasar.doma.jdbc.dialect.H2Dialect
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource

//noinspection SpellCheckingInspection
object AsyncFunctionTestConfig1 extends LocalTransactionConfig(
  dataSource =  new LocalTransactionDataSource(
    "jdbc:h2:mem:asyncfnctest1;DB_CLOSE_DELAY=-1", "sa", null),
    //"jdbc:h2:mem:asyncfnctest;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=4", "sa", null),
  dialect = new H2Dialect,
  naming = Naming.SNAKE_LOWER_CASE,
) {
  Class.forName("org.h2.Driver")
}

object AsyncFunctionTestConfig2 extends LocalTransactionConfig(
  dataSource =  new LocalTransactionDataSource(
    "jdbc:h2:mem:asyncfnctest2;DB_CLOSE_DELAY=-1", "sa", null),
    //"jdbc:h2:mem:asyncfnctest2;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=4", "sa", null),
  dialect = new H2Dialect,
  naming = Naming.SNAKE_LOWER_CASE,
) {
  Class.forName("org.h2.Driver")
}

object AsyncFunctionTestConfig3 extends LocalTransactionConfig(
  dataSource =  new LocalTransactionDataSource(
  "jdbc:h2:mem:asyncfnctest3;DB_CLOSE_DELAY=-1", "sa", null),
  //"jdbc:h2:mem:asyncfnctest3;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=4", "sa", null),
  dialect = new H2Dialect,
  naming = Naming.SNAKE_LOWER_CASE,
) {
  Class.forName("org.h2.Driver")
}

object AsyncFunctionTestConfig4 extends LocalTransactionConfig(
  dataSource =  new LocalTransactionDataSource(
    "jdbc:h2:mem:asyncfnctest4;DB_CLOSE_DELAY=-1", "sa", null),
  //"jdbc:h2:mem:asyncfnctest4;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=4", "sa", null),
  dialect = new H2Dialect,
  naming = Naming.SNAKE_LOWER_CASE,
) {
  Class.forName("org.h2.Driver")
}