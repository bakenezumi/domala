package domala

import javax.sql.DataSource

import org.seasar.doma.jdbc.Naming
import org.seasar.doma.jdbc.dialect.Dialect
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource
import org.seasar.doma.jdbc.tx.LocalTransactionManager

class Config(dataSource: DataSource, dialect: Dialect, naming: Naming = Naming.NONE) extends org.seasar.doma.jdbc.Config {

  getSqlFileRepository.clearCache

  val ds = dataSource match {
    case ltds: LocalTransactionDataSource => ltds
    case _ => new LocalTransactionDataSource(dataSource)
  }
  val transactionManager = new LocalTransactionManager(ds.getLocalTransaction(getJdbcLogger))

  override def getDataSource = ds
  override def getDialect = dialect
  override def getTransactionManager = transactionManager
  override def getNaming = naming

}
