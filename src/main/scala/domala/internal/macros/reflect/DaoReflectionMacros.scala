package domala.internal.macros.reflect

import java.util.Optional

import domala.internal.macros.{DaoParam, DaoParamClass}
import domala.jdbc.Result
import domala.jdbc.query.EntityAndEntityType
import org.seasar.doma.internal.jdbc.command._
import org.seasar.doma.internal.jdbc.sql.SqlParser
import org.seasar.doma.jdbc.command.ResultSetHandler
import org.seasar.doma.jdbc.domain.AbstractDomainType
import org.seasar.doma.jdbc.entity.AbstractEntityType
import org.seasar.doma.jdbc.query.AbstractSelectQuery
import org.seasar.doma.message.Message

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
    if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
      reify {
        import scala.compat.java8.StreamConverters._
        val entity = ReflectionUtil.getEntityCompanion(classTag.splice)
        new EntityStreamHandler(
          entity,
          (p: java.util.stream.Stream[T]) => f.splice.apply(p.toScala[Stream]))
      }
    } else if (tpe.companion <:< typeOf[AbstractDomainType[_, _]]) {
      reify {
        import scala.compat.java8.StreamConverters._
        val domain = ReflectionUtil.getHolderCompanion(classTag.splice)
        new DomainStreamHandler(
          domain,
          (p: java.util.stream.Stream[T]) => f.splice.apply(p.toScala[Stream]))
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition,
              domala.message.Message.DOMALA4245
                .getMessage(tpe.typeSymbol.name, daoNameText, methodNameText))
    }
  }
  def getStreamHandler[T, R](f: Stream[T] => R,
                             daoName: String,
                             methodName: String)(
      implicit classTag: ClassTag[T]): AbstractStreamHandler[T, R] =
    macro getStreamHandlerImpl[T, R]

  def getResultListHandlerImpl[T: c.WeakTypeTag](
      c: blackbox.Context)(daoName: c.Expr[String], methodName: c.Expr[String])(
      classTag: c.Expr[ClassTag[T]]): c.Expr[AbstractResultListHandler[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
      reify {
        val entity = ReflectionUtil.getEntityCompanion(classTag.splice)
        new EntityResultListHandler(entity)
      }
    } else if (tpe.companion <:< typeOf[AbstractDomainType[_, _]]) {
      reify {
        val domain = ReflectionUtil.getHolderCompanion(classTag.splice)
        new DomainResultListHandler(domain)
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition,
              domala.message.Message.DOMALA4007
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
    if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
      reify {
        val entity = ReflectionUtil.getEntityCompanion(classTag.splice)
        new OptionalEntitySingleResultHandler(entity)
      }
    } else if (tpe.companion <:< typeOf[AbstractDomainType[_, _]]) {
      reify {
        val domain = ReflectionUtil.getHolderCompanion(classTag.splice)
        new OptionalDomainSingleResultHandler(domain)
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition,
              domala.message.Message.DOMALA4235
                .getMessage(tpe.typeSymbol.name, daoNameText, methodNameText))
    }
  }
  def getOptionalSingleResultHandler[T](daoName: String, methodName: String)(
      implicit classTag: ClassTag[T]): AbstractSingleResultHandler[
    Optional[T]] = macro getOptionalSingleResultHandlerImpl[T]

  def getSingleResultHandlerImpl[T: c.WeakTypeTag](
      c: blackbox.Context)(daoName: c.Expr[String], methodName: c.Expr[String])(
      classTag: c.Expr[ClassTag[T]]): c.Expr[AbstractSingleResultHandler[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
      reify {
        val entity = ReflectionUtil.getEntityCompanion(classTag.splice)
        new EntitySingleResultHandler(entity)
      }
    } else if (tpe.companion <:< typeOf[AbstractDomainType[_, _]]) {
      reify {
        val domain = ReflectionUtil.getHolderCompanion(classTag.splice)
        new DomainSingleResultHandler(domain)
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition,
              Message.DOMA4008.getMessage(tpe.typeSymbol.name,
                                          daoNameText,
                                          methodNameText))
    }
  }
  def getSingleResultHandler[T](daoName: String, methodName: String)(
      implicit classTag: ClassTag[T]): ResultSetHandler[T] =
    macro getSingleResultHandlerImpl[T]

  def setEntityTypeImpl[T: c.WeakTypeTag](c: blackbox.Context)(
      query: c.Expr[AbstractSelectQuery])(
      classTag: c.Expr[ClassTag[T]]): c.Expr[Unit] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
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
        case param
            if param.actualType.typeArgs.head.companion <:< typeOf[
              AbstractEntityType[_]] =>
          if (weakTypeOf[T] =:= weakTypeOf[Int]) Some(param)
          else if (weakTypeOf[T] <:< weakTypeOf[Result[_]]) {
            if (weakTypeOf[T].typeArgs.head =:= param.actualType.typeArgs.head)
              Some(param)
            else None
          } else
            c.abort(c.enclosingPosition,
                    domala.message.Message.DOMALA4222
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
                  domala.message.Message.DOMALA4001
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

  def validSqlImpl(c: blackbox.Context)(
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
    val sqlNode = new SqlParser(sqlLiteral).parse()
    val sqlValidator = new SqlValidator[c.type](c)(trtNameLiteral, defNameLiteral, expandableLiteral, populatableLiteral, paramTypes)
    sqlValidator.validate(sqlNode)

    //c.abort(c.enclosingPosition, trtNameLiteral + defNameStringLiteral + sqlNode.getChildren.toString)
    reify(())
  }
  def validSql(trtName: String, defName: String, expandable: Boolean, populatable: Boolean, sql: String, params: (DaoParamClass[_])*): Unit = macro validSqlImpl
}
