package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._
import org.scalameta.logger

/**
 * @see [[https://github.com/domaframework/doma/blob/master/src/main/java/org/seasar/doma/internal/apt/DaoGenerator.java]]
 */
object DaoGenerator {

  def generate(trt: Defn.Trait, config: Term.Arg) = {
    val stats = trt.templ.stats.map(l =>
      l.collect {
        case _def :Decl.Def => _def
      }.zip(from(0)).flatMap { t:(Decl.Def, Int) =>
        generateDef(trt.name, t._1, t._2)
      }
    )

    val obj =
      q"""
      object ${Term.Name(trt.name.value)} 
        extends org.seasar.doma.internal.jdbc.dao.AbstractDao($config)
        with ${Ctor.Ref.Name(trt.name.value)} {
        import scala.collection.JavaConverters._
        import scala.compat.java8.OptionConverters._
        ..${stats.get}
      }
      """
    logger.debug(obj)
    Term.Block(Seq(trt, obj))
  }

  private def from(n: Int): Stream[Int] = n #:: from(n+1)

  protected def generateDef(trtName: Type.Name, _def: Decl.Def, idx: Int) = {
    val internalMethodName = Term.Name( s"__method$idx" )
    List(
      {
        val paramClasses = _def.paramss.flatten.map(p => q"classOf[${Type.Name(p.decltpe.get.toString)}]")
        q"""private val ${Pat.Var.Term(internalMethodName)} =
              org.seasar.doma.internal.jdbc.dao.AbstractDao.getDeclaredMethod(classOf[$trtName], ${_def.name.value}..$paramClasses)"""
      },
      // TDOD: Anotationが無い場合
      _def.mods.collectFirst {
        case mod"@Script(sql = $sql)" => generateScript(trtName, _def, internalMethodName, sql)
        case mod"@Select(sql = $sql)" => generateSelect(trtName, _def, internalMethodName, sql)
        case mod"@Insert" => generateInsert(trtName, _def, internalMethodName)
        case mod"@Insert(..$modParams)" => generateInsert(trtName, _def, internalMethodName)
        case mod"@Update" => generateUpdate(trtName, _def, internalMethodName)
        case mod"@Update(..$modParams)" => generateUpdate(trtName, _def, internalMethodName)
        case mod"@Delete" => generateDelete(trtName, _def, internalMethodName)
        case mod"@Delete(..$modParams)" => generateDelete(trtName, _def, internalMethodName)
      }.get.copy(tparams = _def.tparams, paramss = _def.paramss)
    )

  }

  protected def generateScript(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, sql: Term.Arg) = {
    val Decl.Def(mods, name, tparams, paramss, tpe) = _def
    val trtNameStr = trtName.value
    val nameStr = name.value

    q"""
      override def $name = {
        entering($trtNameStr, $nameStr)
        try {
          val __query = new domala.jdbc.query.SqlScriptQuery($sql)
          __query.setMethod($internalMethodName)
          __query.setConfig(__config)
          __query.setCallerClassName($trtNameStr)
          __query.setCallerMethodName($nameStr)
          __query.setBlockDelimiter("")
          __query.setHaltOnError(true)
          __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
          __query.prepare()
          val __command = new domala.jdbc.command.ScriptCommand(__query)
          __command.execute()
          __query.complete()
          exiting($trtNameStr, $nameStr, null)
        } catch {
          case __e: java.lang.RuntimeException => {
            throwing($trtNameStr, $nameStr, __e)
            throw __e
          }
        }
      }
    """
  }

