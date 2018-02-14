package domala.internal.jdbc.dao

import java.sql.{Blob, CallableStatement, Clob, Connection, DatabaseMetaData, NClob, PreparedStatement, SQLClientInfoException, SQLException, SQLWarning, SQLXML, Savepoint, Statement, Struct}
import java.util
import java.util.Properties
import java.util.concurrent.Executor

import org.seasar.doma.internal.util.AssertionUtil.{assertNotNull, assertTrue}

class NeverClosedConnection(connection: Connection) extends Connection {

  assertNotNull(connection, "")
  assertTrue(!connection.isInstanceOf[NeverClosedConnection])

  @throws[SQLException]
  override def clearWarnings(): Unit = {
    connection.clearWarnings()
  }

  @throws[SQLException]
  override def close(): Unit = {
    // do nothing.
  }

  @throws[SQLException]
  override def commit(): Unit = {
    connection.commit()
  }

  @throws[SQLException]
  override def createArrayOf(typeName: String, elements: scala.Array[AnyRef]): java.sql.Array = connection.createArrayOf(typeName, elements)

  @throws[SQLException]
  override def createBlob: Blob = connection.createBlob

  @throws[SQLException]
  override def createClob: Clob = connection.createClob

  @throws[SQLException]
  override def createNClob: NClob = connection.createNClob

  @throws[SQLException]
  override def createSQLXML: SQLXML = connection.createSQLXML

  @throws[SQLException]
  override def createStatement: Statement = connection.createStatement

  @throws[SQLException]
  override def createStatement(resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): Statement = connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability)

  @throws[SQLException]
  override def createStatement(resultSetType: Int, resultSetConcurrency: Int): Statement = connection.createStatement(resultSetType, resultSetConcurrency)

  @throws[SQLException]
  override def createStruct(typeName: String, attributes: Array[Object]): Struct = connection.createStruct(typeName, attributes)

  @throws[SQLException]
  override def getAutoCommit: Boolean = connection.getAutoCommit

  @throws[SQLException]
  override def getCatalog: String = connection.getCatalog

  @throws[SQLException]
  override def getClientInfo: Properties = connection.getClientInfo

  @throws[SQLException]
  override def getClientInfo(name: String): String = connection.getClientInfo(name)

  @throws[SQLException]
  override def getHoldability: Int = connection.getHoldability

  @throws[SQLException]
  override def getMetaData: DatabaseMetaData = connection.getMetaData

  @throws[SQLException]
  override def getTransactionIsolation: Int = connection.getTransactionIsolation

  @throws[SQLException]
  override def getTypeMap: util.Map[String, Class[_]] = connection.getTypeMap

  @throws[SQLException]
  override def getWarnings: SQLWarning = connection.getWarnings

  @throws[SQLException]
  override def isClosed: Boolean = connection.isClosed

  @throws[SQLException]
  override def isReadOnly: Boolean = connection.isReadOnly

  @throws[SQLException]
  override def isValid(timeout: Int): Boolean = connection.isValid(timeout)

  @throws[SQLException]
  override def isWrapperFor(iface: Class[_]): Boolean = {
    if (iface == null) return false
    if (iface.isAssignableFrom(getClass)) return true
    connection.isWrapperFor(iface)
  }

  @throws[SQLException]
  override def nativeSQL(sql: String): String = connection.nativeSQL(sql)

  @throws[SQLException]
  override def prepareCall(sql: String, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): CallableStatement = connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability)

  @throws[SQLException]
  override def prepareCall(sql: String, resultSetType: Int, resultSetConcurrency: Int): CallableStatement = connection.prepareCall(sql, resultSetType, resultSetConcurrency)

  @throws[SQLException]
  override def prepareCall(sql: String): CallableStatement = connection.prepareCall(sql)

  @throws[SQLException]
  override def prepareStatement(sql: String, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): PreparedStatement = connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability)

  @throws[SQLException]
  override def prepareStatement(sql: String, resultSetType: Int, resultSetConcurrency: Int): PreparedStatement = connection.prepareStatement(sql, resultSetType, resultSetConcurrency)

  @throws[SQLException]
  override def prepareStatement(sql: String, autoGeneratedKeys: Int): PreparedStatement = connection.prepareStatement(sql, autoGeneratedKeys)

  @throws[SQLException]
  override def prepareStatement(sql: String, columnIndexes: scala.Array[Int]): PreparedStatement = connection.prepareStatement(sql, columnIndexes)

  @throws[SQLException]
  override def prepareStatement(sql: String, columnNames: scala.Array[String]): PreparedStatement = connection.prepareStatement(sql, columnNames)

  @throws[SQLException]
  override def prepareStatement(sql: String): PreparedStatement = connection.prepareStatement(sql)

  @throws[SQLException]
  override def releaseSavepoint(savepoint: Savepoint): Unit = {
    connection.releaseSavepoint(savepoint)
  }

  @throws[SQLException]
  override def rollback(): Unit = {
    connection.rollback()
  }

  @throws[SQLException]
  override def rollback(savepoint: Savepoint): Unit = {
    connection.rollback(savepoint)
  }

  @throws[SQLException]
  override def setAutoCommit(autoCommit: Boolean): Unit = {
    connection.setAutoCommit(autoCommit)
  }

  @throws[SQLException]
  override def setCatalog(catalog: String): Unit = {
    connection.setCatalog(catalog)
  }

  @throws[SQLClientInfoException]
  override def setClientInfo(properties: Properties): Unit = {
    connection.setClientInfo(properties)
  }

  @throws[SQLClientInfoException]
  override def setClientInfo(name: String, value: String): Unit = {
    connection.setClientInfo(name, value)
  }

  @throws[SQLException]
  override def setHoldability(holdability: Int): Unit = {
    connection.setHoldability(holdability)
  }

  @throws[SQLException]
  override def setReadOnly(readOnly: Boolean): Unit = {
    connection.setReadOnly(readOnly)
  }

  @throws[SQLException]
  override def setSavepoint(): Savepoint = connection.setSavepoint()

  @throws[SQLException]
  override def setSavepoint(name: String): Savepoint = connection.setSavepoint(name)

  @throws[SQLException]
  override def setTransactionIsolation(level: Int): Unit = {
    connection.setTransactionIsolation(level)
  }

  @throws[SQLException]
  override def setTypeMap(map: util.Map[String, Class[_]]): Unit = {
    connection.setTypeMap(map)
  }

  @SuppressWarnings(Array("unchecked"))
  @throws[SQLException]
  override def unwrap[T](iface: Class[T]): T = {
    if (iface == null) throw new SQLException("iface must not be null")
    if (iface.isAssignableFrom(getClass)) return this.asInstanceOf[T]
    connection.unwrap(iface)
  }

  @throws[SQLException]
  override def setSchema(schema: String): Unit = {
    connection.setSchema(schema)
  }

  @throws[SQLException]
  override def getSchema: String = connection.getSchema

  @throws[SQLException]
  override def abort(executor: Executor): Unit = {
    connection.abort(executor)
  }

  @throws[SQLException]
  override def setNetworkTimeout(executor: Executor, milliseconds: Int): Unit = {
    connection.setNetworkTimeout(executor, milliseconds)
  }

  @throws[SQLException]
  override def getNetworkTimeout: Int = connection.getNetworkTimeout

}