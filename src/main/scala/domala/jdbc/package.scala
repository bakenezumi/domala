package domala

import org.seasar.doma


package object jdbc {
  // Alias of Doma type
  type SqlLogType = doma.jdbc.SqlLogType
  object SqlLogType {
    val RAW = doma.jdbc.SqlLogType.RAW
    val FORMATTED = doma.jdbc.SqlLogType.FORMATTED
    val NONE = doma.jdbc.SqlLogType.NONE
  }
  type Naming = doma.jdbc.Naming
  object Naming {
    val NONE: Naming = doma.jdbc.Naming.NONE
    val LOWER_CASE: Naming = doma.jdbc.Naming.LOWER_CASE
    val UPPER_CASE: Naming = doma.jdbc.Naming.UPPER_CASE
    val SNAKE_LOWER_CASE: Naming = doma.jdbc.Naming.SNAKE_LOWER_CASE
    val SNAKE_UPPER_CASE: Naming = doma.jdbc.Naming.SNAKE_UPPER_CASE
    val LENIENT_SNAKE_UPPER_CASE: Naming = doma.jdbc.Naming.LENIENT_SNAKE_UPPER_CASE
  }
}