  protected def generateSelect(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, sql: Term.Arg) = {
    val Decl.Def(mods, name, tparams, paramss, tpe) = _def
    val trtNameStr = trtName.value
    val nameStr = name.value

    // Todo: 戻りの型の対応を増やす
    val  (javaTpe, handler, result, setEntity) = tpe match {
      case t"$containerTpe[$internalTpe]" => {
        val internalTpeTerm = Term.Name(internalTpe.toString)
        containerTpe match {
          case t"Option" => (
            t"java.util.Optional[$internalTpe]",
            q"new org.seasar.doma.internal.jdbc.command.OptionalEntitySingleResultHandler($internalTpeTerm.getSingletonInternal())",
            q"__command.execute().asScala",
            Seq(q"__query.setEntityType($internalTpeTerm.getSingletonInternal())")
          )
          case t"Seq" => (
            t"java.util.List[$internalTpe]",
            q"new org.seasar.doma.internal.jdbc.command.EntityResultListHandler($internalTpeTerm.getSingletonInternal())",
            q"__command.execute().asScala",
            Seq(q"__query.setEntityType($internalTpeTerm.getSingletonInternal())")
          )
        }
      }
      case t"Int" => (
        t"Integer",
        q"new org.seasar.doma.internal.jdbc.command.BasicSingleResultHandler[Integer](() => new org.seasar.doma.wrapper.IntegerWrapper, false)",
        q"__command.execute()",
        Nil
      )
      case _ => { // Entity
        val tpeTerm = Term.Name(tpe.toString)
        (
          tpe,
          q"new org.seasar.doma.internal.jdbc.command.EntitySingleResultHandler($tpeTerm.getSingletonInternal())",
          q"__command.execute()",
          Seq(q"__query.setEntityType($tpeTerm.getSingletonInternal())")
        )
      }
    }
    val command =
      q"""
      getCommandImplementors().createSelectCommand(
        $internalMethodName,
        __query,
        $handler
      )
      """


    val enteringParam = paramss.flatten.map( p =>
      arg"${Term.Name(p.name.toString)}.asInstanceOf[Object]"
    )
    val addParameterStats = paramss.flatten.map{ p =>
      q"""__query.addParameter(${p.name.value}, classOf[${Type.Name(p.decltpe.get.toString)}], ${Term.Name(p.name.value): Term.Arg})"""
    }

    q"""
      override def $name = {
        entering($trtNameStr, $nameStr ..$enteringParam)
        try {
          val __query = new domala.jdbc.query.SqlSelectQuery($sql)
          __query.setMethod($internalMethodName)
          __query.setConfig(__config)
          ..$setEntity
          ..$addParameterStats
          __query.setCallerClassName($trtNameStr)
          __query.setCallerMethodName($nameStr)
          __query.setResultEnsured(false)
          __query.setResultMappingEnsured(false)
          __query.setFetchType(org.seasar.doma.FetchType.LAZY)
          __query.setQueryTimeout(-1)
          __query.setMaxRows(-1)
          __query.setFetchSize(-1)
          __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
          __query.prepare()
          val __command: org.seasar.doma.jdbc.command.SelectCommand[$javaTpe] = $command
          val __result: $tpe = $result
          __query.complete()
          exiting($trtNameStr, $nameStr, __result)
          __result
        } catch {
          case  __e: java.lang.RuntimeException => {
            throwing($trtNameStr, $nameStr, __e)
            throw __e
          }
        }
      }
    """
  }

  protected def generateInsert(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name) = {
    val Decl.Def(mods, name, tparams, paramss, tpe) = _def
    val trtNameStr = trtName.value
    val nameStr = name.value
    val (paramName, paramTpe) = paramss.flatten.head match {
      case param"$paramName: ${Some(paramTpe)}" =>
        (
          Term.Name(paramName.value),
          Type.Name(paramTpe.toString),
        )
    }
    q"""
    override def $name = {
      entering($trtNameStr, $nameStr, $paramName)
      try {
        if ($paramName == null) {
          throw new org.seasar.doma.DomaNullPointerException(${paramName.value})
        }
        val __query: org.seasar.doma.jdbc.query.AutoInsertQuery[$paramTpe] =
          getQueryImplementors.createAutoInsertQuery(
            $internalMethodName,
            ${Term.Name(paramTpe.value)}.getSingletonInternal)
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        __query.setEntity($paramName)
        __query.setCallerClassName($trtNameStr)
        __query.setCallerMethodName($nameStr)
        __query.setQueryTimeout(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setNullExcluded(false)
        __query.setIncludedPropertyNames()
        __query.setExcludedPropertyNames()
        __query.prepare()
        val __command: org.seasar.doma.jdbc.command.InsertCommand =
          getCommandImplementors.createInsertCommand($internalMethodName, __query)
        val __count: Int = __command.execute()
        __query.complete()
        val __result =
          new org.seasar.doma.jdbc.Result[$paramTpe](__count,
                                                        __query.getEntity)
        exiting($trtNameStr, $nameStr, __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing($trtNameStr, $nameStr, __e)
          throw __e
        }
      }
    }
    """
  }

