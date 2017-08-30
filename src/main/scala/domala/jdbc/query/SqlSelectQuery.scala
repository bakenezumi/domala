package domala.jdbc.query

import org.seasar.doma.internal.jdbc.sql.SqlParser

class SqlSelectQuery(sql: String) extends org.seasar.doma.jdbc.query.SqlSelectQuery {
  // TODO: キャッシュ
  setSqlNode(new SqlParser(sql).parse())
}