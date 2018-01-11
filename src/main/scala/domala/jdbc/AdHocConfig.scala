package domala.jdbc

import java.util.logging.Logger
import javax.sql.DataSource

import domala.jdbc.dialect.{Dialect, StandardDialect}
import domala.message.Message
import org.seasar.doma.DomaNullPointerException
import org.seasar.doma.jdbc.SimpleDataSource
import org.seasar.doma.jdbc.tx.TransactionManager

/** An ad-hoc config for REPL
  *
  * When this Config is used, it operates in AutoCommit mode.
  * Use only at ad-hoc confirmation in REPL.
  */
class AdHocConfig private (
  url: String,
  user: String,
  password: String,
  dialect: Dialect,
  naming: Naming
) extends Config {
  private[this] val logger = Logger.getLogger(AdHocConfig.getClass.getName)
  logger.warning(Message.DOMALA6023.getMessage())

  if (url == null) throw new DomaNullPointerException("url")
  private[this] val simpleDataSource = new SimpleDataSource
  simpleDataSource.setUrl(url)
  if (user != null) simpleDataSource.setUser(user)
  if (password != null) simpleDataSource.setPassword(password)
  private[this] val dataSource = simpleDataSource

  override def getDataSource: DataSource = dataSource
  override def getDialect: Dialect = dialect
  override def getNaming: Naming = naming
  override def getTransactionManager: TransactionManager = throw new UnsupportedOperationException(Message.DOMALA6022.getMessage())
}

object AdHocConfig {
  def apply(url: String,
    user: String = "",
    password: String = "",
    dialect: Dialect = new StandardDialect,
    naming: Naming = Naming.NONE) = new AdHocConfig(url, user, password, dialect, naming)
}
