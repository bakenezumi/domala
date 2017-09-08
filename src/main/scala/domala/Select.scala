package domala

import domala.internal.macros.{DomaType, TypeHelper}

import collection.immutable.Seq
import scala.meta._

class Select(sql: String, strategy: SelectType = SelectType.RETURN) extends scala.annotation.StaticAnnotation

sealed trait SelectType
object SelectType {
  case object RETURN extends SelectType
  case object STREAM extends SelectType
}

object SelectGenerator {
  def generate(trtName: Type.Name, _def: Decl.Def, internalMethodName: Term.Name, args: Seq[Term.Arg]): Defn.Def = {
    val sql = args.collectFirst{case arg"sql = $sql" => sql}.get
    val strategy = args.collectFirst{case arg"strategy = $strategy" => strategy}
    val Decl.Def(mods, name, tparams, paramss, tpe) = _def
    val trtNameStr = trtName.value
    val nameStr = name.value

    val (checkParameter: Seq[Stat], isStream: Boolean) = strategy match {
      case None => (Nil, false)
      case Some(q"SelectType.RETURN") | Some(q"RETURN") => (Nil, false)
      case Some(q"SelectType.STREAM") | Some(q"STREAM") => (Seq {
        val functionParams = paramss.flatten.filter{ p =>
          p.decltpe.get match {
            case t"$_ => $_" => true
            case _ => false
          }
        }
        if (functionParams.isEmpty) {
          abort(_def.pos, org.seasar.doma.message.Message.DOMA4247.getMessage(trtName.value, name.value))
        } else if (functionParams.length > 1) {
          abort(_def.pos, org.seasar.doma.message.Message.DOMA4249.getMessage(trtName.value, name.value))
        }
        val functionParam = Term.Name(functionParams.head.name.toString)
        q"""if ($functionParam == null) throw new org.seasar.doma.DomaNullPointerException(${functionParam.value})"""
      }, true)
    }

    // Todo: 戻りの型の対応を増やす
    val (javaTpe, handler, result, setEntity) =
      if (isStream) {
        val (functionParamTerm, internalTpe, retTpe) = paramss.flatten.find { p =>
          p.decltpe.get match {
            case t"Stream[$_] => $_" => true
            case _ => false
          }
        }.map { p =>
          p.decltpe.get match {
            case t"Stream[$internalTpe] => $retTpe" => (Term.Name(p.name.value), internalTpe, retTpe)
          }
        }.getOrElse(abort(_def.pos, domala.message.Message.DOMALA4244.getMessage(trtName.value, name.value)))
        if(retTpe.toString() != tpe.toString()) {
          abort(_def.pos, org.seasar.doma.message.Message.DOMA4246.getMessage(tpe, retTpe, trtName.value, name.value))
        }
        TypeHelper.convertToDomaType(internalTpe) match {
          case DomaType.Map => (
            retTpe,
            q"""new org.seasar.doma.internal.jdbc.command.MapStreamHandler[$retTpe](org.seasar.doma.MapKeyNamingType.NONE, new java.util.function.Function[java.util.stream.Stream[java.util.Map[String, Object]], $retTpe](){
              def apply(p: java.util.stream.Stream[java.util.Map[String, Object]]) = $functionParamTerm(p.toScala[Stream].map(_.asScala.toMap))
              })""",
            q"__command.execute()",
            Nil
          )
          case DomaType.Basic(_, convertedType, wrapperSupplier) => {
            (
              retTpe,
              q"""new org.seasar.doma.internal.jdbc.command.BasicStreamHandler(($wrapperSupplier): java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$convertedType]],
                new java.util.function.Function[java.util.stream.Stream[$convertedType], $retTpe](){
                def apply(p: java.util.stream.Stream[$convertedType]) = $functionParamTerm(p.toScala[Stream].map(x => x: $internalTpe))
              })""",
              q"__command.execute()",
              Nil
            )
          }
          case _ => { // Entity
            val internalTpeTerm = Term.Name(internalTpe.toString)
            (
              retTpe,
              q"""new org.seasar.doma.internal.jdbc.command.EntityStreamHandler($internalTpeTerm.getSingletonInternal(), new java.util.function.Function[java.util.stream.Stream[$internalTpe], $retTpe](){
                def apply(p: java.util.stream.Stream[$internalTpe]) = $functionParamTerm(p.toScala[Stream])
             })""",
              q"__command.execute()",
              Seq(q"__query.setEntityType($internalTpeTerm.getSingletonInternal())")
            )
          }
        }
      } else {
        TypeHelper.convertToDomaType(tpe) match {
          case DomaType.Option(DomaType.Map) => abort("未実装") // TODO
          case DomaType.Option(DomaType.Basic(_, convertedType, wrapperSupplier)) => (
            t"java.util.Optional[$convertedType]",
            q"new org.seasar.doma.internal.jdbc.command.OptionalBasicSingleResultHandler($wrapperSupplier, false)",
            q"__command.execute().asScala.map(x => x)",
            Nil
          )
          case DomaType.Option(DomaType.Entity(elementType)) => {
            val elementTypeTerm = Term.Name(elementType.toString)
            (
              t"java.util.Optional[$elementType]",
              q"new org.seasar.doma.internal.jdbc.command.OptionalEntitySingleResultHandler($elementTypeTerm.getSingletonInternal())",
              q"__command.execute().asScala",
              Seq(q"__query.setEntityType($elementTypeTerm.getSingletonInternal())")
            )
          }

          case DomaType.Seq(DomaType.Map) => (
            t"java.util.List[java.util.Map[String, Object]]",
            q"new org.seasar.doma.internal.jdbc.command.MapResultListHandler(org.seasar.doma.MapKeyNamingType.NONE)",
            q"__command.execute().asScala.map(_.asScala.toMap)",
            Nil
          )
          case DomaType.Seq(DomaType.Basic(originalType, convertedType, wrapperSupplier)) => {
            (
              t"java.util.List[$convertedType]",
              q"""new org.seasar.doma.internal.jdbc.command.BasicResultListHandler(($wrapperSupplier): java.util.function.Supplier[org.seasar.doma.wrapper.Wrapper[$convertedType]])""",
              q"__command.execute().asScala.map(x => x: $originalType)",
              Nil
            )
          }
          case DomaType.Seq(DomaType.Entity(internalTpe)) => {
            val internalTpeTerm = Term.Name(internalTpe.toString)
            (
              t"java.util.List[$internalTpe]",
              q"new org.seasar.doma.internal.jdbc.command.EntityResultListHandler($internalTpeTerm.getSingletonInternal())",
              q"__command.execute().asScala",
              Seq(q"__query.setEntityType($internalTpeTerm.getSingletonInternal())")
            )
          }

          case DomaType.Map => (
            t"java.util.Map[String, Object]",
            q"new org.seasar.doma.internal.jdbc.command.MapSingleResultHandler(org.seasar.doma.MapKeyNamingType.NONE)",
            q"Option(__command.execute()).map(_.asScala.toMap).getOrElse(null)",
            Nil
          )

          case DomaType.Basic(_, convertedType, wrapperSupplier) =>
            (
              convertedType,
              q"new org.seasar.doma.internal.jdbc.command.BasicSingleResultHandler($wrapperSupplier, false)",
              q"__command.execute()",
              Nil
            )

          case DomaType.Entity(tpe) => {
            val tpeTerm = Term.Name(tpe.toString)
            (
              tpe,
              q"new org.seasar.doma.internal.jdbc.command.EntitySingleResultHandler($tpeTerm.getSingletonInternal())",
              q"__command.execute()",
              Seq(q"__query.setEntityType($tpeTerm.getSingletonInternal())")
            )
          }

          case _ => abort(_def.pos, org.seasar.doma.message.Message.DOMA4008.getMessage(tpe, trtName.value, name.value))
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
      val paramTpe = p.decltpe.get match {
        case t"$container[..$inner]" =>  {
          val placeHolder = inner.map(_ => t"_")
          t"${Type.Name(container.toString)}[..$placeHolder]"
        }
        case t"Stream[$p] => $r" => {
          t"java.util.function.Function[_, _]"
        }
        case _ =>  t"${Type.Name(p.decltpe.get.toString)}"
      }
      q"""__query.addParameter(${p.name.value}, classOf[$paramTpe], ${Term.Name(p.name.value): Term.Arg})"""
    }

    q"""
      override def $name = {
        entering($trtNameStr, $nameStr ..$enteringParam)
        try {
          val __query = new domala.jdbc.query.SqlSelectQuery($sql)
          ..$checkParameter
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
}
