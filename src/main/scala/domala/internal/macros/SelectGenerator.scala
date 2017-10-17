package domala.internal.macros

import domala.message.Message
import scala.collection.immutable.Seq
import scala.meta._

case class SelectSetting(
    fetchSize: Term.Arg,
    maxRows: Term.Arg,
    strategy: Term.Arg,
    fetch: Term.Arg,
    ensureResult: Term.Arg,
    ensureResultMapping: Term.Arg,
    mapKeyNaming: Term.Arg
)

object SelectGenerator {
  def readSelectSetting(args: Seq[Term.Arg]): SelectSetting = {
    val fetchSize =
      args.collectFirst { case arg"fetchSize = $x" => x }.getOrElse(q"-1")
    val maxRows =
      args.collectFirst { case arg"maxRows = $x" => x }.getOrElse(q"-1")
    val strategy = args
      .collectFirst { case arg"strategy = $x" => x }
      .getOrElse(q"SelectType.RETURN")
    val fetch = args
      .collectFirst { case arg"fetch = $x" => x }
      .getOrElse(q"org.seasar.doma.FetchType.LAZY")
    val ensureResult =
      args.collectFirst { case arg"ensureResult = $x" => x }.getOrElse(q"false")
    val ensureResultMapping = args
      .collectFirst { case arg"ensureResultMapping = $x" => x }
      .getOrElse(q"false")
    val mapKeyNaming = args
      .collectFirst { case arg"mapKeyNaming = $x" => x }
      .getOrElse(q"org.seasar.doma.MapKeyNamingType.NONE")
    SelectSetting(
      fetchSize,
      maxRows,
      strategy,
      fetch,
      ensureResult,
      ensureResultMapping,
      mapKeyNaming)
  }

