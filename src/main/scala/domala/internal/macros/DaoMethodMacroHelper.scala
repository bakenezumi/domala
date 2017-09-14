package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._

object DaoMethodMacroHelper {
  def readCommonSetting(args: Seq[Term.Arg]): DaoMethodCommonSetting = {
    val sql =  args.collectFirst{ case arg"sql = $x" => x }.getOrElse(arg""" "" """)
    val queryTimeOut =  args.collectFirst{ case arg"queryTimeOut = $x" => x }.getOrElse(arg"-1")
    val sqlLog =  args.collectFirst{ case arg"sqlLog = $x" => x }.getOrElse(arg"org.seasar.doma.jdbc.SqlLogType.FORMATTED")
    DaoMethodCommonSetting(sql, queryTimeOut, sqlLog)
  }

  def readCommonBatchSetting(args: Seq[Term.Arg]): DaoMethodCommonBatchSetting = {
    val commonSetting = readCommonSetting((args))
    val batchSize = args.collectFirst{ case arg"batchSize = $x" => x }.getOrElse(arg"-1")
    DaoMethodCommonBatchSetting(commonSetting.sql, commonSetting.queryTimeout, commonSetting.sqlLogType, batchSize)
  }
}
