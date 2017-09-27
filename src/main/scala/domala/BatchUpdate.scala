package domala

import org.seasar.doma.jdbc.SqlLogType

class BatchUpdate(
  sql: String = "",
  queryTimeOut: Int = -1,
  batchSize: Int = -1,
  ignoreVersion: Boolean = false,
  include: collection.Seq[String] = Nil,
  exclude: collection.Seq[String] = Nil,
  suppressOptimisticLockException: Boolean = false,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
