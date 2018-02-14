package domala.async.jdbc

import javax.sql.DataSource

import domala.jdbc.dialect.Dialect
import domala.jdbc.{LocalTransactionConfig, Naming}

import scala.concurrent.Future



/** A basic implementation of [[domala.async.jdbc.AsyncConfig AsyncConfig]] for local transactions.
  *
  * future created from this is an independent transaction.
  *
  * The implementation must be thread safe.
  *
  * @param dataSource the data source
  * @param dialect the SQL dialect
  * @param naming the naming convention controller. defaults to
  *  [[org.seasar.doma.jdbc.Naming Naming#NONE]]
  */
abstract class AsyncLocalTransactionConfig(
  dataSource: DataSource,
  dialect: Dialect,
  naming: Naming = Naming.NONE) extends LocalTransactionConfig(dataSource, dialect, naming) with AsyncConfig {

  private[domala] def future[T](block: => T): Future[T] = {
    Future {
      getTransactionManager.requiresNew(() => block)
    }(executionContext)
  }

}
