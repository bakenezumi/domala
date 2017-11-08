package domala.jdbc.query

import domala.jdbc.SqlNodeRepository

import scala.collection.JavaConverters._

class SqlAnnotationSelectQuery(sql: String)(implicit sqlNodeRepository: SqlNodeRepository) extends SqlSelectQuery {

  setSqlNode(sqlNodeRepository.get(sql))

  override def addParameter(name: String, `type`: Class[_], value: Any): Unit = {
    value match {
      case x: Seq[_] =>
        val converted = x.asJava
        super.addParameter(name, converted.getClass, converted)
      case _ => super.addParameter(name, `type`, value)
    }
  }

}

