package domala

import org.seasar.doma.jdbc.SqlLogType
import org.seasar.doma.{FetchType, MapKeyNamingType}
import collection.immutable.Seq
import scala.meta._

class Select(
  sql: String,
  queryTimeout: Int = -1,
  fetchSize: Int = -1,
  maxRows: Int = -1,
  strategy: SelectType = SelectType.RETURN,
  fetch: FetchType = FetchType.LAZY,
  ensureResult: Boolean = false,
  ensureResultMapping: Boolean = false,
  mapKeyNaming: MapKeyNamingType = MapKeyNamingType.NONE,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation

sealed trait SelectType

object SelectType {
  case object RETURN extends SelectType
  case object STREAM extends SelectType
}

package internal { package macros {




}}
