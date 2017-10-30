package domala.jdbc

import java.lang.reflect.Method
import java.sql.Statement
import javax.sql.DataSource

import domala.jdbc.command.ScriptCommand
import domala.jdbc.query.SqlAnnotationScriptQuery
import org.seasar.doma.jdbc._
import org.seasar.doma.jdbc.dialect.Dialect
import org.seasar.doma.jdbc.query.{Query, ScriptQuery}
import org.seasar.doma.jdbc.tx.{LocalTransactionDataSource, LocalTransactionManager, TransactionManager}

/** A runtime configuration for DAOs.
  *
  * The implementation must be thread safe.
  *
  * @param dataSource the data source
  * @param dialect the SQL dialect
  * @param jdbcLogger the JDBC logger
  * @param requiresNewController  the transaction controller whose transaction attribute is
  * `REQUIRES_NEW`.
  * @param classHelper the class helper
  * @param queryImplementors the factory for [[Query]] implementation classes
  * @param exceptionSqlLogType the SQL log type that determines the SQL log format in
  * exceptions
  * @param unknownColumnHandler the unknown column handler
  * @param naming the naming convention controller
  * @param mapKeyNaming a naming convention controller for keys contained in a
  * `Map[String, AnyRef]` object.
  * @param commenter the commenter for SQL strings
  * @param maxRows the maximum number of rows for a `ResultSet` object
  * @param fetchSize the fetch size
  * @param queryTimeout Returns the query timeout limit in seconds.
  * If the value is greater than or equal to 1, it is passed to
  * [[Statement]].
  * @param batchSize the query timeout limit in seconds.
  * If the value is less than 1, it is regarded as 1
  * @param  entityListenerProvider the provider for [[org.seasar.doma.jdbc.entity.EntityListener]]
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
  entityListenerProvider: EntityListenerProvider = ConfigSupport.defaultEntityListenerProvider
) extends org.seasar.doma.jdbc.Config {

  getSqlFileRepository.clearCache()

  private val ds: LocalTransactionDataSource = dataSource match {
    case ltds: LocalTransactionDataSource => ltds
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
