package domala.jdbc

import java.lang.reflect.Method
import javax.sql.DataSource

import domala.jdbc.command.ScriptCommand
import domala.jdbc.query.SqlAnnotationScriptQuery
import org.seasar.doma.jdbc.Naming
import org.seasar.doma.jdbc.dialect.Dialect
import org.seasar.doma.jdbc.query.ScriptQuery
import org.seasar.doma.jdbc.tx.{LocalTransactionDataSource, LocalTransactionManager, TransactionManager}

class Config(dataSource: DataSource, dialect: Dialect, naming: Naming = Naming.NONE) extends org.seasar.doma.jdbc.Config {

  getSqlFileRepository.clearCache

  val ds = dataSource match {
    case ltds: LocalTransactionDataSource => ltds
    case _ => new LocalTransactionDataSource(dataSource)
  }
  val transactionManager = new LocalTransactionManager(ds.getLocalTransaction(getJdbcLogger))

  override def getDataSource: DataSource = ds
  override def getDialect: Dialect = dialect
  override def getTransactionManager: TransactionManager = transactionManager
  override def getNaming: Naming = naming

  override def getCommandImplementors: org.seasar.doma.jdbc.CommandImplementors = new CommandImplementors()
}

class CommandImplementors extends org.seasar.doma.jdbc.CommandImplementors {
  override def createScriptCommand(method: Method, query: ScriptQuery): org.seasar.doma.jdbc.command.ScriptCommand = {
    query match {
      case q: SqlAnnotationScriptQuery => new ScriptCommand(q)
      case _ => super.createScriptCommand(method, query)
    }
  }
}
