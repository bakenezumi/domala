package domala.internal.macros.meta.args

import domala.internal.macros.meta.util.MetaHelper
import org.seasar.doma.internal.jdbc.sql.SqlParser
import org.seasar.doma.jdbc.JdbcException

import scala.collection.immutable.Seq
import scala.meta._

case class DaoMethodCommonArgs(
  hasSqlAnnotation: Boolean,
  sql: Term.Arg,
  sqlFile: Boolean,
  queryTimeOut: Term.Arg,
  sqlLogType: Term.Arg
)

object DaoMethodCommonArgs {
  def of(args: Seq[Term.Arg], traitName: String, methodName: String): DaoMethodCommonArgs = {
    val (hasSql, sql) =  args.collectFirst{
      case arg"sql = $x" => x
      case arg"$x" if x.syntax.startsWith("\"") => x.syntax.parse[Term.Arg].get
    }.map { x =>
      try {
        new SqlParser(x.syntax).parse()
      } catch {
        case e: JdbcException =>
          MetaHelper.abort(domala.message.Message.DOMALA4069, domala.message.Message.DOMALA9901.getSimpleMessage(traitName, methodName), e)
      }
      (true, x)
    }.getOrElse((false, arg""""""""))
    val sqlFile = args.collectFirst { case arg"sqlFile = true" => true }.getOrElse(false)
    val queryTimeOut =  args.collectFirst{ case arg"queryTimeOut = $x" => x }.getOrElse(arg"-1")
    val sqlLog = args.collectFirst{ case arg"sqlLog = $x" => x }.getOrElse(arg"org.seasar.doma.jdbc.SqlLogType.FORMATTED")
    if(hasSql && sqlFile) {
      MetaHelper.abort(domala.message.Message.DOMALA6021, traitName, methodName)
    }
    DaoMethodCommonArgs(hasSql, sql, sqlFile, queryTimeOut, sqlLog)
  }

}
