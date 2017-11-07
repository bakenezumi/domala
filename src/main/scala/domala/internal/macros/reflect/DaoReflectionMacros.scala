package domala.internal.macros.reflect

import java.lang.reflect.Method
import java.util.Optional

import domala.internal.{WrapIterator, WrapStream}
import domala.internal.macros.reflect.util.{ReflectionUtil, TypeUtil}
import domala.internal.macros.{DaoParam, DaoParamClass}
import domala.jdbc.{BatchResult, Result}
import domala.jdbc.query.EntityAndEntityType
import domala.message.Message
import org.seasar.doma.internal.jdbc.command._
import org.seasar.doma.internal.jdbc.sql.SqlParser
import org.seasar.doma.jdbc.CommandImplementors
import org.seasar.doma.jdbc.entity.{AbstractEntityType, EntityType}
import org.seasar.doma.jdbc.query.AbstractSelectQuery

import scala.language.experimental.macros
import scala.reflect.ClassTag
import scala.reflect.macros.blackbox

object DaoReflectionMacros {

  def getStreamHandlerImpl[T: c.WeakTypeTag, R: c.WeakTypeTag](
      c: blackbox.Context)(f: c.Expr[Stream[T] => R],
                           daoName: c.Expr[String],
                           methodName: c.Expr[String])(
      classTag: c.Expr[ClassTag[T]]): c.Expr[AbstractStreamHandler[T, R]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (TypeUtil.isEntity(c)(tpe)) {
      reify {
        val entity = ReflectionUtil.getEntityCompanion(classTag.splice)
        new EntityStreamHandler(
          entity,
          (p: java.util.stream.Stream[T]) => f.splice.apply(WrapStream.of(p)))
      }
    } else if (TypeUtil.isHolder(c)(tpe)) {
      reify {
        val domain = ReflectionUtil.getHolderCompanion(classTag.splice)
        new DomainStreamHandler(
          domain,
          (p: java.util.stream.Stream[T]) => f.splice.apply(WrapStream.of(p)))
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition,
              Message.DOMALA4245
                .getMessage(tpe.typeSymbol.name, daoNameText, methodNameText))
    }
  }
  def getStreamHandler[T, R](f: Stream[T] => R,
                             daoName: String,
                             methodName: String)(
      implicit classTag: ClassTag[T]): AbstractStreamHandler[T, R] =
    macro getStreamHandlerImpl[T, R]

  def getIteratorHandlerImpl[T: c.WeakTypeTag, R: c.WeakTypeTag](
    c: blackbox.Context)(f: c.Expr[Iterator[T] => R],
    daoName: c.Expr[String],
    methodName: c.Expr[String])(
    classTag: c.Expr[ClassTag[T]]): c.Expr[AbstractStreamHandler[T, R]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (TypeUtil.isEntity(c)(tpe)) {
      reify {
        val entity = ReflectionUtil.getEntityCompanion(classTag.splice)
        new EntityStreamHandler(
          entity,
          (p: java.util.stream.Stream[T]) => f.splice.apply(WrapIterator.of(p)))
      }
    } else if (TypeUtil.isHolder(c)(tpe)) {
      reify {
        val domain = ReflectionUtil.getHolderCompanion(classTag.splice)
        new DomainStreamHandler(
          domain,
          (p: java.util.stream.Stream[T]) => f.splice.apply(WrapIterator.of(p)))
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition,
        Message.DOMALA6012
          .getMessage(tpe.typeSymbol.name, daoNameText, methodNameText))
    }
  }
  def getIteratorHandler[T, R](f: Iterator[T] => R,
    daoName: String,
    methodName: String)(
    implicit classTag: ClassTag[T]): AbstractStreamHandler[T, R] =
    macro getIteratorHandlerImpl[T, R]

