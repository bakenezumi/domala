package domala.internal.macros
import java.util.Optional

import org.seasar.doma.internal.jdbc.command._
import org.seasar.doma.jdbc.command.ResultSetHandler
import org.seasar.doma.jdbc.domain.AbstractDomainType
import org.seasar.doma.jdbc.entity.AbstractEntityType
import org.seasar.doma.jdbc.query.AbstractSelectQuery
import org.seasar.doma.message.Message

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object DaoReflectionMacros {

  def getCompanion(c: blackbox.Context)(param: c.Expr[Class[_]]): c.universe.Expr[Any] = {
    import c.universe._
    reify {
      Class.forName(param.splice.getName + "$").getField("MODULE$").get(null)
    }
  }

  def getStreamHandlerImpl[T: c.WeakTypeTag, R: c.WeakTypeTag](c: blackbox.Context)(param: c.Expr[Class[T]], f: c.Expr[Stream[T] => R], daoName: c.Expr[String], methodName: c.Expr[String]): c.universe.Expr[AbstractStreamHandler[T, R]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
      val entity = getCompanion(c)(param)
      reify {
        import scala.compat.java8.StreamConverters._
        new EntityStreamHandler(entity.splice.asInstanceOf[AbstractEntityType[T]],
          (p: java.util.stream.Stream[T]) => f.splice.apply(p.toScala[Stream]))
      }
    } else if (tpe.companion <:< typeOf[AbstractDomainType[_, _]]){
      val domain = getCompanion(c)(param)
      reify {
        import scala.compat.java8.StreamConverters._
        new DomainStreamHandler(domain.splice.asInstanceOf[AbstractDomainType[_, T]],
          (p: java.util.stream.Stream[T]) => f.splice.apply(p.toScala[Stream]))
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition, domala.message.Message.DOMALA4245.getMessage(tpe.typeSymbol.name, daoNameText, methodNameText))
    }
  }
  def getStreamHandler[T, R](param: Class[T], f: Stream[T] => R, daoName: String, methodName: String): AbstractStreamHandler[T, R] = macro getStreamHandlerImpl[T, R]

  def getResultListHandlerImpl[T: c.WeakTypeTag](c: blackbox.Context)(param: c.Expr[Class[T]], daoName: c.Expr[String], methodName: c.Expr[String]): c.universe.Expr[AbstractResultListHandler[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
      val entity = getCompanion(c)(param)
      reify {
        new EntityResultListHandler(entity.splice.asInstanceOf[AbstractEntityType[T]])
      }
    } else if (tpe.companion <:< typeOf[AbstractDomainType[_, _]]){
      val domain = getCompanion(c)(param)
      reify {
        new DomainResultListHandler(domain.splice.asInstanceOf[AbstractDomainType[_, T]])
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition, domala.message.Message.DOMALA4008.getMessage(tpe.typeSymbol.name, daoNameText, methodNameText))
    }
  }
  def getResultListHandler[T](param: Class[T], daoName: String, methodName: String): AbstractResultListHandler[T] = macro getResultListHandlerImpl[T]

  def getOptionalSingleResultHandlerImpl[T: c.WeakTypeTag](c: blackbox.Context)(param: c.Expr[Class[T]], daoName: c.Expr[String], methodName: c.Expr[String]): c.universe.Expr[AbstractSingleResultHandler[Optional[T]]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
      val entity = getCompanion(c)(param)
      reify {
        new OptionalEntitySingleResultHandler(entity.splice.asInstanceOf[AbstractEntityType[T]])
      }
    } else if (tpe.companion <:< typeOf[AbstractDomainType[_, _]]){
      val domain = getCompanion(c)(param)
      reify {
        new OptionalDomainSingleResultHandler(domain.splice.asInstanceOf[AbstractDomainType[_, T]])
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition, domala.message.Message.DOMALA4235.getMessage(tpe.typeSymbol.name, daoNameText, methodNameText))
    }
  }
  def getOptionalSingleResultHandler[T](param: Class[T], daoName: String, methodName: String): AbstractSingleResultHandler[Optional[T]] = macro getOptionalSingleResultHandlerImpl[T]

  def getSingleResultHandlerImpl[T: c.WeakTypeTag](c: blackbox.Context)(param: c.Expr[Class[T]], daoName: c.Expr[String], methodName: c.Expr[String]): c.universe.Expr[AbstractSingleResultHandler[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
      val entity = getCompanion(c)(param)
      reify {
        new EntitySingleResultHandler(entity.splice.asInstanceOf[AbstractEntityType[T]])
      }
    } else if (tpe.companion <:< typeOf[AbstractDomainType[_, _]]){
      val domain = getCompanion(c)(param)
      reify {
        new DomainSingleResultHandler(domain.splice.asInstanceOf[AbstractDomainType[_, T]])
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition, Message.DOMA4008.getMessage(tpe.typeSymbol.name, daoNameText, methodNameText))
    }
  }
  def getSingleResultHandler[T](param: Class[T], daoName: String, methodName: String): ResultSetHandler[T] = macro getSingleResultHandlerImpl[T]

  def setEntityTypeImpl[T: c.WeakTypeTag](c: blackbox.Context)(query: c.Expr[AbstractSelectQuery], param: c.Expr[Class[T]]): c.universe.Expr[Unit] = {
    import c.universe._
    if (param.actualType <:< typeOf[AbstractEntityType[_]]) {
      val entity = getCompanion(c)(param)
      reify {
        query.splice.setEntityType(entity.splice.asInstanceOf[AbstractEntityType[T]])
      }
    } else reify ((): Unit) // No operation
  }
  def setEntityType[T](query: AbstractSelectQuery, param: Class[T]): Unit = macro setEntityTypeImpl[T]
}