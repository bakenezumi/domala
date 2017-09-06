package domala

import scala.meta._

class Update extends scala.annotation.StaticAnnotation

object UpdateGenerator {
  def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name): Defn.Def = {
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
}
