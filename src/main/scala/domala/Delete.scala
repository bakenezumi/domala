package domala

import org.seasar.doma.jdbc.SqlLogType

class Delete(
  sql: String = "",
  queryTimeOut: Int = -1,
  ignoreVersion: Boolean = false,
  suppressOptimisticLockException: Boolean = false,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