  def getResultListHandlerImpl[T: c.WeakTypeTag](
      c: blackbox.Context)(daoName: c.Expr[String], methodName: c.Expr[String])(
      classTag: c.Expr[ClassTag[T]]): c.Expr[AbstractResultListHandler[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (TypeUtil.isEntity(c)(tpe)) {
      reify {
        val entity = ReflectionUtil.getEntityCompanion(classTag.splice)
        new EntityResultListHandler(entity)
      }
    } else if (TypeUtil.isHolder(c)(tpe)) {
      reify {
        val domain = ReflectionUtil.getHolderCompanion(classTag.splice)
        new DomainResultListHandler(domain)
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition,
              Message.DOMALA4007
                .getMessage(tpe.typeSymbol.name, daoNameText, methodNameText))
    }
  }
  def getResultListHandler[T](daoName: String, methodName: String)(
      implicit classTag: ClassTag[T]): AbstractResultListHandler[T] =
    macro getResultListHandlerImpl[T]

  def getOptionalSingleResultHandlerImpl[T: c.WeakTypeTag](c: blackbox.Context)(
      daoName: c.Expr[String],
      methodName: c.Expr[String])(classTag: c.Expr[ClassTag[T]])
    : c.Expr[AbstractSingleResultHandler[Optional[T]]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (TypeUtil.isEntity(c)(tpe)) {
      reify {
        val entity = ReflectionUtil.getEntityCompanion(classTag.splice)
        new OptionalEntitySingleResultHandler(entity)
      }
    } else if (TypeUtil.isHolder(c)(tpe)) {
      reify {
        val domain = ReflectionUtil.getHolderCompanion(classTag.splice)
        new OptionalDomainSingleResultHandler(domain)
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition,
              Message.DOMALA4235
                .getMessage(tpe.typeSymbol.name, daoNameText, methodNameText))
    }
  }
  def getOptionalSingleResultHandler[T](daoName: String, methodName: String)(
      implicit classTag: ClassTag[T]): AbstractSingleResultHandler[
    Optional[T]] = macro getOptionalSingleResultHandlerImpl[T]

  def getOtherResultImpl[T: c.WeakTypeTag](
      c: blackbox.Context)(daoName: c.Expr[String], methodName: c.Expr[String], commandImplementors: c.Expr[CommandImplementors], query: c.Expr[AbstractSelectQuery], method: c.Expr[Method])(
      classTag: c.Expr[ClassTag[T]]): c.Expr[T] = {
    import c.universe._
    val Literal(Constant(daoNameText: String)) = daoName.tree
    val Literal(Constant(methodNameText: String)) = methodName.tree
    val tpe = weakTypeOf[T]
    ResultType.convert(c)(tpe) match {
      case ResultType.Entity(_, _) =>
        reify {
          val entity = ReflectionUtil.getEntityCompanion(classTag.splice)
          val handler = new EntitySingleResultHandler(entity)
          commandImplementors.splice.createSelectCommand(method.splice, query.splice, handler).execute()
        }
      case ResultType.Holder(_, _) =>
        reify {
          val domain = ReflectionUtil.getHolderCompanion(classTag.splice)
          val handler = new DomainSingleResultHandler(domain)
          commandImplementors.splice.createSelectCommand(method.splice, query.splice, handler).execute()
        }
      case ResultType.Seq(_, t) =>
        t match {
          case ResultType.UnSupport(_, tt) if tt =:= typeOf[Any] =>
            c.abort(c.enclosingPosition,
            Message.DOMALA4113.getMessage(tpe.toString, daoNameText, methodNameText))
          case _ =>
            // TODO: 処理できる可能性はまだあるが現在未検査
            c.abort(c.enclosingPosition,
              Message.DOMALA4008.getMessage(tpe.toString, daoNameText, methodNameText))
        }
      case _ =>
        // TODO: 処理できる可能性はまだあるが現在未検査
        c.abort(c.enclosingPosition,
          Message.DOMALA4008.getMessage(tpe.toString, daoNameText, methodNameText))
    }
  }
  def getOtherResult[T](daoName: String, methodName: String, commandImplementors: CommandImplementors, query: AbstractSelectQuery, method: Method)(
      implicit classTag: ClassTag[T]): T =
    macro getOtherResultImpl[T]

  def setEntityTypeImpl[T: c.WeakTypeTag](c: blackbox.Context)(
      query: c.Expr[AbstractSelectQuery])(
      classTag: c.Expr[ClassTag[T]]): c.Expr[Unit] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (TypeUtil.isEntity(c)(tpe)) {
      reify {
        val entity = ReflectionUtil.getEntityCompanion(classTag.splice)
        query.splice.setEntityType(entity)
      }
    } else reify((): Unit) // No operation
  }
  def setEntityType[T](query: AbstractSelectQuery)(
      implicit classTag: ClassTag[T]): Unit = macro setEntityTypeImpl[T]

