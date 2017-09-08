package domala.internal.macros
import org.seasar.doma.DomaException
import org.seasar.doma.internal.jdbc.command.AbstractSingleResultHandler
import org.seasar.doma.jdbc.command.ResultSetHandler
import org.seasar.doma.jdbc.domain.AbstractDomainType
import org.seasar.doma.jdbc.entity.AbstractEntityType
import org.seasar.doma.jdbc.query.AbstractSelectQuery
import org.seasar.doma.message.Message

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object DaoRefrectionMacros {
  def getSingleResultHandlerImpl[T: c.WeakTypeTag](c: Context)(param: c.Expr[T], daoName: c.Expr[String], methodName: c.Expr[String]): c.universe.Expr[AbstractSingleResultHandler[T]] = {
    import c.universe._
    reify {
      val target = param.splice
      target match {
        case _: AbstractEntityType[_] =>
          val entity = target.asInstanceOf[AbstractEntityType[T]]
          new org.seasar.doma.internal.jdbc.command.EntitySingleResultHandler(entity)
        case _: AbstractDomainType[_, _] =>
          val domain = target.asInstanceOf[AbstractDomainType[_, T]]
          new org.seasar.doma.internal.jdbc.command.DomainSingleResultHandler(domain)
        case _ =>
          throw new DomaException(Message.DOMA4008, target.getClass.getName, daoName.splice, methodName.splice)
      }
    }
  }
  def getSingleResultHandler[T](param: T, daoName: String, methodName: String): ResultSetHandler[T] = macro getSingleResultHandlerImpl[T]

  def setEntityTypeImpl[T: c.WeakTypeTag](c: Context)(query: c.Expr[AbstractSelectQuery], param: c.Expr[T]): c.universe.Expr[Unit] = {
    import c.universe._
    reify {
      val query_ = query.splice
      val param_ = param.splice
      param_ match {
        case p: AbstractEntityType[_] => query_.setEntityType(p)
        case _ => ()
      }
    }
  }

  def setEntityType[T](query: AbstractSelectQuery, param: T): Unit = macro setEntityTypeImpl[T]
}