package domala.internal.macros.meta.generator

import domala.internal.macros.meta.QueryDefDecl
import domala.internal.macros.meta.util.MetaHelper

import scala.collection.immutable.Seq
import scala.meta._

object DaoMethodGeneratorHelper {

  private def hasEntityParameter(defDecl: QueryDefDecl, resultEntityType: Type, paramTypes: Seq[Term.Param]): Boolean = {
    paramTypes.map(_.decltpe.get.syntax).exists {
      case x if x == resultEntityType.syntax => true
      case _ => MetaHelper.abort(domala.message.Message.DOMALA4222, defDecl.trtName.syntax, defDecl.name.syntax)
    }
  }

  def getResultType(defDecl: QueryDefDecl): (Boolean, Type) = {
    val params = defDecl.paramss.flatten
    defDecl.tpe match {
      case t"Result[$entity]" if hasEntityParameter(defDecl, entity, params) => (true, entity)
      case t"jdbc.Result[$entity]" if hasEntityParameter(defDecl, entity, params) => (true, entity)
      case t"domala.jdbc.Result[$entity]" if hasEntityParameter(defDecl, entity, params) => (true, entity)
      case t"Int" => (false, t"Int")
      case _ =>
        MetaHelper.abort(domala.message.Message.DOMALA4001,
          defDecl.trtName.syntax, defDecl.name.syntax)
    }
  }

  //noinspection ScalaUnusedSymbol
  private def hasEntitySeqParameter(defDecl: QueryDefDecl, resultEntityType: Type, paramTypes: Seq[Term.Param]): Boolean = {
    paramTypes.map(_.decltpe.get).exists {
      case t"$_[$entity]" if entity.syntax == resultEntityType.syntax => true
      case _ => MetaHelper.abort(domala.message.Message.DOMALA4223, defDecl.trtName.syntax, defDecl.name.syntax)
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
        MetaHelper.abort(domala.message.Message.DOMALA4040, defDecl.trtName.syntax, defDecl.name.syntax)
    }
  }

  def validateEntityPropertyNames(defDecl: QueryDefDecl, paramTpe: Type.Name, includedPropertyNames: Seq[Term.Arg], excludedPropertyNames: Seq[Term.Arg]): Seq[Term.Apply] = {
    val validateInclude = if(includedPropertyNames.nonEmpty) {
      Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.validateInclude(classOf[${defDecl.trtName}], ${defDecl.name.syntax}, classOf[$paramTpe], ..$includedPropertyNames)")
    } else Nil
    val validateExclude = if(excludedPropertyNames.nonEmpty) {
      Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.validateExclude(classOf[${defDecl.trtName}], ${defDecl.name.syntax}, classOf[$paramTpe], ..$excludedPropertyNames)")
    } else Nil
    validateInclude ++ validateExclude
  }

}
