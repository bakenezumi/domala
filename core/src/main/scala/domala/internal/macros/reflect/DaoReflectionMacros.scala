package domala.internal.macros.reflect

import java.lang.reflect.Method
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import domala.internal.jdbc.command.{OptionEntitySingleResultHandler, OptionHolderSingleResultHandler}
import domala.internal.macros.reflect.util._
import domala.internal.macros.{DaoParam, DaoParamClass}
import domala.internal.reflect.util.ReflectionUtil
import domala.internal.{WrapIterator, WrapStream}
import domala.jdbc.`type`.Types
import domala.jdbc.entity.EntityDesc
import domala.jdbc.query.EntityAndEntityDesc
import domala.jdbc.{BatchResult, Result}
import domala.message.Message
import org.seasar.doma.internal.Constants
import org.seasar.doma.internal.jdbc.command._
import org.seasar.doma.internal.jdbc.sql.SqlParser
import org.seasar.doma.jdbc.query.AbstractSelectQuery
import org.seasar.doma.jdbc.{CommandImplementors, JdbcException}

import scala.language.experimental.macros
import scala.reflect.ClassTag
import scala.reflect.macros.blackbox

object DaoReflectionMacros {

  private def handle[D: c.WeakTypeTag, R](c: blackbox.Context)(daoClass: c.Expr[Class[D]], methodName: c.Expr[String])(block: => R): R = try {
    block
  } catch {
    case e: ReflectAbortException =>
      import c.universe._
      val Literal(Constant(methodNameLiteral: String)) = methodName.tree
      c.abort(weakTypeOf[D].member(TermName(methodNameLiteral)).pos, e.getLocalizedMessage)
    case e: JdbcException =>
      import c.universe._
      val Literal(Constant(methodNameLiteral: String)) = methodName.tree
      c.abort(weakTypeOf[D].member(TermName(methodNameLiteral)).pos, e.getLocalizedMessage)
  }

  def getStreamHandlerImpl[D: c.WeakTypeTag, T: c.WeakTypeTag, R: c.WeakTypeTag](
      c: blackbox.Context)(f: c.Expr[Stream[T] => R],
                           daoClass: c.Expr[Class[D]],
                           methodName: c.Expr[String])(
      classTag: c.Expr[ClassTag[T]]): c.Expr[AbstractStreamHandler[T, R]] = handle(c)(daoClass, methodName) {
    import c.universe._
    val daoTpe = weakTypeOf[D]
    val Literal(Constant(methodNameLiteral: String)) = methodName.tree
    val tpe = weakTypeOf[T]
    MacroTypeConverter.of(c).toType(tpe) match {
      case Types.GeneratedEntityType =>
        reify {
          val entityDesc = ReflectionUtil.getEntityDesc(classTag.splice)
          new EntityStreamHandler(
            entityDesc,
            (p: java.util.stream.Stream[T]) => f.splice.apply(WrapStream.of(p)))
        }
      case Types.MacroEntityType =>
        val entityDesc = MacroEntityDescGenerator.get[blackbox.Context, T](c)(tpe)
        reify {
          new EntityStreamHandler(
            entityDesc.splice,
            (p: java.util.stream.Stream[T]) => f.splice.apply(WrapStream.of(p)))
        }
      case Types.GeneratedHolderType(_) =>
        reify {
          val holderDesc = ReflectionUtil.getHolderDesc(classTag.splice)
          new DomainStreamHandler(
            holderDesc,
            (p: java.util.stream.Stream[T]) => f.splice.apply(WrapStream.of(p)))
        }
      case Types.AnyValHolderType(_) =>
        val holderDesc = AnyValHolderDescGenerator.get[blackbox.Context, T](c)(tpe)
        reify {
          new DomainStreamHandler(
            holderDesc.get.splice,
            (p: java.util.stream.Stream[T]) => f.splice.apply(WrapStream.of(p)))
        }
      case _ =>
        ReflectionUtil.abort(Message.DOMALA4245,
          tpe, daoTpe, methodNameLiteral, MacroUtil.getPropertyErrorMessage(c)(tpe))
    }
  }
  def getStreamHandler[D, T, R](f: Stream[T] => R,
                             daoClass: Class[D],
                             methodName: String)(
      implicit classTag: ClassTag[T]): AbstractStreamHandler[T, R] =
    macro getStreamHandlerImpl[D, T, R]

