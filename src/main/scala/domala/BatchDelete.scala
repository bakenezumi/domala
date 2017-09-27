package domala

import org.seasar.doma.jdbc.SqlLogType

class BatchDelete(
  sql: String = "",
  queryTimeOut: Int = -1,
  batchSize: Int = -1,
  ignoreVersion: Boolean = false,
  suppressOptimisticLockException: Boolean = false,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