  def getEntityAndEntityTypeImpl[T: c.WeakTypeTag](c: blackbox.Context)(
      traitName: c.Expr[String],
      methodName: c.Expr[String],
      resultClass: c.Expr[Class[T]],
      params: c.Expr[DaoParam[_]]*): c.Expr[Option[EntityAndEntityType[Any]]] = {
    import c.universe._
    params
      .map {
        case param if TypeUtil.isEntity(c)(param.actualType.typeArgs.head) =>
          if (weakTypeOf[T] =:= weakTypeOf[Int]) Some(param)
          else if (weakTypeOf[T] <:< weakTypeOf[Result[_]]) {
            if (weakTypeOf[T].typeArgs.head =:= param.actualType.typeArgs.head)
              Some(param)
            else None
          } else
            c.abort(c.enclosingPosition,
                    Message.DOMALA4222
                      .getMessage(traitName.tree.toString().tail.init,
                                  methodName.tree.toString().tail.init))
        case _ => None
      }
      .collectFirst {
        case Some(param) =>
          reify {
            val entity = Class
              .forName(param.splice.clazz.getName + "$")
              .getField("MODULE$")
              .get(null)
              .asInstanceOf[AbstractEntityType[Any]]
            Some(
              EntityAndEntityType(param.splice.name,
                                  param.splice.value,
                                  entity))
          }
      }
      .getOrElse(
        if (weakTypeOf[T] =:= weakTypeOf[Int]) reify(None)
        else
          c.abort(c.enclosingPosition,
                 Message.DOMALA4001
                    .getMessage(traitName.tree.toString().tail.init,
                                methodName.tree.toString().tail.init))
      )
  }
  def getEntityAndEntityType[T](
      traitName: String,
      methodName: String,
      resultClass: Class[T],
      params: (DaoParam[_])*): Option[EntityAndEntityType[Any]] =
    macro getEntityAndEntityTypeImpl[T]

  def getBatchEntityTypeImpl[T: c.WeakTypeTag](c: blackbox.Context)(
    traitName: c.Expr[String],
    methodName: c.Expr[String],
    resultClass: c.Expr[Class[T]],
    param: c.Expr[DaoParam[_]]): c.Expr[Option[EntityType[Any]]] = {
    import c.universe._
    if (TypeUtil.isEntity(c)(param.actualType.typeArgs.head)) {
      if (weakTypeOf[T] =:= weakTypeOf[Int]) reify(None)
      else if (weakTypeOf[T] <:< weakTypeOf[BatchResult[_]]) {
        if (weakTypeOf[T].typeArgs.head =:= param.actualType.typeArgs.head)
          reify {
            val entity = Class
              .forName(param.splice.clazz.getName + "$")
              .getField("MODULE$")
              .get(null)
              .asInstanceOf[AbstractEntityType[Any]]
            Some(entity)
          }
        else reify(None)
      } else
        c.abort(c.enclosingPosition,
          Message.DOMALA4223
            .getMessage(traitName.tree.toString().tail.init,
              methodName.tree.toString().tail.init))
    } else reify(None)
  }
  def getBatchEntityType[T](
    traitName: String,
    methodName: String,
    resultClass: Class[T],
    param: (DaoParam[_])): Option[EntityType[Any]] =
  macro getBatchEntityTypeImpl[T]

  def validateParameterAndSqlImpl(c: blackbox.Context)(
    trtName: c.Expr[String],
    defName: c.Expr[String],
    expandable: c.Expr[Boolean],
    populatable: c.Expr[Boolean],
    sql: c.Expr[String],
    params: c.Expr[DaoParamClass[_]]*): c.Expr[Unit] = {
    import c.universe._
    val Literal(Constant(trtNameLiteral: String)) = trtName.tree
    val Literal(Constant(defNameLiteral: String)) = defName.tree
    val Literal(Constant(expandableLiteral: Boolean)) = expandable.tree
    val Literal(Constant(populatableLiteral: Boolean)) = populatable.tree
    val Literal(Constant(sqlLiteral: String)) = sql.tree
    import scala.language.existentials
    val paramTypes = new ReflectionHelper[c.type](c).paramTypes(params)
    paramTypes.foreach {
      case (_, tpe) =>
        ParamType.convert(c)(tpe) match {
          case ParamType.Iterable(_, ParamType.Other(_, t)) if t =:= typeOf[Any] =>
            c.abort(c.enclosingPosition, Message.DOMALA4160.getMessage(trtNameLiteral, defNameLiteral))
          case _ => ()
        }

    }
    val sqlNode = new SqlParser(sqlLiteral).parse()
    val sqlValidator = new SqlValidator[c.type](c)(trtNameLiteral, defNameLiteral, expandableLiteral, populatableLiteral, paramTypes)
    sqlValidator.validate(sqlNode)
    reify(())
  }
  def validateParameterAndSql(trtName: String, defName: String, expandable: Boolean, populatable: Boolean, sql: String, params: (DaoParamClass[_])*): Unit = macro validateParameterAndSqlImpl

