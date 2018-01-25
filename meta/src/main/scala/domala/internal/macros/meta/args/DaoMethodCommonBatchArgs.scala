package domala.internal.macros.meta.args

import scala.collection.immutable.Seq
import scala.meta._

case class DaoMethodCommonBatchArgs(
  hasSqlAnnotation: Boolean,
  sql: Term.Arg,
  sqlFile: Boolean,
  queryTimeOut: Term.Arg,
  sqlLogType: Term.Arg,
  batchSize: Term.Arg
)

object DaoMethodCommonBatchArgs {
  def of(args: Seq[Term.Arg], traitName: String, methodName: String): DaoMethodCommonBatchArgs = {
    val commonArgs = DaoMethodCommonArgs.of(args, traitName, methodName)
    val batchSize = args.collectFirst{ case arg"batchSize = $x" => x }.getOrElse(arg"-1")
    DaoMethodCommonBatchArgs(commonArgs.hasSqlAnnotation, commonArgs.sql, commonArgs.sqlFile, commonArgs.queryTimeOut, commonArgs.sqlLogType, batchSize)
  }

}
