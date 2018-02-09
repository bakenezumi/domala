package domala.tests

import domala.jdbc.LocalTransactionConfig
import domala.jdbc.Naming
import domala.jdbc.dialect.H2Dialect
import domala.jdbc.tx.LocalTransactionDataSource

abstract class H2TestConfigTemplate(dbName: String) extends LocalTransactionConfig(
    dataSource =  new LocalTransactionDataSource(
      "jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1", "sa", null),
    dialect = new H2Dialect,
    naming = Naming.SNAKE_LOWER_CASE
  ) {
  Class.forName("org.h2.Driver")
}
