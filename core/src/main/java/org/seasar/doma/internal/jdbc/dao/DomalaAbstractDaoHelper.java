package org.seasar.doma.internal.jdbc.dao;

import org.seasar.doma.DomaNullPointerException;

import javax.sql.DataSource;
import java.sql.Connection;

// Scalaではコンストラクタオーバーロードが困難であり、
// NeverClosedConnection, NeverClosedConnectionProviderがパッケージプライベートのため
// このパッケージにてフックしている
@SuppressWarnings("unused")
public class DomalaAbstractDaoHelper {
    public static DataSource toDataSource(Connection connection) {
        if (connection == null) {
            throw new DomaNullPointerException("connection");
        }
        return (connection instanceof NeverClosedConnection) ?
            new NeverClosedConnectionProvider(
                    (NeverClosedConnection) connection):
            new NeverClosedConnectionProvider(
                    new NeverClosedConnection(connection));
    }
}
