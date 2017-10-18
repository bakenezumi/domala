package domala

import org.seasar.doma.jdbc.SqlLogType

class Script(
  sql: String,
  blockDelimiter: String = "",
  haltOnError: Boolean = true,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
