package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._

object DaoMethodMacroHelper {
  def readCommonSetting(args: Seq[Term.Arg]): DaoMethodCommonSetting = {
    val sql =  args.collectFirst{case arg"sql = $x" => x.toString()}.getOrElse("")
    val queryTimeOut =  args.collectFirst{case arg"queryTimeOut = $x" => x}.getOrElse(q"-1")
    val sqlLog =  args.collectFirst{case arg"sqlLog = $x" => x}.getOrElse(q"org.seasar.doma.jdbc.SqlLogType.FORMATTED")
    DaoMethodCommonSetting(sql, queryTimeOut, sqlLog)
  }
}
