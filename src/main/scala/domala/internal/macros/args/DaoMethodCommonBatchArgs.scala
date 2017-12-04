package domala.internal.macros.args

import scala.collection.immutable.Seq
import scala.meta._

case class DaoMethodCommonBatchArgs(
  hasSql: Boolean, sql: Term.Arg,
  queryTimeOut: Term.Arg,
  sqlLogType: Term.Arg,
  batchSize: Term.Arg
)

object DaoMethodCommonBatchArgs {
  def read(args: Seq[Term.Arg], traitName: String, methodName: String): DaoMethodCommonBatchArgs = {
    val commonArgs = DaoMethodCommonArgs.read(args, traitName, methodName)
    val batchSize = args.collectFirst{ case arg"batchSize = $x" => x }.getOrElse(arg"-1")
    DaoMethodCommonBatchArgs(commonArgs.hasSql, commonArgs.sql, commonArgs.queryTimeOut, commonArgs.sqlLogType, batchSize)
  }

}