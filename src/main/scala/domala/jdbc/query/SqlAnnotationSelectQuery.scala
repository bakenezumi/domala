package domala.jdbc.query

import domala.jdbc.SqlNodeRepository

class SqlAnnotationSelectQuery(sql: String)(implicit sqlNodeRepository: SqlNodeRepository) extends SqlSelectQuery {

  setSqlNode(sqlNodeRepository.get(sql))

}
