package domala.internal.macros

import org.seasar.doma.internal.jdbc.sql.SqlParser
import org.seasar.doma.jdbc.JdbcException

import scala.collection.immutable.Seq
import scala.meta._

object DaoMacroHelper {
  def readCommonSetting(args: Seq[Term.Arg], traitName: String, methodName: String): DaoMethodCommonSetting = {
    val (hasSql, sql) =  args.collectFirst{
      case arg"sql = $x" => x
      case arg"$x" if x.syntax.startsWith("\"") => x.syntax.parse[Term.Arg].get
    }.map { x =>
      try {
        new SqlParser(x.syntax).parse()
      } catch {
        case e: JdbcException =>
          MacrosHelper.abort(domala.message.Message.DOMALA4069, traitName, methodName, e)
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
    DaoMethodCommonBatchSetting(commonSetting.hasSql, commonSetting.sql, commonSetting.queryTimeOut, commonSetting.sqlLogType, batchSize)
  }

  private def hasEntityParameter(defDecl: QueryDefDecl, resultEntityType: Type, paramTypes: Seq[Term.Param]): Boolean = {
    paramTypes.map(_.decltpe.get.syntax).exists {
      case x if x == resultEntityType.syntax => true
      case _ => MacrosHelper.abort(domala.message.Message.DOMALA4222, defDecl.trtName.syntax, defDecl.name.syntax)
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
        MacrosHelper.abort(domala.message.Message.DOMALA4001,
          defDecl.trtName.syntax, defDecl.name.syntax)
    }
  }

  //noinspection ScalaUnusedSymbol
  private def hasEntitySeqParameter(defDecl: QueryDefDecl, resultEntityType: Type, paramTypes: Seq[Term.Param]): Boolean = {
    paramTypes.map(_.decltpe.get).exists {
      case t"$_[$entity]" if entity.syntax == resultEntityType.syntax => true
      case _ => MacrosHelper.abort(domala.message.Message.DOMALA4223, defDecl.trtName.syntax, defDecl.name.syntax)
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
        MacrosHelper.abort(domala.message.Message.DOMALA4040, defDecl.trtName.syntax, defDecl.name.syntax)
    }
  }

  def validateEntityPropertyNames(defDecl: QueryDefDecl, paramTpe: Type.Name, includedPropertyNames: Seq[Term.Arg], excludedPropertyNames: Seq[Term.Arg]): Seq[Term.Apply] = {
    val validateInclude = if(includedPropertyNames.nonEmpty) {
      Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.validateInclude(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, classOf[$paramTpe], ..$includedPropertyNames)")
    } else Nil
    val validateExclude = if(excludedPropertyNames.nonEmpty) {
      Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.validateExclude(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, classOf[$paramTpe], ..$excludedPropertyNames)")
    } else Nil
    validateInclude ++ validateExclude
  }

}
