package domala.internal.macros

import domala.Select
import domala.internal.macros.args.DaoMethodCommonArgs
import domala.internal.macros.util.LiteralConverters._
import domala.internal.macros.util.{MacrosHelper, TypeUtil}
import domala.message.Message

import scala.collection.immutable.Seq
import scala.meta._

object SelectGenerator extends DaoMethodGenerator {

  override def annotationClass: Class[Select] = classOf[Select]

  case class SelectArgs(
    fetchSize: Term.Arg,
    maxRows: Term.Arg,
    strategy: Term.Arg,
    fetch: Term.Arg,
    ensureResult: Term.Arg,
    ensureResultMapping: Term.Arg,
    mapKeyNaming: Term.Arg
  )

  object SelectArgs {
    def read(args: Seq[Term.Arg]): SelectArgs = {
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
      SelectArgs(
        fetchSize,
        maxRows,
        strategy,
        fetch,
        ensureResult,
        ensureResultMapping,
        mapKeyNaming)
    }
  }

  override def generate(
    trtName: Type.Name,
    _def: Decl.Def,
    internalMethodName: Term.Name,
    args: Seq[Term.Arg]): Defn.Def = {
    val defDecl = QueryDefDecl.of(trtName, _def)
    val commonArgs = DaoMethodCommonArgs.read(
      args,
      trtName.syntax,
      defDecl.name.syntax)
    if (commonArgs.sql.syntax == """""""")
      MacrosHelper.abort(Message.DOMALA4020, trtName.syntax, defDecl.name.syntax)
    val selectArgs = SelectArgs.read(args)
    if(TypeUtil.isWildcardType(defDecl.tpe))
      MacrosHelper.abort(Message.DOMALA4207, defDecl.tpe, trtName.syntax, defDecl.name.syntax)

    val (checkParameter: Seq[Stat], isStream: Boolean, isIterator: Boolean) =
      selectArgs.strategy match {
        case q"SelectType.RETURN" | q"RETURN" => (Nil, false, false)
        case q"SelectType.STREAM" | q"STREAM" =>
          (Seq {
            //noinspection ScalaUnusedSymbol
            val functionParams = defDecl.paramss.flatten.filter { p =>
              p.decltpe.get match {
                case t"Stream[$_] => $_" => true
                case _           => false
              }
            }
            if (functionParams.isEmpty) {
              MacrosHelper.abort(Message.DOMALA4247, trtName.syntax, defDecl.name.syntax)
            } else if (functionParams.length > 1) {
              MacrosHelper.abort(Message.DOMALA4249, trtName.syntax, defDecl.name.syntax)
            }
            val functionParam = Term.Name(functionParams.head.name.toString)
            q"""if ($functionParam == null) throw new org.seasar.doma.DomaNullPointerException(${functionParam.literal})"""
          }, true, false)
        case q"SelectType.ITERATOR" | q"ITERATOR" =>
          (Seq {
            //noinspection ScalaUnusedSymbol
            val functionParams = defDecl.paramss.flatten.filter { p =>
              p.decltpe.get match {
                case t"Iterator[$_] => $_" => true
                case _           => false
              }
            }
            if (functionParams.isEmpty) {
              MacrosHelper.abort(Message.DOMALA6009, trtName.syntax, defDecl.name.syntax)
            } else if (functionParams.length > 1) {
              MacrosHelper.abort(Message.DOMALA6010, trtName.syntax, defDecl.name.syntax)
            }
            val functionParam = Term.Name(functionParams.head.name.toString)
            q"""if ($functionParam == null) throw new org.seasar.doma.DomaNullPointerException(${functionParam.literal})"""
          }, false, true)
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
        MacrosHelper.abort(Message.DOMALA4053, trtName.syntax, defDecl.name.syntax)
      } else if(optionParameters.isEmpty) {
        Nil
      } else {
        Seq(q"__query.setOptions(${Term.Name(optionParameters.head.name.syntax)})")
      }

    }

    val commandTemplate = (handler: Term) =>
      q"""
      getCommandImplementors.createSelectCommand(
        $internalMethodName,
        __query,
        $handler
      )
      """

