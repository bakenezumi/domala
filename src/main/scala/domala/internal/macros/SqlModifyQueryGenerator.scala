package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._

object SqlModifyQueryGenerator {
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
               internalMethodName: Term.Name,
               query: Term,
               otherQuerySettings: Seq[Stat],
               command: Term.Apply): Defn.Def = {
    val params = defDecl.paramss.flatten
    val (isReturnResult, entityType) = DaoMacroHelper.getResultType(defDecl)

    val enteringParam = params.map(p =>
      arg"${Term.Name(p.name.toString)}.asInstanceOf[Object]"
    )

    val checkNullParameter = params.map(p => {
      TypeHelper.convertToDomaType(p.decltpe.get) match {
        case DomaType.Basic(_, _, _) => q"()"
        case _ =>
          q"""
          if (${Term.Name(p.name.syntax)} == null) {
            throw new org.seasar.doma.DomaNullPointerException(${p.name.syntax})
          }
          """
      }
    })

    val addParameters = params.map { p =>
      val paramTpe = p.decltpe.get match {
        case t"$container[..$inner]" =>
          val placeHolder = inner.map(_ => t"_")
          t"${Type.Name(container.toString)}[..$placeHolder]"
        case _ => t"${Type.Name(p.decltpe.get.toString)}"
      }
      q"__query.addParameter(${p.name.syntax}, classOf[$paramTpe], ${Term.Name(p.name.syntax): Term.Arg})"
    }

    val entityAndEntityType = if(isReturnResult) {
      val entityParam = params.collectFirst{
        case x if x.decltpe.get.syntax == entityType.syntax => x
      }.get
      q"Some(domala.jdbc.query.EntityAndEntityType(${entityParam.name.syntax}, ${Term.Name(entityParam.name.syntax)}, ${Term.Name(entityType.syntax)}))"
    } else {
      q"None"
    }

    val result = if(isReturnResult) {
      q"new domala.jdbc.Result[$entityType](__count, __query.getEntity)"
    } else {
      q"__count"
    }

    q"""
    override def ${defDecl.name} = {
      entering(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, ..$enteringParam)
      try {
        val __query = $query($entityAndEntityType)
        ..$checkNullParameter
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        ..$addParameters
        __query.setCallerClassName(${defDecl.trtName.syntax})
        __query.setCallerMethodName(${defDecl.name.syntax})
        __query.setQueryTimeout(${commonSetting.queryTimeout})
        __query.setSqlLogType(${commonSetting.sqlLogType})
        ..$otherQuerySettings
        __query.prepare()
        val __command = $command
        val __count = __command.execute()
        __query.complete()
        val __result = $result
        exiting(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing(${defDecl.trtName.syntax}, ${defDecl.name.syntax}, __e)
          throw __e
        }
      }
    }
    """
  }
}