  def validateBatchParameterAndSqlImpl(c: blackbox.Context)(
    trtName: c.Expr[String],
    defName: c.Expr[String],
    expandable: c.Expr[Boolean],
    populatable: c.Expr[Boolean],
    sql: c.Expr[String],
    param: c.Expr[DaoParamClass[_]],
    suppress: c.Expr[String]*): c.Expr[Unit] = {
    import c.universe._
    val Literal(Constant(trtNameLiteral: String)) = trtName.tree
    val Literal(Constant(defNameLiteral: String)) = defName.tree
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
            c.abort(c.enclosingPosition, Message.DOMALA4160.getMessage(trtNameLiteral, defNameLiteral))
          case _ => ()
        }

    }
    val sqlNode = new SqlParser(sqlLiteral).parse()
    val sqlValidator = new BatchSqlValidator[c.type](c)(trtNameLiteral, defNameLiteral, expandableLiteral, populatableLiteral, paramTypes, suppressLiterals)
    sqlValidator.validate(sqlNode)
    reify(())
  }
  def validateBatchParameterAndSql(trtName: String, defName: String, expandable: Boolean, populatable: Boolean, sql: String, param: DaoParamClass[_], suppress: String*): Unit = macro validateBatchParameterAndSqlImpl


  def validateAutoModifyParamImpl[T: c.WeakTypeTag](c: blackbox.Context)(
    trtName: c.Expr[String],
    defName: c.Expr[String],
    paramClass: c.Expr[Class[T]]): c.Expr[Unit] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (TypeUtil.isEntity(c)(tpe)) {
      reify(())
    } else {
      c.abort(c.enclosingPosition,
        Message.DOMALA4003
          .getMessage(trtName.tree.toString().tail.init,
            defName.tree.toString().tail.init))
    }
  }
  def validateAutoModifyParam[T](trtName: String, defName: String, paramClass: Class[T]): Unit = macro validateAutoModifyParamImpl[T]

  def validateAutoBatchModifyParamImpl[C: c.WeakTypeTag, T: c.WeakTypeTag](c: blackbox.Context)(
    trtName: c.Expr[String],
    defName: c.Expr[String],
    paramClass: c.Expr[Class[C]],
    internalClass: c.Expr[Class[T]]): c.Expr[Unit] = {
    import c.universe._
    val containerTpe = weakTypeOf[C]
    if (TypeUtil.isIterable(c)(containerTpe)) {
      val tpe = weakTypeOf[T]
      if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
        reify(())
      } else {
        c.abort(c.enclosingPosition,
          Message.DOMALA4043
            .getMessage(trtName.tree.toString().tail.init,
              defName.tree.toString().tail.init))
      }
    } else {
      c.abort(c.enclosingPosition,
        Message.DOMALA4042
          .getMessage(trtName.tree.toString().tail.init,
            defName.tree.toString().tail.init))
    }
  }
  def validateAutoBatchModifyParam[C, T](trtName: String, defName: String, paramClass: Class[C], internalClass: Class[T]): Unit = macro validateAutoBatchModifyParamImpl[C, T]

  private def validatePropertyName[T: c.WeakTypeTag](c: blackbox.Context)(tpe: c.universe.Type, namess: Seq[List[String]], errorMessage: domala.message.Message): c.Expr[Unit] = {
    import c.universe._
    val terms = tpe.members.filter(_.isTerm)
    namess.foreach { names =>
      if(names.isEmpty) c.abort(c.enclosingPosition, namess.toString)
      val term = terms.find(_.name.toString == names.head)
      if(term.isEmpty)
        c.abort(c.enclosingPosition,
          errorMessage.getMessage(names.head, tpe.toString))
      else if(names.length > 1) {
        validatePropertyName(c)(term.get.typeSignature, Seq(names.tail), errorMessage)
      }
    }
    reify(())
  }

  def validateIncludeImpl[T: c.WeakTypeTag](c: blackbox.Context)(
    trtName: c.Expr[String],
    defName: c.Expr[String],
    paramClass: c.Expr[Class[T]],
    includes: c.Expr[String]*): c.Expr[Unit] = {
    import c.universe._
    val includeNames = includes.map { name =>
      val Literal(Constant(nameLiteral: String)) = name.tree
      nameLiteral.split('.').toList
    }
    validatePropertyName(c)(weakTypeOf[T], includeNames, Message.DOMALA4084)
  }
  def validateInclude[T](trtName: String, defName: String, paramClass: Class[T], includes: String*): Unit = macro validateIncludeImpl[T]

  def validateExcludeImpl[T: c.WeakTypeTag](c: blackbox.Context)(
    trtName: c.Expr[String],
    defName: c.Expr[String],
    paramClass: c.Expr[Class[T]],
    excludes: c.Expr[String]*): c.Expr[Unit] = {
    import c.universe._
    val excludeNames = excludes.map { name =>
      val Literal(Constant(nameLiteral: String)) = name.tree
      nameLiteral.split('.').toList
    }
    validatePropertyName(c)(weakTypeOf[T], excludeNames, Message.DOMALA4085)
  }
  def validateExclude[T](trtName: String, defName: String, paramClass: Class[T], excludes: String*): Unit = macro validateExcludeImpl[T]

}
