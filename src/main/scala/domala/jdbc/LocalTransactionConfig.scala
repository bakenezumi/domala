package domala.jdbc

import javax.sql.DataSource

import domala.jdbc.dialect.Dialect
import org.seasar.doma.jdbc.tx.{LocalTransactionDataSource, LocalTransactionManager, TransactionManager}

/** A basic implementation of [[domala.jdbc.Config Config]] for local transactions.
  *
  * The implementation must be thread safe.
  *
  * @param dataSource the data source
  * @param dialect the SQL dialect
  * @param naming the naming convention controller. defaults to
  *  [[org.seasar.doma.jdbc.Naming Naming#NONE]]
  */
abstract class LocalTransactionConfig(
  dataSource: DataSource,
  dialect: Dialect,
  naming: Naming = Naming.NONE
) extends Config {

  private val ds: LocalTransactionDataSource = dataSource match {
    case ds: LocalTransactionDataSource => ds
    case _ => new LocalTransactionDataSource(dataSource)
  }
  private val transactionManager = new LocalTransactionManager(ds.getLocalTransaction(getJdbcLogger))

  override def getDataSource: DataSource = ds
  override def getDialect: Dialect = dialect
  override def getTransactionManager: TransactionManager = transactionManager
  override def getNaming: Naming = naming
}
