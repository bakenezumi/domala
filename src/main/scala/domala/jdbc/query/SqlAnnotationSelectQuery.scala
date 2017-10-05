package domala.jdbc.query

import org.seasar.doma.internal.jdbc.sql.SqlParser

import scala.collection.JavaConverters._

class SqlAnnotationSelectQuery(sql: String) extends SqlSelectQuery {
  // TODO: キャッシュ
  setSqlNode(new SqlParser(sql).parse())

  override def addParameter(name: String, `type`: Class[_], value: Any): Unit = {
    value match {
      case x: Seq[_] => {
        val converted = x.asJava
        super.addParameter(name, converted.getClass, converted)
      }
      case _ => super.addParameter(name, `type`, value)
    }
  }

}