    val (result, setEntityType) =
      if (isStream) {
        val (functionParamTerm, internalTpe, retTpe) = defDecl.paramss.flatten
          .find { p =>
            //noinspection ScalaUnusedSymbol
            p.decltpe.get match {
              case t"Stream[$_] => $_" => true
              case x if TypeUtil.isWildcardType(x) =>
                MacrosHelper.abort(Message.DOMALA4243, x.children.head.syntax, trtName.syntax, defDecl.name.syntax)
              case _ => false
            }
          }
          .map { p =>
            p.decltpe.get match {
              case t"Stream[$internalTpe] => $retTpe" =>
                (Term.Name(p.name.syntax), internalTpe, retTpe)
            }
          }
          .getOrElse(MacrosHelper.abort(Message.DOMALA4244, trtName.syntax, defDecl.name.syntax))
        if (retTpe.toString().trim != defDecl.tpe.toString().trim) {
          MacrosHelper.abort(Message.DOMALA4246,
            defDecl.tpe,
            retTpe,
            trtName.syntax,
            defDecl.name.syntax)
        }
        TypeUtil.convertToDomaType(internalTpe) match {
          case DomaType.Map =>
            val command = commandTemplate(
              q"""
              new org.seasar.doma.internal.jdbc.command.MapStreamHandler[$retTpe](${selectArgs.mapKeyNaming},
                (p: java.util.stream.Stream[java.util.Map[String, Object]]) => $functionParamTerm(domala.internal.WrapStream.of(p).map(_.asScala.toMap))
              )""")
            (
              q"$command.execute()",
              Nil
            )
          case DomaType.Basic(_, convertedType, wrapperSupplier, _) =>
            val command = commandTemplate(
              q"""
              new org.seasar.doma.internal.jdbc.command.BasicStreamHandler(($wrapperSupplier),
                (p: java.util.stream.Stream[$convertedType]) => $functionParamTerm(domala.internal.WrapStream.of(p).map(x => x: $internalTpe))
              )""")
            (
              q"$command.execute()",
              Nil
            )
          case DomaType.EntityOrHolderOrEmbeddable(_) =>
            // 注釈マクロ時は型のメタ情報が見れないためもう一段マクロをかます
            val command = commandTemplate(
              q"domala.internal.macros.reflect.DaoReflectionMacros.getStreamHandler($functionParamTerm, classOf[$trtName], ${defDecl.name.literal})")
            (
              q"$command.execute()",
              Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[$internalTpe](__query)")
            )
          case _ =>
            MacrosHelper.abort(
              Message.DOMALA4008, defDecl.tpe, trtName.syntax, defDecl.name.syntax)
        }
      } else if (isIterator) {
        val (functionParamTerm, internalTpe, retTpe) = defDecl.paramss.flatten
          .find { p =>
            //noinspection ScalaUnusedSymbol
            p.decltpe.get match {
              case t"Iterator[$_] => $_" => true
              case x if TypeUtil.isWildcardType(x) =>
                MacrosHelper.abort(Message.DOMALA4243, x.children.head.syntax, trtName.syntax, defDecl.name.syntax)
              case _  => false
            }
          }
          .map { p =>
            p.decltpe.get match {
              case t"Iterator[$internalTpe] => $retTpe" =>
                (Term.Name(p.name.syntax), internalTpe, retTpe)
            }
          }
          .getOrElse( MacrosHelper.abort(Message.DOMALA6011, trtName.syntax, defDecl.name.syntax))
        if (retTpe.toString().trim != defDecl.tpe.toString().trim) {
          MacrosHelper.abort(Message.DOMALA4246,
            defDecl.tpe,
            retTpe,
            trtName.syntax,
            defDecl.name.syntax)
        }
        TypeUtil.convertToDomaType(internalTpe) match {
          case DomaType.Map =>
            val command = commandTemplate(
              q"""
              new org.seasar.doma.internal.jdbc.command.MapStreamHandler[$retTpe](${selectArgs.mapKeyNaming},
                (p: java.util.stream.Stream[java.util.Map[String, Object]]) => $functionParamTerm(domala.internal.WrapIterator.of(p).map(_.asScala.toMap))
              )""")
            (
              q"$command.execute()",
              Nil
            )
          case DomaType.Basic(_, convertedType, wrapperSupplier, _) =>
            val command = commandTemplate(
              q"""
              new org.seasar.doma.internal.jdbc.command.BasicStreamHandler(($wrapperSupplier),
                (p: java.util.stream.Stream[$convertedType]) => $functionParamTerm(domala.internal.WrapIterator.of(p).map(x => x: $internalTpe))
              )""")
            (
              q"$command.execute()",
              Nil
            )
          case DomaType.EntityOrHolderOrEmbeddable(_) =>
            // 注釈マクロ時は型のメタ情報が見れないためもう一段マクロをかます
            val command = commandTemplate(
              q"domala.internal.macros.reflect.DaoReflectionMacros.getIteratorHandler($functionParamTerm, classOf[$trtName], ${defDecl.name.literal})")
            (
              q"$command.execute()",
              Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[$internalTpe](__query)")
            )
          case _ =>
            MacrosHelper.abort(
              Message.DOMALA4008, defDecl.tpe, trtName.syntax, defDecl.name.syntax)
        }
      } else
        TypeUtil.convertToDomaType(defDecl.tpe) match {
          case DomaType.Option(DomaType.Map, _) =>
            val command = commandTemplate(
              q"new org.seasar.doma.internal.jdbc.command.OptionalMapSingleResultHandler(${selectArgs.mapKeyNaming})")
            (
              q"domala.internal.OptionConverters.asScala($command.execute()).map(x => x.asScala.toMap)",
              Nil
            )
          case DomaType.Option(DomaType.Basic(_, _, wrapperSupplier, _), _) =>
            val command = commandTemplate(
              q"new org.seasar.doma.internal.jdbc.command.OptionalBasicSingleResultHandler($wrapperSupplier, false)")
            (
              q"domala.internal.OptionConverters.asScala($command.execute()).map(x => x)",
              Nil
            )
          case DomaType
                .Option(DomaType.EntityOrHolderOrEmbeddable(elementType), _) =>
            // 注釈マクロ時は型のメタ情報が見れないためもう一段マクロをかます
            val command = commandTemplate(
              q"domala.internal.macros.reflect.DaoReflectionMacros.getOptionalSingleResultHandler[$trtName, $elementType](classOf[$trtName], ${defDecl.name.literal})")
            (
              q"domala.internal.OptionConverters.asScala($command.execute())",
              Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[$elementType](__query)")
            )
          case DomaType.Option(_, elementTpe) =>
            MacrosHelper.abort(
              Message.DOMALA4235, elementTpe, trtName.syntax, defDecl.name.syntax)

          case DomaType.Seq(DomaType.Map, _) =>
            val command = commandTemplate(
              q"new org.seasar.doma.internal.jdbc.command.MapResultListHandler(${selectArgs.mapKeyNaming})")
            (
              q"$command.execute().asScala.map(_.asScala.toMap)",
              Nil
            )
          case DomaType.Seq(
              DomaType.Basic(originalType, convertedType, wrapperSupplier, _),
              _) =>
            val command = commandTemplate(
              q"""new org.seasar.doma.internal.jdbc.command.BasicResultListHandler(($wrapperSupplier): java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$convertedType]])""")
            (
              q"$command.execute().asScala.map(x => x: $originalType)",
              Nil
            )
          case DomaType
                .Seq(DomaType.EntityOrHolderOrEmbeddable(internalTpe), _) =>
            val command = commandTemplate(
              // 注釈マクロ時は型のメタ情報が見れないためもう一段マクロをかます
              q"domala.internal.macros.reflect.DaoReflectionMacros.getResultListHandler[$trtName, $internalTpe](classOf[$trtName], ${defDecl.name.literal})")
            (
              q"$command.execute().asScala",
              Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[$internalTpe](__query)")
            )

          case DomaType.Map =>
            val command = commandTemplate(
              q"new org.seasar.doma.internal.jdbc.command.MapSingleResultHandler(${selectArgs.mapKeyNaming})")
            (
              q"Option($command.execute()).map(_.asScala.toMap).getOrElse(Map.empty)",
              Nil
            )

          case DomaType.Basic(_, _, wrapperSupplier, _) =>
            val command = commandTemplate(
              q"new org.seasar.doma.internal.jdbc.command.BasicSingleResultHandler($wrapperSupplier, false)")
            (
              q"$command.execute()",
              Nil
            )

          case DomaType.EntityOrHolderOrEmbeddable(tpe) =>
            // 注釈マクロ時は型のメタ情報が見れないためもう一段マクロをかます
            (
              q"domala.internal.macros.reflect.DaoReflectionMacros.getOtherResult[$trtName, $tpe](classOf[$trtName], ${defDecl.name.literal}, getCommandImplementors, __query, $internalMethodName)",
              Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[$tpe](__query)")
            )

          case _ =>
            MacrosHelper.abort(
              Message.DOMALA4008, defDecl.tpe, trtName.syntax, defDecl.name.syntax)
        }

