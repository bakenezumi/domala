package domala.internal.macros.meta.generator

import domala.{Select, SelectType}
import domala.internal.macros.meta.args.DaoMethodCommonArgs
import domala.internal.macros.meta.util.NameConverters._
import domala.internal.macros.meta.{QueryDefDecl, Types}
import domala.internal.macros.meta.util.{MetaHelper, TypeUtil}
import domala.message.Message

import scala.collection.immutable.Seq
import scala.meta._

object SelectGenerator extends DaoMethodGenerator {

  override def annotationClass: Class[Select] = classOf[Select]

  case class SelectArgs(
    common: DaoMethodCommonArgs,
    fetchSize: Term.Arg,
    maxRows: Term.Arg,
    strategy: Term.Arg,
    fetch: Term.Arg,
    ensureResult: Term.Arg,
    ensureResultMapping: Term.Arg,
    mapKeyNaming: Term.Arg
  ) {
    val selectType: SelectType = strategy match {
      case q"SelectType.RETURN" | q"RETURN" => SelectType.RETURN
      case q"SelectType.STREAM" | q"STREAM" => SelectType.STREAM
      case q"SelectType.ITERATOR" | q"ITERATOR" => SelectType.ITERATOR
    }
    val isStream: Boolean = selectType == SelectType.STREAM
    val isIterator: Boolean = selectType == SelectType.ITERATOR
  }

