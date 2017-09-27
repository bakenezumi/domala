package domala.internal.macros

import org.seasar.doma.internal.jdbc.sql.SqlParser
import org.seasar.doma.jdbc.JdbcException

import scala.collection.immutable.Seq
import scala.meta._

object DaoMacroHelper {
  def readCommonSetting(args: Seq[Term.Arg], traitName: String, methodName: String): DaoMethodCommonSetting = {
    val (hasSql, sql) =  args.collectFirst{
      case arg"sql = $x" => x
      case arg"$x" => x.syntax.parse[Term.Arg].get
    }.map { x =>
      try {
        new SqlParser(x.syntax).parse()
      } catch {
        case e: JdbcException =>
          abort(domala.message.Message.DOMALA4069
            .getMessage(traitName, methodName, e))
      }
      (true, x)
    }.getOrElse((false, arg""""""""))
    val queryTimeOut =  args.collectFirst{ case arg"queryTimeOut = $x" => x }.getOrElse(arg"-1")
    val sqlLog =  args.collectFirst{ case arg"sqlLog = $x" => x }.getOrElse(arg"org.seasar.doma.jdbc.SqlLogType.FORMATTED")
    DaoMethodCommonSetting(hasSql, sql, queryTimeOut, sqlLog)
  }

  def readCommonBatchSetting(args: Seq[Term.Arg], traitName: String, methodName: String): DaoMethodCommonBatchSetting = {
    val commonSetting = readCommonSetting(args, traitName, methodName)
    val batchSize = args.collectFirst{ case arg"batchSize = $x" => x }.getOrElse(arg"-1")
    DaoMethodCommonBatchSetting(commonSetting.hasSql, commonSetting.sql, commonSetting.queryTimeout, commonSetting.sqlLogType, batchSize)
  }

  private def hasEntityParameter(defDecl: QueryDefDecl, resultEntityType: Type, paramTypes: Seq[Term.Param]): Boolean = {
    paramTypes.map(_.decltpe.get.syntax).exists {
      case x if x == resultEntityType.syntax => true
      case _ => abort(domala.message.Message.DOMALA4222.getMessage(defDecl.trtName.syntax, defDecl.name.syntax))
    }
  }

  def getResultType(defDecl: QueryDefDecl): (Boolean, Type) =  {
    val params = defDecl.paramss.flatten
    defDecl.tpe match {
      case t"Result[$entity]" if hasEntityParameter(defDecl, entity, params) => (true, entity)
      case t"jdbc.Result[$entity]" if hasEntityParameter(defDecl, entity, params) => (true, entity)
      case t"domala.jdbc.Result[$entity]" if hasEntityParameter(defDecl, entity, params) => (true, entity)
      case t"Int" => (false, t"Int")
      case _ =>
        abort(defDecl._def.pos, domala.message.Message.DOMALA4001
          .getMessage(defDecl.trtName.syntax, defDecl.name.syntax))
    }
  }

  private def hasEntitySeqParameter(defDecl: QueryDefDecl, resultEntityType: Type, paramTypes: Seq[Term.Param]): Boolean = {
    paramTypes.map(_.decltpe.get).exists {
      case t"Seq[$entity]" if entity.syntax == resultEntityType.syntax => true
      case _ => abort(domala.message.Message.DOMALA4223.getMessage(defDecl.trtName.syntax, defDecl.name.syntax))
    }
  }

  def getBatchResultType(defDecl: QueryDefDecl): (Boolean, Type) =  {
    val params = defDecl.paramss.flatten
    defDecl.tpe match {
      case t"BatchResult[$entity]"
        if hasEntitySeqParameter(defDecl, entity, params) => (true, entity)
      case t"jdbc.BatchResult[$entity]"
        if hasEntitySeqParameter(defDecl, entity, params) => (true, entity)
      case t"domala.jdbc.BatchResult[$entity]"
        if hasEntitySeqParameter(defDecl, entity, params) => (true, entity)
      case t"Array[Int]" => (false, t"Array[Int]")
      case _ =>
        abort(defDecl._def.pos,
          domala.message.Message.DOMALA4040
            .getMessage(defDecl.trtName.syntax, defDecl.name.syntax))
    }
  }

}