  def getIteratorHandlerImpl[D: c.WeakTypeTag, T: c.WeakTypeTag, R: c.WeakTypeTag](
    c: blackbox.Context)(f: c.Expr[Iterator[T] => R],
    daoClass: c.Expr[Class[D]],
    methodName: c.Expr[String])(
    classTag: c.Expr[ClassTag[T]]): c.Expr[AbstractStreamHandler[T, R]] = handle(c)(daoClass, methodName) {
    import c.universe._
    val daoTpe = weakTypeOf[D]
    val tpe = weakTypeOf[T]
    MacroTypeConverter.of(c).toType(tpe) match {
      case Types.GeneratedEntityType =>
        reify {
          val entityDesc = ReflectionUtil.getEntityDesc(classTag.splice)
          new EntityStreamHandler(
            entityDesc,
            (p: java.util.stream.Stream[T]) => f.splice.apply(WrapIterator.of(p)))
        }
      case Types.MacroEntityType =>
        val entityDesc = MacroEntityDescGenerator.get[blackbox.Context, T](c)(tpe)
        reify {
          new EntityStreamHandler(
            entityDesc.splice,
            (p: java.util.stream.Stream[T]) => f.splice.apply(WrapIterator.of(p)))
        }
      case Types.GeneratedHolderType(_) =>
        reify {
          val holderDesc = ReflectionUtil.getHolderDesc(classTag.splice)
          new DomainStreamHandler(
            holderDesc,
            (p: java.util.stream.Stream[T]) => f.splice.apply(WrapIterator.of(p)))
        }
      case Types.AnyValHolderType(_) =>
        val holderDesc = AnyValHolderDescGenerator.get[blackbox.Context, T](c)(tpe)
        reify {
          new DomainStreamHandler(
            holderDesc.get.splice,
            (p: java.util.stream.Stream[T]) => f.splice.apply(WrapIterator.of(p)))
        }
      case _ =>
        val Literal(Constant(methodNameText: String)) = methodName.tree
        ReflectionUtil.abort(Message.DOMALA6012,
          tpe, daoTpe, methodNameText, MacroUtil.getPropertyErrorMessage(c)(tpe))
    }
  }
  def getIteratorHandler[D, T, R](f: Iterator[T] => R,
    daoClass: Class[D],
    methodName: String)(
    implicit classTag: ClassTag[T]): AbstractStreamHandler[T, R] =
    macro getIteratorHandlerImpl[D, T, R]

  def getResultListHandlerImpl[D: c.WeakTypeTag, T: c.WeakTypeTag](
      c: blackbox.Context)(daoClass: c.Expr[Class[D]], methodName: c.Expr[String])(
      classTag: c.Expr[ClassTag[T]]): c.Expr[AbstractResultListHandler[T]] = handle(c)(daoClass, methodName) {
    import c.universe._
    val daoTpe = weakTypeOf[D]
    val Literal(Constant(methodNameLiteral: String)) = methodName.tree
    val tpe = weakTypeOf[T]
    MacroTypeConverter.of(c).toType(tpe) match {
      case Types.GeneratedEntityType =>
        reify {
          val entityDesc = ReflectionUtil.getEntityDesc(classTag.splice)
          new EntityResultListHandler(entityDesc)
        }
      case Types.MacroEntityType =>
        val entityDesc = MacroEntityDescGenerator.get[blackbox.Context, T](c)(tpe)
        reify {
          new EntityResultListHandler(entityDesc.splice)
        }
      case Types.GeneratedHolderType(_) =>
        reify {
          val holderDesc = ReflectionUtil.getHolderDesc(classTag.splice)
          new DomainResultListHandler(holderDesc)
        }
      case Types.AnyValHolderType(_) =>
        val holderDesc = AnyValHolderDescGenerator.get[blackbox.Context, T](c)(tpe)
        reify {
          new DomainResultListHandler(holderDesc.get.splice)
        }
      case _ =>
        ReflectionUtil.abort(Message.DOMALA4007,
          tpe, daoTpe, methodNameLiteral, MacroUtil.getPropertyErrorMessage(c)(tpe))
    }
  }
  def getResultListHandler[D, T](daoClass: Class[D], methodName: String)(
      implicit classTag: ClassTag[T]): AbstractResultListHandler[T] =
    macro getResultListHandlerImpl[D, T]

