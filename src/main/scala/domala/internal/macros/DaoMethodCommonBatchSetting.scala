package domala.internal.macros

import scala.meta._

case class DaoMethodCommonBatchSetting(hasSql: Boolean, sql: Term.Arg, queryTimeOut: Term.Arg, sqlLogType: Term.Arg, batchSize: Term.Arg)
