package domala.internal.macros.generator

import domala.internal.macros.args.DaoMethodCommonArgs
import domala.internal.macros.util.NameConverters._
import domala.internal.macros.util.TypeUtil
import domala.internal.macros.{DomaType, QueryDefDecl}

import scala.collection.immutable.Seq
import scala.meta._

object SqlModifyQueryGenerator {

  def generate(
    defDecl: QueryDefDecl,
    commonArgs: DaoMethodCommonArgs,
    internalMethodName: Term.Name,
    queryTemplate: (Term, Option[Term]) => Term.New,
    otherQueryArgs: Seq[Stat],
    command: Term.Apply,
    populatable: Term,
  ): Defn.Def = {
    val params = defDecl.paramss.flatten

    val enteringParam = params.map(p =>
      arg"${Term.Name(p.name.toString)}.asInstanceOf[Object]"
    )

    val checkNullParameter = params.map(p => {
      TypeUtil.convertToDomaType(p.decltpe.get) match {
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
      val paramTpe = t"${Type.Name(p.decltpe.get.toString)}"
      q"__query.addParameter(${p.name.literal}, classOf[$paramTpe], ${Term.Name(p.name.syntax): Term.Arg})"
    }

    val (isReturnResult, entityType) = DaoMethodGeneratorHelper.getResultType(defDecl)

    val daoParams = defDecl.paramss.flatten.map { p =>
      q"domala.internal.macros.DaoParam.apply(${p.name.literal}, ${Term.Name(p.name.syntax)}, classOf[${TypeUtil.toType(p.decltpe.get)}])"
    }

    val entityAndEntityType = q"""
    domala.internal.macros.reflect.DaoReflectionMacros.getEntityAndEntityType(classOf[${defDecl.trtName}], ${defDecl.name.literal}, classOf[${defDecl.tpe}], ..$daoParams)
    """
    val result = if(isReturnResult) {
      q"domala.jdbc.Result(__count, __query.getEntity.asInstanceOf[$entityType])"
    } else {
      q"__count"
    }

    val daoParamTypes = defDecl.paramss.flatten.map { p =>
      val pType: Type = p.decltpe.get match {
        case tpe => TypeUtil.toType(tpe)
      }
      q"domala.internal.macros.DaoParamClass.apply(${p.name.literal}, classOf[$pType])"
    }

    val sqlValidator =
      if (commonArgs.hasSqlAnnotation) {
        q"domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[${defDecl.trtName}], ${defDecl.name.literal}, false, $populatable, ${commonArgs.sql}, ..$daoParamTypes)"
      } else q"()"

    val query =
      if (commonArgs.hasSqlAnnotation) queryTemplate(entityAndEntityType, None)
      else queryTemplate(entityAndEntityType, Some(q"domala.internal.macros.reflect.DaoReflectionMacros.getSqlFilePath(classOf[${defDecl.trtName}], ${defDecl.name.literal}, false, $populatable, false, ..$daoParamTypes)"))
    q"""
    override def ${defDecl.name} = {
      $sqlValidator
      entering(${defDecl.trtName.className}, ${defDecl.name.literal}, ..$enteringParam)
      try {
        val __query = $query
        ..$checkNullParameter
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        ..$addParameters
        __query.setCallerClassName(${defDecl.trtName.className})
        __query.setCallerMethodName(${defDecl.name.literal})
        __query.setQueryTimeout(${commonArgs.queryTimeOut})
        __query.setSqlLogType(${commonArgs.sqlLogType})
        ..$otherQueryArgs
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