  def getOptionSingleResultHandlerImpl[D: c.WeakTypeTag, T: c.WeakTypeTag](c: blackbox.Context)(
      daoClass: c.Expr[Class[D]],
      methodName: c.Expr[String])(classTag: c.Expr[ClassTag[T]])
    : c.Expr[AbstractSingleResultHandler[Option[T]]] = handle(c)(daoClass, methodName) {
    import c.universe._
    val daoTpe = weakTypeOf[D]
    val Literal(Constant(methodNameText: String)) = methodName.tree
    val tpe = weakTypeOf[T]
    MacroTypeConverter.of(c).toType(tpe) match {
      case Types.GeneratedEntityType =>
        reify {
          val entityDesc = ReflectionUtil.getEntityDesc(classTag.splice)
          new OptionEntitySingleResultHandler(entityDesc)
        }
      case Types.MacroEntityType =>
        val entityDesc = MacroEntityDescGenerator.get[blackbox.Context, T](c)(tpe)
        reify {
          new OptionEntitySingleResultHandler(entityDesc.splice)
        }
      case Types.GeneratedHolderType(_) =>
        reify {
          val holderDesc = ReflectionUtil.getHolderDesc(classTag.splice)
          new OptionHolderSingleResultHandler(holderDesc)
        }
      case Types.AnyValHolderType(_) =>
        val holderDesc = AnyValHolderDescGenerator.get[blackbox.Context, T](c)(tpe)
        reify {
          new OptionHolderSingleResultHandler(holderDesc.get.splice)
        }
      case _ =>
        ReflectionUtil.abort(Message.DOMALA4235,
          tpe, daoTpe, methodNameText, MacroUtil.getPropertyErrorMessage(c)(tpe))
    }
  }
  def getOptionSingleResultHandler[D, T](daoClass: Class[D], methodName: String)(
      implicit classTag: ClassTag[T]): AbstractSingleResultHandler[
    Option[T]] = macro getOptionSingleResultHandlerImpl[D, T]

  def getOtherResultImpl[D: c.WeakTypeTag, T: c.WeakTypeTag](
      c: blackbox.Context)(daoClass: c.Expr[Class[D]], methodName: c.Expr[String], commandImplementors: c.Expr[CommandImplementors], query: c.Expr[AbstractSelectQuery], method: c.Expr[Method])(
      classTag: c.Expr[ClassTag[T]]): c.Expr[T] = handle(c)(daoClass, methodName) {
    import c.universe._
    val daoTpe = weakTypeOf[D]
    val Literal(Constant(methodNameLiteral: String)) = methodName.tree
    val tpe = weakTypeOf[T]
    ResultType.convert(c)(tpe) match {
      case ResultType.GeneratedEntity(_, _) =>
        reify {
          val entityDesc = ReflectionUtil.getEntityDesc(classTag.splice)
          val handler = new EntitySingleResultHandler(entityDesc)
          commandImplementors.splice.createSelectCommand(method.splice, query.splice, handler).execute()
        }
      case ResultType.RuntimeEntity(_, _) =>
        val entityDesc = MacroEntityDescGenerator.get[blackbox.Context, T](c)(tpe)
        reify {
          val handler = new EntitySingleResultHandler(entityDesc.splice)
          commandImplementors.splice.createSelectCommand(method.splice, query.splice, handler).execute()
        }
      case ResultType.GeneratedHolder(_, _) =>
        reify {
          val holderDesc = ReflectionUtil.getHolderDesc(classTag.splice)
          val handler = new DomainSingleResultHandler(holderDesc)
          commandImplementors.splice.createSelectCommand(method.splice, query.splice, handler).execute()
        }
      case ResultType.AnyValHolder(_, _) =>
        val holderDesc = AnyValHolderDescGenerator.get[blackbox.Context, T](c)(tpe)
        reify {
          val handler = new DomainSingleResultHandler(holderDesc.get.splice)
          commandImplementors.splice.createSelectCommand(method.splice, query.splice, handler).execute()
        }
      case ResultType.Seq(_, t) =>
        t match {
          case ResultType.UnSupport(_, tt) if tt =:= typeOf[Any] =>
            ReflectionUtil.abort(
              Message.DOMALA4113, tpe, daoTpe, methodNameLiteral)
          case _ =>
            // TODO: 処理できる可能性はまだあるが現在未検査
            ReflectionUtil.abort(Message.DOMALA4008, tpe, daoTpe, methodNameLiteral)
        }
      case _ =>
        // TODO: 処理できる可能性はまだあるが現在未検査
        ReflectionUtil.abort(Message.DOMALA4008, tpe, daoTpe, methodNameLiteral)
    }
  }
  def getOtherResult[D, T](daoClass: Class[D], methodName: String, commandImplementors: CommandImplementors, query: AbstractSelectQuery, method: Method)(
      implicit classTag: ClassTag[T]): T =
    macro getOtherResultImpl[D, T]

