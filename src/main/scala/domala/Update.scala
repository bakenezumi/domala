package domala

import org.seasar.doma.jdbc.SqlLogType

class Update(
  sql: String = "",
  queryTimeOut: Int = -1,
  excludeNull: Boolean = false,
  ignoreVersion: Boolean = false,
  include: collection.Seq[String] = Nil,
  exclude: collection.Seq[String] = Nil,
  suppressOptimisticLockException: Boolean = false,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
