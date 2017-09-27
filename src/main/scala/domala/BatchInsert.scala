package domala

import org.seasar.doma.jdbc.SqlLogType

class BatchInsert(
  sql: String = "",
  queryTimeOut: Int = -1,
  batchSize: Int = -1,
  include: collection.Seq[String] = Nil,
  exclude: collection.Seq[String] = Nil,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