    val enteringParam = defDecl.paramss.flatten.map { p =>
      arg"${Term.Name(p.name.toString)}.asInstanceOf[Object]"
    }

    val addParameters = defDecl.paramss.flatten.map { p =>
      //noinspection ScalaUnusedSymbol
      val paramTpe = p.decltpe.get match {
        case t"Option[$inner]" => inner
        case t"Stream[$parameter] => $_" =>
          t"java.util.function.Function[Stream[$parameter], _]"
        case t"Iterator[$parameter] => $_" =>
          t"java.util.function.Function[Iterator[$parameter], _]"
        case _ => TypeUtil.toType(p.decltpe.get)
      }
      val param = p.decltpe.get match {
        case t"Option[$_]" => q"${Term.Name(p.name.syntax)}.orNull": Term.Arg
        case _ => Term.Name(p.name.syntax): Term.Arg
      }
      q"""__query.addParameter(${p.name.literal}, classOf[$paramTpe], $param)"""
    }

    //noinspection ScalaUnusedSymbol
    val daoParamTypes = defDecl.paramss.flatten.filter(p => p.decltpe.get match {
      case t"Stream[$_] => $_" => false
      case t"Iterator[$_] => $_" => false
      case t"SelectOptions" => false
      case _ => true
    }).map { p =>
      val pType: Type = p.decltpe.get match {
        case tpe => TypeUtil.toType(tpe)
      }
      q"domala.internal.macros.DaoParamClass.apply(${p.name.literal}, classOf[$pType])"
    }

