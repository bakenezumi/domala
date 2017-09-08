package domala.internal.macros
import org.seasar.doma.internal.jdbc.command.AbstractSingleResultHandler
import org.seasar.doma.jdbc.command.ResultSetHandler
import org.seasar.doma.jdbc.domain.AbstractDomainType
import org.seasar.doma.jdbc.entity.AbstractEntityType
import org.seasar.doma.jdbc.query.AbstractSelectQuery
import org.seasar.doma.message.Message

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object DaoRefrectionMacros {
  def getSingleResultHandlerImpl[T: c.WeakTypeTag](c: Context)(param: c.Expr[Class[T]], daoName: c.Expr[String], methodName: c.Expr[String]): c.universe.Expr[AbstractSingleResultHandler[T]] = {
    import c.universe._
    val wtt = weakTypeOf[T]
    if (wtt.companion <:< typeOf[AbstractEntityType[_]]) {
      reify {
        val entity = Class.forName(param.splice.getName + "$").getField("MODULE$").get(null).asInstanceOf[AbstractEntityType[T]]
        new org.seasar.doma.internal.jdbc.command.EntitySingleResultHandler(entity)
      }
    } else if (wtt.companion <:< typeOf[AbstractDomainType[_, _]]){
      reify {
        val domain = Class.forName(param.splice.getName + "$").getField("MODULE$").get(null).asInstanceOf[AbstractDomainType[_, T]]
        new org.seasar.doma.internal.jdbc.command.DomainSingleResultHandler(domain)
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition, Message.DOMA4008.getMessage(param.actualType, daoNameText, methodNameText))
    }
  }
  def getSingleResultHandler[T](param: Class[T], daoName: String, methodName: String): ResultSetHandler[T] = macro getSingleResultHandlerImpl[T]

  def setEntityTypeImpl[T: c.WeakTypeTag](c: Context)(query: c.Expr[AbstractSelectQuery], param: c.Expr[T]): c.universe.Expr[Unit] = {
    import c.universe._
    if (param.actualType <:< typeOf[AbstractEntityType[_]]) {
      reify {
        val query_ = query.splice
        val param_ = param.splice.asInstanceOf[AbstractEntityType[_]]
        query_.setEntityType(param_)
      }
    } else reify ()
  }
  def setEntityType[T](query: AbstractSelectQuery, param: T): Unit = macro setEntityTypeImpl[T]
}