  def setEntityTypeImpl[T: c.WeakTypeTag](c: blackbox.Context)(
      query: c.Expr[AbstractSelectQuery])(
      classTag: c.Expr[ClassTag[T]]): c.Expr[Unit] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    MacroTypeConverter.of(c).toType(tpe) match {
      case Types.GeneratedEntityType =>
        reify {
          val entityDesc = ReflectionUtil.getEntityDesc(classTag.splice)
          query.splice.setEntityType(entityDesc)
        }
      case Types.MacroEntityType =>
        val entityDesc = MacroEntityDescGenerator.get[blackbox.Context, T](c)(tpe)
        reify {
          query.splice.setEntityType(entityDesc.splice)
        }
      case _ => reify((): Unit) // No operation
    }
  }
  def setEntityType[T](query: AbstractSelectQuery)(
      implicit classTag: ClassTag[T]): Unit = macro setEntityTypeImpl[T]

  def getEntityAndEntityDescImpl[D: c.WeakTypeTag, T: c.WeakTypeTag](c: blackbox.Context)(
      daoClass: c.Expr[Class[D]],
      methodName: c.Expr[String],
      resultClass: c.Expr[Class[T]],
      params: c.Expr[DaoParam[_]]*): c.Expr[Option[EntityAndEntityDesc[Any]]] = handle(c)(daoClass, methodName) {
    import c.universe._
    val daoTpe = weakTypeOf[D]
    val Literal(Constant(methodNameLiteral: String)) = methodName.tree
    val converter = MacroTypeConverter.of(c)
    val resultType = weakTypeOf[T]
    params
      .map { param =>
        converter.toType(param.actualType.typeArgs.head) match {
          case convertedType if convertedType.isEntity =>
            if (resultType =:= weakTypeOf[Int]) Some(param, convertedType)
            else if (resultType <:< weakTypeOf[Result[_]]) {
              if (resultType.typeArgs.head =:= param.actualType.typeArgs.head)
                Some(param, convertedType)
              else None
            } else
              ReflectionUtil.abort(Message.DOMALA4222,
                daoTpe,
                methodNameLiteral)
          case _ => None
        }
      }
      .collectFirst {
        case Some((param, Types.GeneratedEntityType)) =>
          reify {
            val entityDesc = ReflectionUtil.getEntityDesc(param.splice.tag.asInstanceOf[ClassTag[Any]])
            Some(
              EntityAndEntityDesc(param.splice.name,
                                  param.splice.value,
                                  entityDesc))
          }
        case Some((param, Types.MacroEntityType)) =>
          val entityDesc = MacroEntityDescGenerator.get[blackbox.Context, Any](c)(param.actualType.typeArgs.head)
          reify {
            Some(
              EntityAndEntityDesc(param.splice.name,
                param.splice.value,
                entityDesc.splice.asInstanceOf[EntityDesc[Any]]))
          }
      }
      .getOrElse(
        if (resultType =:= weakTypeOf[Int]) reify(None)
        else
          ReflectionUtil.abort(Message.DOMALA4001,
            daoTpe,
            methodNameLiteral)
      )
  }
  def getEntityAndEntityDesc[D, T](
      daoClass: Class[D],
      methodName: String,
      resultClass: Class[T],
      params: (DaoParam[_])*): Option[EntityAndEntityDesc[Any]] =
    macro getEntityAndEntityDescImpl[D, T]

  def getBatchEntityDescImpl[D: c.WeakTypeTag, T: c.WeakTypeTag](c: blackbox.Context)(
    daoClass: c.Expr[Class[D]],
    methodName: c.Expr[String],
    resultClass: c.Expr[Class[T]],
    param: c.Expr[DaoParam[_]]): c.Expr[Option[EntityDesc[Any]]] = handle(c)(daoClass, methodName) {
    import c.universe._
    val daoTpe = weakTypeOf[D]
    val Literal(Constant(methodNameLiteral: String)) = methodName.tree
    val resultType = weakTypeOf[T]
    val paramType = param.actualType.typeArgs.head.typeArgs.head
    val convertedType = MacroTypeConverter.of(c).toType(paramType)
    if (convertedType.isEntity) {
      if (resultType =:= weakTypeOf[Array[Int]]) reify(None)
      else if (resultType <:< weakTypeOf[BatchResult[_]]) {
        if (resultType.typeArgs.head =:= paramType)
          convertedType match {
            case Types.GeneratedEntityType =>
              val entityDesc: c.Expr[EntityDesc[Any]] = {
                val entityTypeName = paramType.typeSymbol.name.toTypeName
                c.Expr[EntityDesc[Any]](
                  q"""{
                    domala.internal.reflect.util.ReflectionUtil.getEntityDesc[$entityTypeName].asInstanceOf[domala.jdbc.entity.EntityDesc[Any]]
                  }
                  """
                )
              }
              reify {
                Some(entityDesc.splice)
              }
            case Types.MacroEntityType =>
              val entityDesc = MacroEntityDescGenerator.get[blackbox.Context, Any](c)(paramType)
              reify {
                Some(entityDesc.splice.asInstanceOf[EntityDesc[Any]])
              }
            case _ =>
              reify(None)
          }
        else reify(None)
      } else
        ReflectionUtil.abort(
          Message.DOMALA4223,
          daoTpe,
          methodNameLiteral)
    } else {
      if (resultType =:= weakTypeOf[Array[Int]]) reify(None)
      else {
        ReflectionUtil.abort(
          Message.DOMALA4040,
          paramType,
          methodNameLiteral)
      }
    }
  }
  def getBatchEntityDesc[D, T](
    daoClass: Class[D],
    methodName: String,
    resultClass: Class[T],
    param: (DaoParam[_])): Option[EntityDesc[Any]] =
  macro getBatchEntityDescImpl[D, T]

  def validateParameterAndSqlImpl[D: c.WeakTypeTag](c: blackbox.Context)(
    daoClass: c.Expr[Class[D]],
    methodName: c.Expr[String],
    expandable: c.Expr[Boolean],
    populatable: c.Expr[Boolean],
    sql: c.Expr[String],
    params: c.Expr[DaoParamClass[_]]*): c.Expr[Unit] = handle(c)(daoClass, methodName) {
    import c.universe._
    val daoTpe = weakTypeOf[D]
    val Literal(Constant(methodNameLiteral: String)) = methodName.tree
    val Literal(Constant(expandableLiteral: Boolean)) = expandable.tree
    val Literal(Constant(populatableLiteral: Boolean)) = populatable.tree
    val sqlLiteral: String = sql.tree match {
      case Literal (Constant(sqlLiteral: String)) => sqlLiteral
      case _ =>  ReflectionUtil.abort(Message.DOMALA6015, Message.DOMALA9901.getSimpleMessage(daoTpe, methodNameLiteral))
    }
    import scala.language.existentials
    val paramTypes = new ReflectionHelper[c.type](c).paramTypes(params)
    paramTypes.foreach {
      case (_, tpe) =>
        ParamType.convert(c)(tpe) match {
          case ParamType.Iterable(_, ParamType.Other(_, t)) if t =:= typeOf[Any] =>
            ReflectionUtil.abort(Message.DOMALA4160, daoTpe, methodNameLiteral)
          case _ => ()
        }
    }
    val sqlNode = new SqlParser(sqlLiteral).parse()
    val sqlValidator = new SqlValidator[c.type](c)(Message.DOMALA9901.getSimpleMessage(daoTpe, methodNameLiteral), expandableLiteral, populatableLiteral, paramTypes)
    sqlValidator.validate(sqlNode)
    reify(())
  }
  def validateParameterAndSql[D](daoClass: Class[D], methodName: String, expandable: Boolean, populatable: Boolean, sql: String, params: (DaoParamClass[_])*): Unit = macro validateParameterAndSqlImpl[D]

  def getSqlFilePathImpl[D: c.WeakTypeTag](c: blackbox.Context)(
    daoTrt: c.Expr[Class[D]],
    defName: c.Expr[String],
    expandable: c.Expr[Boolean],
    populatable: c.Expr[Boolean],
    isScript: c.Expr[Boolean],
    params: c.Expr[DaoParamClass[_]]*): c.Expr[String] = handle(c)(daoTrt, defName) {
    import c.universe._
    val daoTpe = weakTypeOf[D]

    val Literal(Constant(defNameLiteral: String)) = defName.tree
    val Literal(Constant(expandableLiteral: Boolean)) = expandable.tree
    val Literal(Constant(populatableLiteral: Boolean)) = populatable.tree
    val Literal(Constant(isScriptLiteral: Boolean)) = isScript.tree

    val pathSuffix = if (isScriptLiteral) Constants.SCRIPT_PATH_SUFFIX else Constants.SQL_PATH_SUFFIX
    val fileName = s"${Constants.SQL_PATH_PREFIX}${daoTpe.toString.replace('.', '/')}/$defNameLiteral$pathSuffix"
    val classPaths = c.classPath.map(url => Paths.get(url.toURI).toFile).filter(_.isDirectory)
    val sqlFilePath = classPaths.map(dir => Paths.get(dir.getCanonicalPath + "/" + fileName))
    val sqlFile = sqlFilePath.find(path => Files.exists(path))
    if (sqlFile.isEmpty) {
      ReflectionUtil.abort(Message.DOMALA4019, Message.DOMALA9902.getSimpleMessage(fileName), sqlFilePath.head.toString)
    }
    val sql = new String(Files.readAllBytes(sqlFile.get), StandardCharsets.UTF_8)
    if (sql.trim.isEmpty) {
      ReflectionUtil.abort(Message.DOMALA4020, Message.DOMALA9902.getSimpleMessage(fileName))
    }
    import scala.language.existentials
    val paramTypes = new ReflectionHelper[c.type](c).paramTypes(params)
    paramTypes.foreach {
      case (_, tpe) =>
        ParamType.convert(c)(tpe) match {
          case ParamType.Iterable(_, ParamType.Other(_, t)) if t =:= typeOf[Any] =>
            ReflectionUtil.abort(Message.DOMALA4160, daoTpe, defNameLiteral)
          case _ => ()
        }
    }
    val sqlNode = new SqlParser(sql).parse()
    val sqlValidator = new SqlValidator[c.type](c)(Message.DOMALA9902.getSimpleMessage(fileName), expandableLiteral, populatableLiteral, paramTypes)
    sqlValidator.validate(sqlNode)
    reify(s"${Constants.SQL_PATH_PREFIX}${daoTrt.splice.getName.replace('.', '/')}/${defName.splice}${c.Expr(q"$pathSuffix").splice}")
  }
  def getSqlFilePath[D](daoTrt: Class[D], defName: String, expandable: Boolean, populatable: Boolean, isScript: Boolean, params: (DaoParamClass[_])*): String = macro getSqlFilePathImpl[D]

  def validateBatchParameterAndSqlImpl[D: c.WeakTypeTag](c: blackbox.Context)(
    daoClass: c.Expr[Class[D]],
    methodName: c.Expr[String],
    expandable: c.Expr[Boolean],
    populatable: c.Expr[Boolean],
    sql: c.Expr[String],
    param: c.Expr[DaoParamClass[_]],
    suppress: c.Expr[String]*): c.Expr[Unit] = handle(c)(daoClass, methodName) {
    import c.universe._
    val daoTpe = weakTypeOf[D]
    val Literal(Constant(methodNameLiteral: String)) = methodName.tree
    val Literal(Constant(expandableLiteral: Boolean)) = expandable.tree
    val Literal(Constant(populatableLiteral: Boolean)) = populatable.tree
    val Literal(Constant(sqlLiteral: String)) = sql.tree
    val suppressLiterals = suppress.map { sup =>
      val Literal(Constant(ret: String)) = sup.tree
      val pos = ret.lastIndexOf(".")
      if(pos > 0) ret.substring(pos + 1)
      else ""
    }
    import scala.language.existentials
    val paramTypes = new ReflectionHelper[c.type](c).paramTypes(Seq(param))
    paramTypes.foreach {
      case (_, tpe) =>
        ParamType.convert(c)(tpe) match {
          case ParamType.Iterable(_, ParamType.Other(_, t)) if t =:= typeOf[Any] =>
            ReflectionUtil.abort(Message.DOMALA4160, daoTpe, methodNameLiteral)
          case _ => ()
        }

    }
    val sqlNode = new SqlParser(sqlLiteral).parse()
    val sqlValidator = new BatchSqlValidator[c.type](c)(daoTpe, Message.DOMALA9901.getSimpleMessage(daoTpe, methodNameLiteral), expandableLiteral, populatableLiteral, paramTypes, suppressLiterals)
    sqlValidator.validate(sqlNode)
    reify(())
  }
  def validateBatchParameterAndSql[D](daoClass: Class[D], methodName: String, expandable: Boolean, populatable: Boolean, sql: String, param: DaoParamClass[_], suppress: String*): Unit = macro validateBatchParameterAndSqlImpl[D]


  def validateAutoModifyParamImpl[D: c.WeakTypeTag, T: c.WeakTypeTag](c: blackbox.Context)(
    daoClass: c.Expr[Class[D]],
    methodName: c.Expr[String],
    paramClass: c.Expr[Class[T]])(
    classTag: c.Expr[ClassTag[T]]): c.Expr[EntityDesc[T]] = handle(c)(daoClass, methodName) {
    import c.universe._
    val daoTpe = weakTypeOf[D]
    val Literal(Constant(methodNameLiteral: String)) = methodName.tree
    val tpe = weakTypeOf[T]
    MacroTypeConverter.of(c).toType(tpe) match {
      case Types.GeneratedEntityType =>
        reify(ReflectionUtil.getEntityDesc(classTag.splice))
      case Types.MacroEntityType =>
        val entityDesc = MacroEntityDescGenerator.get[blackbox.Context, T](c)(tpe)
        reify(entityDesc.splice)
      case _ =>
        ReflectionUtil.abort(
          Message.DOMALA4003,
          daoTpe,
          methodNameLiteral)

    }
  }
  def validateAutoModifyParam[D, T](daoClass: Class[D], methodName: String, paramClass: Class[T])(implicit classTag: ClassTag[T]): EntityDesc[T] = macro validateAutoModifyParamImpl[D, T]

  def validateAutoBatchModifyParamImpl[D: c.WeakTypeTag, C: c.WeakTypeTag, T: c.WeakTypeTag](c: blackbox.Context)(
    daoClass: c.Expr[Class[D]],
    methodName: c.Expr[String],
    paramClass: c.Expr[Class[C]],
    internalClass: c.Expr[Class[T]])(
    classTag: c.Expr[ClassTag[T]]): c.Expr[EntityDesc[T]] = handle(c)(daoClass, methodName) {
    import c.universe._
    val daoTpe = weakTypeOf[D]
    val Literal(Constant(methodNameLiteral: String)) = methodName.tree
    val containerTpe = weakTypeOf[C]
    val converter = MacroTypeConverter.of(c)
    if (converter.toType(containerTpe).isIterable) {
      val tpe = weakTypeOf[T]
      converter.toType(tpe) match {
        case Types.GeneratedEntityType =>
          reify(ReflectionUtil.getEntityDesc(classTag.splice))
        case Types.MacroEntityType =>
          val entityDesc = MacroEntityDescGenerator.get[blackbox.Context, T](c)(tpe)
          reify(entityDesc.splice)
        case _ =>
          ReflectionUtil.abort(
            Message.DOMALA4043,
            daoTpe,
            methodNameLiteral)

      }
    } else {
      ReflectionUtil.abort(
        Message.DOMALA4042,
        daoTpe,
        methodNameLiteral)
    }
  }
  def validateAutoBatchModifyParam[D, C, T](daoClass: Class[D], methodName: String, paramClass: Class[C], internalClass: Class[T])(implicit classTag: ClassTag[T]): EntityDesc[T] = macro validateAutoBatchModifyParamImpl[D, C, T]

  private def validatePropertyName[D: c.WeakTypeTag, T: c.WeakTypeTag](c: blackbox.Context)(daoTpe: c.universe.Type, defName: c.Expr[String], tpe: c.universe.Type, namess: Seq[List[String]], errorMessage: domala.message.Message): c.Expr[Unit] = {
    import c.universe._
    val terms = tpe.members.filter(_.isTerm)
    val Literal(Constant(defNameLiteral: String)) = defName.tree
    namess.foreach { names =>
      if(names.isEmpty) c.abort(daoTpe.typeSymbol.pos, namess.toString)
      val term = terms.find(_.name.toString == names.head)
      if(term.isEmpty)
        c.abort(daoTpe.member(TermName(defNameLiteral)).pos,
          errorMessage.getMessage(names.head, tpe.toString, daoTpe, defNameLiteral))
      else if(names.length > 1) {
        validatePropertyName[D, T](c)(daoTpe, defName, term.get.typeSignature, Seq(names.tail), errorMessage)
      }
    }
    reify(())
  }

  def validateIncludeImpl[D: c.WeakTypeTag, T: c.WeakTypeTag](c: blackbox.Context)(
    daoTrt: c.Expr[Class[D]],
    defName: c.Expr[String],
    paramClass: c.Expr[Class[T]],
    includes: c.Expr[String]*): c.Expr[Unit] = {
    import c.universe._
    val includeNames = includes.map { name =>
      val Literal(Constant(nameLiteral: String)) = name.tree
      nameLiteral.split('.').toList
    }
    validatePropertyName[D, T](c)(weakTypeOf[D], defName, weakTypeOf[T], includeNames, Message.DOMALA4084)
  }
  def validateInclude[D, T](daoTrt: Class[D], defName: String, paramClass: Class[T], includes: String*): Unit = macro validateIncludeImpl[D, T]

  def validateExcludeImpl[D: c.WeakTypeTag, T: c.WeakTypeTag](c: blackbox.Context)(
    daoTrt: c.Expr[Class[D]],
    defName: c.Expr[String],
    paramClass: c.Expr[Class[T]],
    excludes: c.Expr[String]*): c.Expr[Unit] = {
    import c.universe._
    val excludeNames = excludes.map { name =>
      val Literal(Constant(nameLiteral: String)) = name.tree
      nameLiteral.split('.').toList
    }
    validatePropertyName[D, T](c)(weakTypeOf[D], defName, weakTypeOf[T], excludeNames, Message.DOMALA4085)
  }
  def validateExclude[D, T](daoTrt: Class[D], defName: String, paramClass: Class[T], excludes: String*): Unit = macro validateExcludeImpl[D, T]

  def findInvalidProperty(c: blackbox.Context)(tpe: c.Type): Option[(String, String)] = {
    import c.universe._

    def inner(names: Seq[String], t: c.Type): Option[(String, String)] = {
      val constructor = t.decl(termNames.CONSTRUCTOR).asMethod
      val params = constructor.paramLists.flatten
      if(params.isEmpty && names.nonEmpty)
        Some((names.reverse.mkString("."), t.toString))
      else
        params.map { p =>
          MacroTypeConverter.of(c).toType(p.typeSignature) match {
            case _: Types.Basic[_] => None
            case _: Types.Holder[_, _] => None
            case Types.Option(_: Types.Basic[_]) => None
            case Types.Option(_: Types.Holder[_, _]) => None
            case _ if p.typeSignature <:< typeOf[Product] => inner(p.name.toString +: names, p.typeSignature)
            case _ => Some(((p.name.toString +: names).reverse.mkString("."), p.typeSignature.toString))
          }
        }.find {
          case None => false
          case _ => true
        }.flatten
    }
    inner(Nil, tpe)
  }

}
