package domala.jdbc

import java.sql.Connection

import org.seasar.doma

package object tx {
  // Alias of Doma type
  type KeepAliveLocalTransaction = doma.jdbc.tx.KeepAliveLocalTransaction
  type LocalTransaction = doma.jdbc.tx.LocalTransaction
  type LocalTransactionContext = doma.jdbc.tx.LocalTransactionContext
  type LocalTransactionDataSource  = doma.jdbc.tx.LocalTransactionDataSource
  type LocalTransactionManager = doma.jdbc.tx.LocalTransactionManager
  type TransactionAttribute = doma.jdbc.tx.TransactionAttribute
  object TransactionAttribute {
    val REQURED = doma.jdbc.tx.TransactionAttribute.REQURED
    val REQURES_NEW = doma.jdbc.tx.TransactionAttribute.REQURES_NEW
    val NOT_SUPPORTED = doma.jdbc.tx.TransactionAttribute.NOT_SUPPORTED
  }
  type TransactionIsolationLevel = doma.jdbc.tx.TransactionIsolationLevel
  object TransactionIsolationLevel {
    val READ_UNCOMMITTED = doma.jdbc.tx.TransactionIsolationLevel.READ_UNCOMMITTED
    val READ_COMMITTED = doma.jdbc.tx.TransactionIsolationLevel.READ_COMMITTED
    val REPEATABLE_READ = doma.jdbc.tx.TransactionIsolationLevel.REPEATABLE_READ
    val SERIALIZABLE = doma.jdbc.tx.TransactionIsolationLevel.SERIALIZABLE
    val DEFAULT = doma.jdbc.tx.TransactionIsolationLevel.DEFAULT
  }
  type TransactionManager = doma.jdbc.tx.TransactionManager

}
