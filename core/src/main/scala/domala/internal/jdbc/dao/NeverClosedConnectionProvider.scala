package domala.internal.jdbc.dao

import java.io.PrintWriter
import java.sql.{Connection, SQLException, SQLFeatureNotSupportedException}
import javax.sql.DataSource

import org.seasar.doma.internal.util.AssertionUtil.assertNotNull

private[dao] class NeverClosedConnectionProvider(connection: NeverClosedConnection) extends DataSource {
  assertNotNull(connection, "")

  @throws[SQLException]
  override def getConnection: Connection = connection

  @throws[SQLException]
  override def getConnection(username: String, password: String): Connection = connection

  @throws[SQLException]
  override def getLoginTimeout = 0

  @throws[SQLException]
  override def getLogWriter: PrintWriter = null

  @throws[SQLException]
  override def setLoginTimeout(seconds: Int): Unit = ()

  @throws[SQLException]
  override def setLogWriter(out: PrintWriter): Unit = ()

  @throws[SQLException]
  override def isWrapperFor(iface: Class[_]): Boolean = iface != null && iface.isAssignableFrom(getClass)

  @SuppressWarnings(Array("unchecked"))
  @throws[SQLException]
  override def unwrap[T](iface: Class[T]): T = {
    if (iface == null) throw new SQLException("iface must not be null")
    if (iface.isAssignableFrom(getClass)) return this.asInstanceOf[T]
    throw new SQLException("cannot unwrap to " + iface.getName)
  }

  @throws[SQLFeatureNotSupportedException]
  override def getParentLogger = throw new SQLFeatureNotSupportedException

}