  protected def generateUpdate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name) = {
    val Decl.Def(mods, name, tparams, paramss, tpe) = _def
    val trtNameStr = trtName.value
    val nameStr = name.value
    val (paramName, paramTpe) = paramss.flatten.head match {
      case param"$paramName: ${Some(paramTpe)}" =>
        (
          Term.Name(paramName.value),
          Type.Name(paramTpe.toString),
        )
    }

    q"""
    override def $name = {
      entering($trtNameStr, $nameStr, $paramName)
      try {
        if ($paramName == null) {
          throw new org.seasar.doma.DomaNullPointerException(${paramName.value})
        }
        val __query: org.seasar.doma.jdbc.query.AutoUpdateQuery[$paramTpe] =
          getQueryImplementors.createAutoUpdateQuery(
            $internalMethodName,
            ${Term.Name(paramTpe.value)}.getSingletonInternal)
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        __query.setEntity($paramName)
        __query.setCallerClassName($trtNameStr)
        __query.setCallerMethodName($nameStr)
        __query.setQueryTimeout(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setNullExcluded(false)
        __query.setVersionIgnored(false)
        __query.setIncludedPropertyNames()
        __query.setExcludedPropertyNames()
        __query.setUnchangedPropertyIncluded(false)
        __query.setOptimisticLockExceptionSuppressed(false)
        __query.prepare()
        val __command: org.seasar.doma.jdbc.command.UpdateCommand =
          getCommandImplementors.createUpdateCommand($internalMethodName, __query)
        val __count: Int = __command.execute()
        __query.complete()
        val __result =
          new org.seasar.doma.jdbc.Result[$paramTpe](__count,
                                                        __query.getEntity)
        exiting($trtNameStr, $nameStr, __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing($trtNameStr, $nameStr, __e)
          throw __e
        }
      }
    }
    """
  }
  protected def generateDelete(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name) = {
    val Decl.Def(mods, name, tparams, paramss, tpe) = _def
    val trtNameStr = trtName.value
    val nameStr = name.value
    val (paramName, paramTpe) = paramss.flatten.head match {
      case param"$paramName: ${Some(paramTpe)}" =>
        (
          Term.Name(paramName.value),
          Type.Name(paramTpe.toString),
        )
    }

    q"""
    override def $name = {
      entering($trtNameStr, $nameStr, $paramName)
      try {
        if ($paramName == null) {
          throw new org.seasar.doma.DomaNullPointerException(${paramName.value})
        }
        val __query: org.seasar.doma.jdbc.query.AutoDeleteQuery[$paramTpe] =
          getQueryImplementors.createAutoDeleteQuery(
            $internalMethodName,
            ${Term.Name(paramTpe.value)}.getSingletonInternal)
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        __query.setEntity($paramName)
        __query.setCallerClassName($trtNameStr)
        __query.setCallerMethodName($nameStr)
        __query.setQueryTimeout(-1)
        __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
        __query.setVersionIgnored(false)
        __query.setOptimisticLockExceptionSuppressed(false)
        __query.prepare()
        val __command: org.seasar.doma.jdbc.command.DeleteCommand =
          getCommandImplementors.createDeleteCommand($internalMethodName, __query)
        val __count: Int = __command.execute()
        __query.complete()
        val __result =
          new org.seasar.doma.jdbc.Result[$paramTpe](__count,
                                                        __query.getEntity)
        exiting($trtNameStr, $nameStr, __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing($trtNameStr, $nameStr, __e)
          throw __e
        }
      }
    }
    """
  }
}