  def generate(
    trtName: Type.Name,
    _def: Decl.Def,
    internalMethodName: Term.Name,
    args: Seq[Term.Arg]): Defn.Def = {
    val defDecl = QueryDefDecl.of(trtName, _def)
    val commonSetting = DaoMacroHelper.readCommonSetting(
      args,
      trtName.syntax,
      defDecl.name.syntax)
    if (commonSetting.sql.syntax == """""""")
      abort(_def.pos,
            Message.DOMALA4020
              .getMessage(trtName.syntax, defDecl.name.syntax))
    val selectSetting = readSelectSetting(args)

    val (checkParameter: Seq[Stat], isStream: Boolean) =
      selectSetting.strategy match {
        case q"SelectType.RETURN" | q"RETURN" => (Nil, false)
        case q"SelectType.STREAM" | q"STREAM" =>
          (Seq {
            val functionParams = defDecl.paramss.flatten.filter { p =>
              p.decltpe.get match {
                case t"$_ => $_" => true
                case _           => false
              }
            }
            if (functionParams.isEmpty) {
              abort(_def.pos,
                    Message.DOMALA4247
                      .getMessage(trtName.syntax, defDecl.name.syntax))
            } else if (functionParams.length > 1) {
              abort(_def.pos,
                    Message.DOMALA4249
                      .getMessage(trtName.syntax, defDecl.name.syntax))
            }
            val functionParam = Term.Name(functionParams.head.name.toString)
            q"""if ($functionParam == null) throw new org.seasar.doma.DomaNullPointerException(${functionParam.syntax})"""
          }, true)
        case _ => abort(_def.pos, "error")
      }
    val setOptions = {
      val optionParameters = defDecl.paramss.flatten.filter { p =>
        p.decltpe.get match {
          case t"SelectOptions" => true
          case _                => false
        }
      }
      if(optionParameters.length > 1) {
        abort(Message.DOMALA4053.getMessage(trtName.syntax, defDecl.name.syntax))
      } else if(optionParameters.isEmpty) {
        Nil
      } else {
        Seq(q"__query.setOptions(${Term.Name(optionParameters.head.name.syntax)})")
      }

    }

    val (handler, result, setEntityType) =
      if (isStream) {
        val (functionParamTerm, internalTpe, retTpe) = defDecl.paramss.flatten
          .find { p =>
            p.decltpe.get match {
              case t"Stream[$_] => $_" => true
              case _                   => false
            }
          }
          .map { p =>
            p.decltpe.get match {
              case t"Stream[$internalTpe] => $retTpe" =>
                (Term.Name(p.name.syntax), internalTpe, retTpe)
            }
          }
          .getOrElse(abort(_def.pos,
                           Message.DOMALA4244
                             .getMessage(trtName.syntax, defDecl.name.syntax)))
        if (retTpe.toString().trim != defDecl.tpe.toString().trim) {
          abort(_def.pos,
                Message.DOMALA4246.getMessage(
                  defDecl.tpe,
                  retTpe,
                  trtName.syntax,
                  defDecl.name.syntax))
        }
        TypeHelper.convertToDomaType(internalTpe) match {
          case DomaType.Map =>
            (
              q"""new org.seasar.doma.internal.jdbc.command.MapStreamHandler[$retTpe](${selectSetting.mapKeyNaming}, new java.util.function.Function[java.util.stream.Stream[java.util.Map[String, Object]], $retTpe](){
            def apply(p: java.util.stream.Stream[java.util.Map[String, Object]]) = $functionParamTerm(p.toScala[Stream].map(_.asScala.toMap))
            })""",
              q"__command.execute()",
              Nil
            )
          case DomaType.Basic(_, convertedType, wrapperSupplier) =>
            (
              q"""new org.seasar.doma.internal.jdbc.command.BasicStreamHandler(($wrapperSupplier),
              new java.util.function.Function[java.util.stream.Stream[$convertedType], $retTpe](){
              def apply(p: java.util.stream.Stream[$convertedType]) = $functionParamTerm(p.toScala[Stream].map(x => x: $internalTpe))
            })""",
              q"__command.execute()",
              Nil
            )
          case DomaType.EntityOrHolderOrEmbeddable(_) =>
            // 注釈マクロ時は型のメタ情報が見れないためもう一段マクロをかます
            (
              q"domala.internal.macros.reflect.DaoReflectionMacros.getStreamHandler($functionParamTerm, ${trtName.syntax}, ${defDecl.name.syntax})",
              q"__command.execute()",
              Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[$internalTpe](__query)")
            )
          case _ =>
            abort(
              _def.pos,
              Message.DOMALA4008
                .getMessage(defDecl.tpe, trtName.syntax, defDecl.name.syntax))
        }
      } else
        TypeHelper.convertToDomaType(defDecl.tpe) match {
          case DomaType.Option(DomaType.Map, _) =>
            (
              q"new org.seasar.doma.internal.jdbc.command.OptionalMapSingleResultHandler(${selectSetting.mapKeyNaming})",
              q"__command.execute().asScala.map(x => x.asScala.toMap)",
              Nil
            )
          case DomaType.Option(DomaType.Basic(_, _, wrapperSupplier), _) =>
            (
              q"new org.seasar.doma.internal.jdbc.command.OptionalBasicSingleResultHandler($wrapperSupplier, false)",
              q"__command.execute().asScala.map(x => x)",
              Nil
            )
          case DomaType
                .Option(DomaType.EntityOrHolderOrEmbeddable(elementType), _) =>
            // 注釈マクロ時は型のメタ情報が見れないためもう一段マクロをかます
            (
              q"domala.internal.macros.reflect.DaoReflectionMacros.getOptionalSingleResultHandler[$elementType](${trtName.syntax}, ${defDecl.name.syntax})",
              q"__command.execute().asScala",
              Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[$elementType](__query)")
            )
          case DomaType.Option(_, elementTpe) =>
            abort(
              _def.pos,
              Message.DOMALA4235
                .getMessage(elementTpe, trtName.syntax, defDecl.name.syntax))

          case DomaType.Seq(DomaType.Map, _) =>
            (
              q"new org.seasar.doma.internal.jdbc.command.MapResultListHandler(${selectSetting.mapKeyNaming})",
              q"__command.execute().asScala.map(_.asScala.toMap)",
              Nil
            )
          case DomaType.Seq(
              DomaType.Basic(originalType, convertedType, wrapperSupplier),
              _) =>
            (
              q"""new org.seasar.doma.internal.jdbc.command.BasicResultListHandler(($wrapperSupplier): java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$convertedType]])""",
              q"__command.execute().asScala.map(x => x: $originalType)",
              Nil
            )
          case DomaType
                .Seq(DomaType.EntityOrHolderOrEmbeddable(internalTpe), _) =>
            (
              // 注釈マクロ時は型のメタ情報が見れないためもう一段マクロをかます
              q"domala.internal.macros.reflect.DaoReflectionMacros.getResultListHandler[$internalTpe](${trtName.syntax}, ${defDecl.name.syntax})",
              q"__command.execute().asScala",
              Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[$internalTpe](__query)")
            )

          case DomaType.Map =>
            (
              q"new org.seasar.doma.internal.jdbc.command.MapSingleResultHandler(${selectSetting.mapKeyNaming})",
              q"Option(__command.execute()).map(_.asScala.toMap).getOrElse(null)",
              Nil
            )

          case DomaType.Basic(_, _, wrapperSupplier) =>
            (
              q"new org.seasar.doma.internal.jdbc.command.BasicSingleResultHandler($wrapperSupplier, false)",
              q"__command.execute()",
              Nil
            )

          case DomaType.EntityOrHolderOrEmbeddable(tpe) =>
            // 注釈マクロ時は型のメタ情報が見れないためもう一段マクロをかます
            (
              q"domala.internal.macros.reflect.DaoReflectionMacros.getSingleResultHandler[$tpe](${trtName.syntax}, ${defDecl.name.syntax})",
              q"__command.execute().asInstanceOf[$tpe]",
              Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[$tpe](__query)")
            )

          case _ =>
            abort(
              _def.pos,
              Message.DOMALA4008
                .getMessage(defDecl.tpe, trtName.syntax, defDecl.name.syntax))
        }
    val command =
      q"""
      getCommandImplementors.createSelectCommand(
        $internalMethodName,
        __query,
        $handler
      )
      """

    val enteringParam = defDecl.paramss.flatten.map(p =>
      arg"${Term.Name(p.name.toString)}.asInstanceOf[Object]")

    val addParameters = defDecl.paramss.flatten.map { p =>
      val paramTpe = p.decltpe.get match {
        case t"Option[$inner]" => inner
        case t"$container[..$inner]" =>
          val placeHolder = inner.map(_ => t"_")
          t"${Type.Name(container.toString)}[..$placeHolder]"
        case t"Stream[$_] => $r" =>
          t"java.util.function.Function[_, _]"
        case _ => TypeHelper.toType(p.decltpe.get)
      }
      val param = p.decltpe.get match {
        case t"Option[$_]" => q"${Term.Name(p.name.syntax)}.orNull": Term.Arg
        case _ => Term.Name(p.name.syntax): Term.Arg
      }
      q"""__query.addParameter(${p.name.syntax}, classOf[$paramTpe], $param)"""
    }

    val daoParamTypes = defDecl.paramss.flatten.filter(p => p.decltpe.get match {
      case t"Stream[$_] => $_" => false
      case t"SelectOptions" => false
      case _ => true
    }).map { p =>
      val pType: Type = p.decltpe.get match {
        case tpe => TypeHelper.toType(tpe)
      }
      q"domala.internal.macros.DaoParamClass.apply(${p.name.syntax}, classOf[$pType])"
    }

    q"""
    override def ${defDecl.name}= {
      domala.internal.macros.reflect.DaoReflectionMacros.validSql(${trtName.syntax}, ${defDecl.name.syntax}, true, false, ${commonSetting.sql}, ..$daoParamTypes)
      entering(${trtName.syntax}, ${defDecl.name.syntax} ..$enteringParam)
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery(${commonSetting.sql})
        ..$checkParameter
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        ..$setOptions
        ..$setEntityType
        ..$addParameters
        __query.setCallerClassName(${trtName.syntax})
        __query.setCallerMethodName(${defDecl.name.syntax})
        __query.setResultEnsured(${selectSetting.ensureResult})
        __query.setResultMappingEnsured(${selectSetting.ensureResultMapping})
        __query.setFetchType(${selectSetting.fetch})
        __query.setQueryTimeout(${commonSetting.queryTimeout})
        __query.setMaxRows(${selectSetting.maxRows})
        __query.setFetchSize(${selectSetting.fetchSize})
        __query.setSqlLogType(${commonSetting.sqlLogType})
        __query.prepare()
        val __command = $command
        val __result: ${defDecl.tpe} = $result
        __query.complete()
        exiting(${trtName.syntax}, ${defDecl.name.syntax}, __result)
        __result
      } catch {
        case  __e: java.lang.RuntimeException => {
          throwing(${trtName.syntax}, ${defDecl.name.syntax}, __e)
          throw __e
        }
      }
    }
    """
  }
}