    val setResultStream = if(isStream || isIterator) {
      Seq(q"__query.setResultStream(true)")
    } else {
      Nil
    }

    q"""
    override def ${defDecl.name}= {
      domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[$trtName], ${defDecl.name.literal}, true, false, ${commonArgs.sql}, ..$daoParamTypes)
      entering(${trtName.className}, ${defDecl.name.literal} ..$enteringParam)
      try {
        val __query = new domala.jdbc.query.SqlAnnotationSelectQuery(${commonArgs.sql})
        ..$checkParameter
        __query.setMethod($internalMethodName)
        __query.setConfig(__config)
        ..$setOptions
        ..$setEntityType
        ..$addParameters
        __query.setCallerClassName(${trtName.className})
        __query.setCallerMethodName(${defDecl.name.literal})
        __query.setResultEnsured(${selectArgs.ensureResult})
        __query.setResultMappingEnsured(${selectArgs.ensureResultMapping})
        __query.setFetchType(${selectArgs.fetch})
        __query.setQueryTimeout(${commonArgs.queryTimeOut})
        __query.setMaxRows(${selectArgs.maxRows})
        __query.setFetchSize(${selectArgs.fetchSize})
        __query.setSqlLogType(${commonArgs.sqlLogType})
        ..$setResultStream
        __query.prepare()
        val __result: ${defDecl.tpe} = $result
        __query.complete()
        exiting(${trtName.className}, ${defDecl.name.literal}, __result)
        __result
      } catch {
        case  __e: java.lang.RuntimeException => {
          throwing(${trtName.className}, ${defDecl.name.literal}, __e)
          throw __e
        }
      }
    }
    """
  }

}
