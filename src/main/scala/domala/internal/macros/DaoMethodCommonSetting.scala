package domala.internal.macros

import scala.meta._

case class DaoMethodCommonSetting(hasSql: Boolean, sql: Term.Arg, queryTimeOut: Term.Arg, sqlLogType: Term.Arg)

