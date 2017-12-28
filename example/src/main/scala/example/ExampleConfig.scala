package example

import domala.jdbc.LocalTransactionConfig
import domala.jdbc.dialect.H2Dialect
import domala.jdbc.tx.LocalTransactionDataSource

object ExampleConfig extends LocalTransactionConfig(
  dataSource =  new LocalTransactionDataSource(
    "jdbc:h2:mem:example;DB_CLOSE_DELAY=-1", "", ""),
  dialect = new H2Dialect
) {
  Class.forName("org.h2.Driver")
}
