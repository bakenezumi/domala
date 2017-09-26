package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._

object DaoMacroHelper {
  def readCommonSetting(args: Seq[Term.Arg]): DaoMethodCommonSetting = {
    val (hasSql, sql) =  args.collectFirst{ case arg"sql = $x" => (true, x) }.getOrElse((false, arg""" "" """))
    val queryTimeOut =  args.collectFirst{ case arg"queryTimeOut = $x" => x }.getOrElse(arg"-1")
    val sqlLog =  args.collectFirst{ case arg"sqlLog = $x" => x }.getOrElse(arg"org.seasar.doma.jdbc.SqlLogType.FORMATTED")
    DaoMethodCommonSetting(hasSql, sql, queryTimeOut, sqlLog)
  }

  def readCommonBatchSetting(args: Seq[Term.Arg]): DaoMethodCommonBatchSetting = {
    val commonSetting = readCommonSetting(args)
    val batchSize = args.collectFirst{ case arg"batchSize = $x" => x }.getOrElse(arg"-1")
    DaoMethodCommonBatchSetting(commonSetting.hasSql, commonSetting.sql, commonSetting.queryTimeout, commonSetting.sqlLogType, batchSize)
  }

  private def hasEntityParameter(resultEntityType: Type, paramTypes: Seq[Term.Param]): Boolean = {
    paramTypes.map(_.decltpe.get.syntax).exists{
      case x if x == resultEntityType.syntax => true
      case _ => false
    }
  }

  def getResultType(defDecl: QueryDefDecl): (Boolean, Type) =  {
    val params = defDecl.paramss.flatten
    defDecl.tpe match {
      case t"Result[$entity]" if hasEntityParameter(entity, params) => (true, entity)
      case t"jdbc.Result[$entity]" if hasEntityParameter(entity, params) => (true, entity)
      case t"domala.jdbc.Result[$entity]" if hasEntityParameter(entity, params) => (true, entity)
      case t"Result[$_]" => abort(defDecl._def.pos,domala.message.Message.DOMALA4222.getMessage(defDecl.trtName.syntax, defDecl.name.syntax))
      case t"jdbc.Result[$_]" => abort(defDecl._def.pos,domala.message.Message.DOMALA4222.getMessage(defDecl.trtName.syntax, defDecl.name.syntax))
      case t"domala.jdbc.Result[$_]" => abort(defDecl._def.pos,domala.message.Message.DOMALA4222.getMessage(defDecl.trtName.syntax, defDecl.name.syntax))
      case t"Int" => (false, t"Int")
      case _ =>
        abort(defDecl._def.pos, domala.message.Message.DOMALA4001
          .getMessage(defDecl.trtName.syntax, defDecl.name.syntax))
    }
  }
}
