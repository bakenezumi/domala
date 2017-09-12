package domala.internal.macros

import meta._

case class DaoMethodCommonSetting(sql: String, queryTimeout: Term.Arg, sqlLogType: Term.Arg)
