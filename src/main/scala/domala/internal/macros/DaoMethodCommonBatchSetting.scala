package domala.internal.macros

import scala.meta._

case class DaoMethodCommonBatchSetting(sql: Term.Arg, queryTimeout: Term.Arg, sqlLogType: Term.Arg, batchSize: Term.Arg)
