package domala

import org.seasar.doma.jdbc.SqlLogType

class Insert(
  sql: String = "",
  queryTimeOut: Int = -1,
  excludeNull: Boolean = false,
  include: collection.Seq[String] = Nil,
  exclude: collection.Seq[String] = Nil,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
