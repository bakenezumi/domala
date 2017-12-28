package domala.jdbc

import org.seasar.doma

package object dialect {
  // Alias of Doma type
  type Dialect = doma.jdbc.dialect.Dialect
  type Db2Dialect = doma.jdbc.dialect.Db2Dialect
  type H2Dialect = doma.jdbc.dialect.H2Dialect
  type H212126Dialect = doma.jdbc.dialect.H212126Dialect
  type HsqldbDialect = doma.jdbc.dialect.HsqldbDialect
  type Mssql2008Dialect = doma.jdbc.dialect.Mssql2008Dialect
  type MssqlDialect = doma.jdbc.dialect.MssqlDialect
  type MysqlDialect = doma.jdbc.dialect.MysqlDialect
  type Oracle11Dialect = doma.jdbc.dialect.Oracle11Dialect
  type OracleDialect = doma.jdbc.dialect.OracleDialect
  type PostgresDialect = doma.jdbc.dialect.PostgresDialect
  type SqliteDialect = doma.jdbc.dialect.SqliteDialect
  type StandardDialect = doma.jdbc.dialect.StandardDialect

}
