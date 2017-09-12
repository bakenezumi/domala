package domala.internal.macros

import scala.meta._
import scala.collection.immutable.Seq

object AutoModifyQueryGenerator {
  def extractParameter(defDecl: QueryDefDecl): (Term.Name, Type.Name) = {
    if (defDecl.paramss.flatten.length != 1)
      abort(defDecl._def.pos,
            org.seasar.doma.message.Message.DOMA4002
              .getMessage(defDecl.trtName.value, defDecl.name.value))
    defDecl.paramss.flatten.head match {
      case param"$paramName: ${Some(paramTpe)}" =>
        (Term.Name(paramName.value), Type.Name(paramTpe.toString))
    }
  }

  def generate(defDecl: QueryDefDecl,
               commonSetting: DaoMethodCommonSetting,
               paramName: Term.Name,
               paramType: Type.Name,
               internalMethodName: Term.Name,
               query: Term.Apply,
               otherQuerySettings: Seq[Stat],
               command: Term.Apply): Defn.Def = {
   defDecl.tpe match {
    case t"Result[$entity]" => ()
    case t"jdbc.Result[$entity]" => ()
    case t"domala.jdbc.Result[$entity]" => ()
    case _ => abort(defDecl._def.pos,
     domala.message.Message.DOMALA4222
       .getMessage(defDecl.trtName.value, defDecl.name.value))
   }

    q"""
    override def ${defDecl.name} = {
      entering(${defDecl.trtName.value}, ${defDecl.name.value}, $paramName)
      try {
        if ($paramName == null) {
          throw new org.seasar.doma.DomaNullPointerException(${paramName.value})
        }
        val __query = $query
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        __query.setEntity($paramName)
        __query.setCallerClassName(${defDecl.trtName.value})
        __query.setCallerMethodName(${defDecl.name.value})
        __query.setQueryTimeout(${commonSetting.queryTimeout})
        __query.setSqlLogType(${commonSetting.sqlLogType})
        ..$otherQuerySettings
        __query.prepare()
        val __command = $command
        val __count: Int = __command.execute()
        __query.complete()
        val __result =
          new domala.jdbc.Result[$paramType](__count, __query.getEntity)
        exiting(${defDecl.trtName.value}, ${defDecl.name.value}, __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing(${defDecl.trtName.value}, ${defDecl.name.value}, __e)
          throw __e
        }
      }
    }
    """
  }
}