  object SelectArgs {
    def of(args: Seq[Term.Arg], traitName: String, methodName: String
    ): SelectArgs = {
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
        DaoMethodCommonArgs.of(args, traitName, methodName),
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
    val selectArgs = SelectArgs.of(args, trtName.syntax, defDecl.name.syntax)
    if (TypeUtil.isWildcardType(defDecl.tpe))
      MetaHelper.abort(Message.DOMALA4207, defDecl.tpe, trtName.syntax, defDecl.name.syntax)

    val validateParameter: Seq[Stat] = selectArgs.selectType match {
      case SelectType.RETURN => Nil
      case SelectType.STREAM =>
        val functionParams = defDecl.paramss.flatten.filter {
          _.decltpe.get match {
            //noinspection ScalaUnusedSymbol
            case t"Stream[$_] => $_" => true
            case _ => false
          }
        }
        if (functionParams.isEmpty) {
          MetaHelper.abort(Message.DOMALA4247, trtName.syntax, defDecl.name.syntax)
        } else if (functionParams.length > 1) {
          MetaHelper.abort(Message.DOMALA4249, trtName.syntax, defDecl.name.syntax)
        }
        val functionParam = Term.Name(functionParams.head.name.toString)
        Seq(q"if ($functionParam == null) throw new org.seasar.doma.DomaNullPointerException(${functionParam.literal})")
      case SelectType.ITERATOR =>
        val functionParams = defDecl.paramss.flatten.filter {
          _.decltpe.get match {
            //noinspection ScalaUnusedSymbol
            case t"Iterator[$_] => $_" => true
            case _ => false
          }
        }
        if (functionParams.isEmpty) {
          MetaHelper.abort(Message.DOMALA6009, trtName.syntax, defDecl.name.syntax)
        } else if (functionParams.length > 1) {
          MetaHelper.abort(Message.DOMALA6010, trtName.syntax, defDecl.name.syntax)
        }
        val functionParam = Term.Name(functionParams.head.name.toString)
        Seq(q"if ($functionParam == null) throw new org.seasar.doma.DomaNullPointerException(${functionParam.literal})")
    }

    if (defDecl.paramss.flatten.exists(_.decltpe.get match {
      //noinspection ScalaUnusedSymbol
      case t"Stream[$_] => $_" => true
      case _ => false
    }) && !selectArgs.isStream) {
      MetaHelper.abort(Message.DOMALA6019, trtName.syntax, defDecl.name.syntax)
    }
    if (defDecl.paramss.flatten.exists(_.decltpe.get match {
      //noinspection ScalaUnusedSymbol
      case t"Iterator[$_] => $_" => true
      case _ => false
    }) && !selectArgs.isIterator) {
      MetaHelper.abort(Message.DOMALA6020, trtName.syntax, defDecl.name.syntax)
    }

    val setOptions = {
      val optionParameters = defDecl.paramss.flatten.filter { p =>
        p.decltpe.get match {
          case t"SelectOptions" => true
          case _ => false
        }
      }
      if (optionParameters.length > 1) {
        MetaHelper.abort(Message.DOMALA4053, trtName.syntax, defDecl.name.syntax)
      } else if (optionParameters.isEmpty) {
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
      if (selectArgs.isStream) {
        val (functionParamTerm, internalTpe, retTpe) = defDecl.paramss.flatten.find {
            _.decltpe.get match {
              //noinspection ScalaUnusedSymbol
              case t"Stream[$_] => $_" => true
              case x if TypeUtil.isWildcardType(x) =>
                MetaHelper.abort(Message.DOMALA4243, x.children.head.syntax, trtName.syntax, defDecl.name.syntax)
              case _ => false
            }
          }
          .map { p =>
            p.decltpe.get match {
              case t"Stream[$internalTpe] => $retTpe" =>
                (Term.Name(p.name.syntax), internalTpe, retTpe)
            }
          }
          .getOrElse(MetaHelper.abort(Message.DOMALA4244, trtName.syntax, defDecl.name.syntax))
        if (retTpe.toString().trim != defDecl.tpe.toString().trim) {
          MetaHelper.abort(Message.DOMALA4246,
            defDecl.tpe,
            retTpe,
            trtName.syntax,
            defDecl.name.syntax)
        }
        Types.of(internalTpe) match {
          case Types.Map =>
            val command = commandTemplate(
              q"""
              new org.seasar.doma.internal.jdbc.command.MapStreamHandler[$retTpe](${selectArgs.mapKeyNaming},
                p => $functionParamTerm(domala.internal.WrapStream.of(p).map(_.asScala.toMap))
              )""")
            (
              q"$command.execute()",
              Nil
            )
          case Types.Basic(_, convertedType, wrapperSupplier, _) =>
            val command = commandTemplate(
              q"""
              new org.seasar.doma.internal.jdbc.command.BasicStreamHandler[$convertedType, $retTpe](($wrapperSupplier),
                p => $functionParamTerm(domala.internal.WrapStream.of(p).map(x => x: $internalTpe))
              )""")
            (
              q"$command.execute()",
              Nil
            )
          case Types.EntityOrHolderOrEmbeddable(_) =>
            // 注釈マクロ時は型のメタ情報が見れないためもう一段マクロをかます
            val command = commandTemplate(
              q"domala.internal.macros.reflect.DaoReflectionMacros.getStreamHandler($functionParamTerm, classOf[$trtName], ${defDecl.name.literal})")
            (
              q"$command.execute()",
              Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[$internalTpe](__query)")
            )
          case _ =>
            MetaHelper.abort(
              Message.DOMALA4008, defDecl.tpe, trtName.syntax, defDecl.name.syntax)
        }
      } else if (selectArgs.isIterator) {
        val (functionParamTerm, internalTpe, retTpe) = defDecl.paramss.flatten
          .find { p =>
            p.decltpe.get match {
              //noinspection ScalaUnusedSymbol
              case t"Iterator[$_] => $_" => true
              case x if TypeUtil.isWildcardType(x) =>
                MetaHelper.abort(Message.DOMALA4243, x.children.head.syntax, trtName.syntax, defDecl.name.syntax)
              case _ => false
            }
          }
          .map { p =>
            p.decltpe.get match {
              case t"Iterator[$internalTpe] => $retTpe" =>
                (Term.Name(p.name.syntax), internalTpe, retTpe)
            }
          }
          .getOrElse(MetaHelper.abort(Message.DOMALA6011, trtName.syntax, defDecl.name.syntax))
        if (retTpe.toString().trim != defDecl.tpe.toString().trim) {
          MetaHelper.abort(Message.DOMALA4246,
            defDecl.tpe,
            retTpe,
            trtName.syntax,
            defDecl.name.syntax)
        }
        Types.of(internalTpe) match {
          case Types.Map =>
            val command = commandTemplate(
              q"""
              new org.seasar.doma.internal.jdbc.command.MapStreamHandler[$retTpe](${selectArgs.mapKeyNaming},
                p => $functionParamTerm(domala.internal.WrapIterator.of(p).map(_.asScala.toMap))
              )""")
            (
              q"$command.execute()",
              Nil
            )
          case Types.Basic(_, convertedType, wrapperSupplier, _) =>
            val command = commandTemplate(
              q"""
              new org.seasar.doma.internal.jdbc.command.BasicStreamHandler[$convertedType, $retTpe](($wrapperSupplier),
                p => $functionParamTerm(domala.internal.WrapIterator.of(p).map(x => x))
              )""")
            (
              q"$command.execute()",
              Nil
            )
          case Types.EntityOrHolderOrEmbeddable(_) =>
            // 注釈マクロ時は型のメタ情報が見れないためもう一段マクロをかます
            val command = commandTemplate(
              q"domala.internal.macros.reflect.DaoReflectionMacros.getIteratorHandler($functionParamTerm, classOf[$trtName], ${defDecl.name.literal})")
            (
              q"$command.execute()",
              Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[$internalTpe](__query)")
            )
          case _ =>
            MetaHelper.abort(
              Message.DOMALA4008, defDecl.tpe, trtName.syntax, defDecl.name.syntax)
        }
      } else
        Types.of(defDecl.tpe) match {
          case Types.Option(Types.Map, _) =>
            val command = commandTemplate(
              q"new domala.internal.jdbc.command.OptionMapSingleResultHandler(${selectArgs.mapKeyNaming})")
            (
              q"$command.execute().map(x => x.asScala.toMap)",
              Nil
            )
          case Types.Option(Types.Basic(_, _, wrapperSupplier, _), _) =>
            val command = commandTemplate(
              q"new domala.internal.jdbc.command.OptionBasicSingleResultHandler($wrapperSupplier, false)")
            (
              q"$command.execute().map(x => x)",
              Nil
            )
          case Types.Option(Types.EntityOrHolderOrEmbeddable(elementType), _) =>
            // 注釈マクロ時は型のメタ情報が見れないためもう一段マクロをかます
            val command = commandTemplate(
              q"domala.internal.macros.reflect.DaoReflectionMacros.getOptionSingleResultHandler[$trtName, $elementType](classOf[$trtName], ${defDecl.name.literal})")
            (
              q"$command.execute()",
              Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[$elementType](__query)")
            )
          case Types.Option(_, elementTpe) =>
            MetaHelper.abort(
              Message.DOMALA4235, elementTpe, trtName.syntax, defDecl.name.syntax)

          case Types.Seq(Types.Map, _) =>
            val command = commandTemplate(
              q"new org.seasar.doma.internal.jdbc.command.MapResultListHandler(${selectArgs.mapKeyNaming})")
            (
              q"$command.execute().asScala.map(_.asScala.toMap)",
              Nil
            )
          case Types.Seq(
          Types.Basic(originalType, convertedType, wrapperSupplier, _),
          _) =>
            val command = commandTemplate(
              q"""new org.seasar.doma.internal.jdbc.command.BasicResultListHandler(($wrapperSupplier): java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$convertedType]])""")
            (
              q"$command.execute().asScala.map(x => x: $originalType)",
              Nil
            )
          case Types
          .Seq(Types.EntityOrHolderOrEmbeddable(internalTpe), _) =>
            val command = commandTemplate(
              // 注釈マクロ時は型のメタ情報が見れないためもう一段マクロをかます
              q"domala.internal.macros.reflect.DaoReflectionMacros.getResultListHandler[$trtName, $internalTpe](classOf[$trtName], ${defDecl.name.literal})")
            (
              q"$command.execute().asScala",
              Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[$internalTpe](__query)")
            )

          case Types.Map =>
            val command = commandTemplate(
              q"new org.seasar.doma.internal.jdbc.command.MapSingleResultHandler(${selectArgs.mapKeyNaming})")
            (
              q"Option($command.execute()).map(_.asScala.toMap).getOrElse(Map.empty)",
              Nil
            )

          case Types.Basic(_, _, wrapperSupplier, _) =>
            val command = commandTemplate(
              q"new org.seasar.doma.internal.jdbc.command.BasicSingleResultHandler($wrapperSupplier, false)")
            (
              q"$command.execute()",
              Nil
            )

          case Types.EntityOrHolderOrEmbeddable(tpe) =>
            // 注釈マクロ時は型のメタ情報が見れないためもう一段マクロをかます
            (
              q"domala.internal.macros.reflect.DaoReflectionMacros.getOtherResult[$trtName, $tpe](classOf[$trtName], ${defDecl.name.literal}, getCommandImplementors, __query, $internalMethodName)",
              Seq(q"domala.internal.macros.reflect.DaoReflectionMacros.setEntityType[$tpe](__query)")
            )

          case _ =>
            MetaHelper.abort(
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
      q"domala.internal.macros.DaoParamClass(${p.name.literal}, classOf[$pType])"
    }

    val setResultStream = if (selectArgs.isStream || selectArgs.isIterator) {
      Seq(q"__query.setResultStream(true)")
    } else {
      Nil
    }


    val sqlValidator =
      if (selectArgs.common.hasSqlAnnotation) {
        q"domala.internal.macros.reflect.DaoReflectionMacros.validateParameterAndSql(classOf[$trtName], ${defDecl.name.literal}, true, false, ${selectArgs.common.sql}, ..$daoParamTypes)"
      } else q"()"


    val query = if (selectArgs.common.hasSqlAnnotation) {
      q"new domala.jdbc.query.SqlAnnotationSelectQuery(${selectArgs.common.sql})"
    } else {
      q"""new domala.jdbc.query.SqlFileSelectQuery(domala.internal.macros.reflect.DaoReflectionMacros.getSqlFilePath(classOf[$trtName], ${defDecl.name.literal}, true, false, false, ..$daoParamTypes))"""
    }

    q"""
    override def ${defDecl.name}= {
      $sqlValidator
      entering(${trtName.className}, ${defDecl.name.literal} ..$enteringParam)
      try {
        val __query = $query
        ..$validateParameter
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
        __query.setQueryTimeout(${selectArgs.common.queryTimeOut})
        __query.setMaxRows(${selectArgs.maxRows})
        __query.setFetchSize(${selectArgs.fetchSize})
        __query.setSqlLogType(${selectArgs.common.sqlLogType})
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
