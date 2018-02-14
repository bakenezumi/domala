package domala.internal.jdbc.dao

import java.sql.Connection
import javax.sql.DataSource

import org.seasar.doma.DomaNullPointerException

object AbstractDaoHelper {
  def toDataSource(connection: Connection): DataSource = {
    if (connection == null) throw new DomaNullPointerException("connection")
    connection match {
      case closedConnection: NeverClosedConnection => new NeverClosedConnectionProvider(closedConnection)
      case _ => new NeverClosedConnectionProvider(new NeverClosedConnection(connection))
    }
  }

}
