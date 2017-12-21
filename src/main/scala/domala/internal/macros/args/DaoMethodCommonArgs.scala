package domala.internal.macros.args

import domala.internal.macros.util.MacrosHelper
import org.seasar.doma.internal.jdbc.sql.SqlParser
import org.seasar.doma.jdbc.JdbcException

import scala.collection.immutable.Seq
import scala.meta._

case class DaoMethodCommonArgs(
  hasSql: Boolean,
  sql: Term.Arg,
  queryTimeOut: Term.Arg,
  sqlLogType: Term.Arg
)

object DaoMethodCommonArgs {
  def read(args: Seq[Term.Arg], traitName: String, methodName: String): DaoMethodCommonArgs = {
    val (hasSql, sql) =  args.collectFirst{
      case arg"sql = $x" => x
      case arg"$x" if x.syntax.startsWith("\"") => x.syntax.parse[Term.Arg].get
    }.map { x =>
      try {
        new SqlParser(x.syntax).parse()
      } catch {
        case e: JdbcException =>
          MacrosHelper.abort(domala.message.Message.DOMALA4069, domala.message.Message.DOMALA9901.getMessage(traitName, methodName), e)
      }
      (true, x)
    }.getOrElse((false, arg""""""""))
    val queryTimeOut =  args.collectFirst{ case arg"queryTimeOut = $x" => x }.getOrElse(arg"-1")
    val sqlLog =  args.collectFirst{ case arg"sqlLog = $x" => x }.getOrElse(arg"org.seasar.doma.jdbc.SqlLogType.FORMATTED")
    DaoMethodCommonArgs(hasSql, sql, queryTimeOut, sqlLog)
  }

}
