package domala.internal.macros

import scala.meta._

case class DaoMethodCommonSetting(hasSql: Boolean, sql: Term.Arg, queryTimeout: Term.Arg, sqlLogType: Term.Arg)

