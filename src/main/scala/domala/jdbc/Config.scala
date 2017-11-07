package domala.jdbc

import java.lang.reflect.Method
import javax.sql.DataSource

import domala.jdbc.command.ScriptCommand
import domala.jdbc.query.SqlAnnotationScriptQuery
import org.seasar.doma.jdbc._
import org.seasar.doma.jdbc.dialect.Dialect
import org.seasar.doma.jdbc.query.ScriptQuery
import org.seasar.doma.jdbc.tx.{LocalTransactionDataSource, LocalTransactionManager, TransactionManager}

/** A runtime configuration for DAOs.
  *
  * The implementation must be thread safe.
  *
  * @param dataSource the data source
  * @param dialect the SQL dialect
  * @param jdbcLogger the JDBC logger. defaults to
  *  [[org.seasar.doma.jdbc.ConfigSupport ConfigSupport#defaultJdbcLogger]]
  * @param requiresNewController  the transaction controller whose transaction
  *  attribute is `REQUIRES_NEW`. defaults to
  *  [[org.seasar.doma.jdbc.ConfigSupport ConfigSupport#defaultRequiresNewController]]
  * @param classHelper the class helper. defaults to
  *  [[org.seasar.doma.jdbc.ConfigSupport ConfigSupport#defaultClassHelper]]
  * @param queryImplementors the factory for [[org.seasar.doma.jdbc.query.Query Query]]
  *  implementation classes. defaults to
  *  [[org.seasar.doma.jdbc.ConfigSupport ConfigSupport#defaultQueryImplementors]]
  * @param exceptionSqlLogType the SQL log type that determines the SQL log format in
  *  exceptions. defaults to [[org.seasar.doma.jdbc.SqlLogType SqlLogType#FORMATTED]]
  * @param unknownColumnHandler the unknown column handler. defaults to
  *  [[org.seasar.doma.jdbc.ConfigSupport ConfigSupport#defaultUnknownColumnHandler]]
  * @param naming the naming convention controller. defaults to
  *  [[org.seasar.doma.jdbc.Naming Naming#NONE]]
  * @param mapKeyNaming a naming convention controller for keys contained in a
  *  `Map[String, AnyRef]` object. defaults to
  *  [[org.seasar.doma.jdbc.ConfigSupport ConfigSupport#defaultMapKeyNaming]]
  * @param commenter the commenter for SQL strings. defaults to
  *  [[org.seasar.doma.jdbc.ConfigSupport ConfigSupport#defaultCommenter]]
  * @param maxRows the maximum number of rows for a `ResultSet` object.
  *  defaults to 0
  * @param fetchSize the fetch size. defaults to 0
  * @param queryTimeout Returns the query timeout limit in seconds.
  *  If the value is greater than or equal to 1, it is passed to
  * [[java.sql.Statement Statement]]. defaults to 0
  * @param batchSize the query timeout limit in seconds.
  *  If the value is less than 1, it is regarded as 1.
  *  defaults to 0
  * @param  entityListenerProvider the provider for
  *  [[org.seasar.doma.jdbc.entity.EntityListener EntityListener]].
  *  defaults to [[org.seasar.doma.jdbc.ConfigSupport ConfigSupport#defaultCommenter]]
  */
abstract class Config(
  dataSource: DataSource,
  dialect: Dialect,
  jdbcLogger: JdbcLogger =  ConfigSupport.defaultJdbcLogger,
  requiresNewController: RequiresNewController = ConfigSupport.defaultRequiresNewController,
  classHelper: ClassHelper = ConfigSupport.defaultClassHelper,
  queryImplementors: QueryImplementors = ConfigSupport.defaultQueryImplementors,
  exceptionSqlLogType: SqlLogType = SqlLogType.FORMATTED,
  unknownColumnHandler: UnknownColumnHandler = ConfigSupport.defaultUnknownColumnHandler,
  naming: Naming = Naming.NONE,
  mapKeyNaming: MapKeyNaming = ConfigSupport.defaultMapKeyNaming,
  commenter: Commenter = ConfigSupport.defaultCommenter,
  maxRows: Int = 0,
  fetchSize: Int = 0,
  queryTimeout: Int = 0,
  batchSize: Int = 0,
  entityListenerProvider: EntityListenerProvider = ConfigSupport.defaultEntityListenerProvider,
  sqlNodeRepository: SqlNodeRepository = GreedyCacheSqlFileRepository
) extends org.seasar.doma.jdbc.Config {

  getSqlFileRepository.clearCache()
  getSqlNodeRepository.clearCache()

  private val ds: LocalTransactionDataSource = dataSource match {
    case ds: LocalTransactionDataSource => ds
    case _ => new LocalTransactionDataSource(dataSource)
  }
  private val transactionManager = new LocalTransactionManager(ds.getLocalTransaction(getJdbcLogger))

  override def getDataSource: DataSource = ds
  override def getDialect: Dialect = dialect
  override def getNaming: Naming = naming
  override def getJdbcLogger: JdbcLogger = jdbcLogger
  override def getRequiresNewController: RequiresNewController = requiresNewController
  override def getClassHelper: ClassHelper = classHelper
  override def getCommandImplementors: org.seasar.doma.jdbc.CommandImplementors = new CommandImplementors()
  override def getQueryImplementors: QueryImplementors = queryImplementors
  override def getExceptionSqlLogType: SqlLogType = exceptionSqlLogType
  override def getUnknownColumnHandler: UnknownColumnHandler = unknownColumnHandler
  override def getMapKeyNaming: MapKeyNaming = mapKeyNaming
  override def getTransactionManager: TransactionManager = transactionManager
  override def getCommenter: Commenter = commenter
  override def getMaxRows: Int = maxRows
  override def getFetchSize: Int = fetchSize
  override def getQueryTimeout: Int = queryTimeout
  override def getBatchSize: Int = batchSize
  override def getEntityListenerProvider: EntityListenerProvider = entityListenerProvider
  def getSqlNodeRepository: SqlNodeRepository = sqlNodeRepository
}

/**
  * A factory for the [[org.seasar.doma.jdbc.command.Command]] implementation classes.
  */
class CommandImplementors extends org.seasar.doma.jdbc.CommandImplementors {
  override def createScriptCommand(method: Method, query: ScriptQuery): org.seasar.doma.jdbc.command.ScriptCommand = query match {
    case q: SqlAnnotationScriptQuery => new ScriptCommand(q)
    case _ => super.createScriptCommand(method, query)
  }
}
