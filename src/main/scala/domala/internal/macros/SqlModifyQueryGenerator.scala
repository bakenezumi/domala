package domala.internal.macros

import domala.internal.macros.helper.LiteralConverters._
import domala.internal.macros.helper.{DaoMacroHelper, TypeHelper}

import scala.collection.immutable.Seq
import scala.meta._

object SqlModifyQueryGenerator {

  def generate(
    defDecl: QueryDefDecl,
    commonSetting: DaoMethodCommonSetting,
    internalMethodName: Term.Name,
    query: Term => Term.New,
    otherQuerySettings: Seq[Stat],
    command: Term.Apply,
    populatable: Term
  ): Defn.Def = {
    val params = defDecl.paramss.flatten

    val enteringParam = params.map(p =>
      arg"${Term.Name(p.name.toString)}.asInstanceOf[Object]"
    )

    val checkNullParameter = params.map(p => {
      TypeHelper.convertToDomaType(p.decltpe.get) match {
        case DomaType.Basic(_, _, _, _) => q"()"
        case _ =>
          q"""
          if (${Term.Name(p.name.syntax)} == null) {
            throw new org.seasar.doma.DomaNullPointerException(${p.name.literal})
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
      q"__query.addParameter(${p.name.literal}, classOf[$paramTpe], ${Term.Name(p.name.syntax): Term.Arg})"
    }

    val (isReturnResult, entityType) = DaoMacroHelper.getResultType(defDecl)

    val daoParams = defDecl.paramss.flatten.map { p =>
      q"domala.internal.macros.DaoParam.apply(${p.name.literal}, ${Term.Name(p.name.syntax)}, classOf[${TypeHelper.toType(p.decltpe.get)}])"
    }

    val entityAndEntityType = q"""
    domala.internal.macros.reflect.DaoReflectionMacros.getEntityAndEntityType(${defDecl.trtName.literal}, ${defDecl.name.literal}, classOf[${defDecl.tpe}], ..$daoParams)
    """
    val result = if(isReturnResult) {
      q"domala.jdbc.Result(__count, __query.getEntity.asInstanceOf[$entityType])"
    } else {
      q"__count"
    }

    val daoParamTypes = defDecl.paramss.flatten.map { p =>
      val pType: Type = p.decltpe.get match {
        case tpe => TypeHelper.toType(tpe)
      }
      q"domala.internal.macros.DaoParamClass.apply(${p.name.literal}, classOf[$pType])"
    }

    q"""
    override def ${defDecl.name} = {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(${defDecl.trtName.literal}, ${defDecl.name.literal}, false, $populatable, ${commonSetting.sql}, ..$daoParamTypes)
      entering(${defDecl.trtName.className}, ${defDecl.name.literal}, ..$enteringParam)
      try {
        val __query = ${query(entityAndEntityType)}
        ..$checkNullParameter
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        ..$addParameters
        __query.setCallerClassName(${defDecl.trtName.className})
        __query.setCallerMethodName(${defDecl.name.literal})
        __query.setQueryTimeout(${commonSetting.queryTimeOut})
        __query.setSqlLogType(${commonSetting.sqlLogType})
        ..$otherQuerySettings
        __query.prepare()
        val __command = $command
        val __count = __command.execute()
        __query.complete()
        val __result = $result
        exiting(${defDecl.trtName.className}, ${defDecl.name.literal}, __result)
        __result
      } catch {
        case __e: java.lang.RuntimeException => {
          throwing(${defDecl.trtName.className}, ${defDecl.name.literal}, __e)
          throw __e
        }
      }
    }
    """
  }
}
