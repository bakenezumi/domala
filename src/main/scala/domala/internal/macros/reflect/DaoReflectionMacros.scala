package domala.internal.macros.reflect

import java.util.Optional

import domala.internal.macros.DaoParam
import domala.jdbc.Result
import domala.jdbc.query.EntityAndEntityType
import org.seasar.doma.internal.jdbc.command._
import org.seasar.doma.jdbc.command.ResultSetHandler
import org.seasar.doma.jdbc.domain.AbstractDomainType
import org.seasar.doma.jdbc.entity.AbstractEntityType
import org.seasar.doma.jdbc.query.AbstractSelectQuery
import org.seasar.doma.message.Message

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object DaoReflectionMacros {

  def getStreamHandlerImpl[T: c.WeakTypeTag, R: c.WeakTypeTag](c: blackbox.Context)(param: c.Expr[Class[T]], f: c.Expr[Stream[T] => R], daoName: c.Expr[String], methodName: c.Expr[String]): c.Expr[AbstractStreamHandler[T, R]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
      val entity = ReflectionUtil.getCompanion[AbstractEntityType[T]](c)(param)
      reify {
        import scala.compat.java8.StreamConverters._
        new EntityStreamHandler(entity.splice,
          (p: java.util.stream.Stream[T]) => f.splice.apply(p.toScala[Stream]))
      }
    } else if (tpe.companion <:< typeOf[AbstractDomainType[_, _]]){
      val domain = ReflectionUtil.getCompanion[AbstractDomainType[_, T]](c)(param)
      reify {
        import scala.compat.java8.StreamConverters._
        new DomainStreamHandler(domain.splice,
          (p: java.util.stream.Stream[T]) => f.splice.apply(p.toScala[Stream]))
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition, domala.message.Message.DOMALA4245.getMessage(tpe.typeSymbol.name, daoNameText, methodNameText))
    }
  }
  def getStreamHandler[T, R](param: Class[T], f: Stream[T] => R, daoName: String, methodName: String): AbstractStreamHandler[T, R] = macro getStreamHandlerImpl[T, R]

  def getResultListHandlerImpl[T: c.WeakTypeTag](c: blackbox.Context)(param: c.Expr[Class[T]], daoName: c.Expr[String], methodName: c.Expr[String]): c.Expr[AbstractResultListHandler[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
      val entity = ReflectionUtil.getCompanion[AbstractEntityType[T]](c)(param)
      reify {
        new EntityResultListHandler(entity.splice)
      }
    } else if (tpe.companion <:< typeOf[AbstractDomainType[_, _]]){
      val domain = ReflectionUtil.getCompanion[AbstractDomainType[_, T]](c)(param)
      reify {
        new DomainResultListHandler(domain.splice)
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition, domala.message.Message.DOMALA4007.getMessage(tpe.typeSymbol.name, daoNameText, methodNameText))
    }
  }
  def getResultListHandler[T](param: Class[T], daoName: String, methodName: String): AbstractResultListHandler[T] = macro getResultListHandlerImpl[T]

  def getOptionalSingleResultHandlerImpl[T: c.WeakTypeTag](c: blackbox.Context)(param: c.Expr[Class[T]], daoName: c.Expr[String], methodName: c.Expr[String]): c.Expr[AbstractSingleResultHandler[Optional[T]]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
      val entity = ReflectionUtil.getCompanion[AbstractEntityType[T]](c)(param)
      reify {
        new OptionalEntitySingleResultHandler(entity.splice)
      }
    } else if (tpe.companion <:< typeOf[AbstractDomainType[_, _]]){
      val domain = ReflectionUtil.getCompanion[AbstractDomainType[_, T]](c)(param)
      reify {
        new OptionalDomainSingleResultHandler(domain.splice)
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition, domala.message.Message.DOMALA4235.getMessage(tpe.typeSymbol.name, daoNameText, methodNameText))
    }
  }
  def getOptionalSingleResultHandler[T](param: Class[T], daoName: String, methodName: String): AbstractSingleResultHandler[Optional[T]] = macro getOptionalSingleResultHandlerImpl[T]

  def getSingleResultHandlerImpl[T: c.WeakTypeTag](c: blackbox.Context)(param: c.Expr[Class[T]], daoName: c.Expr[String], methodName: c.Expr[String]): c.Expr[AbstractSingleResultHandler[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
      val entity = ReflectionUtil.getCompanion[AbstractEntityType[T]](c)(param)
      reify {
        new EntitySingleResultHandler(entity.splice)
      }
    } else if (tpe.companion <:< typeOf[AbstractDomainType[_, _]]){
      val domain = ReflectionUtil.getCompanion[AbstractDomainType[_, T]](c)(param)
      reify {
        new DomainSingleResultHandler(domain.splice)
      }
    } else {
      val Literal(Constant(daoNameText: String)) = daoName.tree
      val Literal(Constant(methodNameText: String)) = methodName.tree
      c.abort(c.enclosingPosition, Message.DOMA4008.getMessage(tpe.typeSymbol.name, daoNameText, methodNameText))
    }
  }
  def getSingleResultHandler[T](param: Class[T], daoName: String, methodName: String): ResultSetHandler[T] = macro getSingleResultHandlerImpl[T]

  def setEntityTypeImpl[T: c.WeakTypeTag](c: blackbox.Context)(query: c.Expr[AbstractSelectQuery], param: c.Expr[Class[T]]): c.Expr[Unit] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (tpe.companion <:< typeOf[AbstractEntityType[_]]) {
      val entity = ReflectionUtil.getCompanion[AbstractEntityType[T]](c)(param)
      reify {
        query.splice.setEntityType(entity.splice)
      }
    } else reify ((): Unit) // No operation
  }
  def setEntityType[T](query: AbstractSelectQuery, param: Class[T]): Unit = macro setEntityTypeImpl[T]

  def EntityAndEntityTypeInternal[E](param: DaoParam[E]): EntityAndEntityType[E] = {
    val entity = Class.forName(param.clazz.getName + "$").getField("MODULE$").get(null).asInstanceOf[AbstractEntityType[E]]
    EntityAndEntityType(param.name, param.value, entity)
  }

  def getEntityAndEntityTypeImpl[T: c.WeakTypeTag](c: blackbox.Context)(traitName: c.Expr[String], methodName: c.Expr[String], resultClass: c.Expr[Class[T]], params: c.Expr[DaoParam[_]]*): c.Expr[Option[EntityAndEntityType[Any]]] = {
    import c.universe._
    params.map {
      case param if param.actualType.typeArgs.head.companion <:< typeOf[AbstractEntityType[_]] =>
        if(weakTypeOf[T] =:= weakTypeOf[Int])  Some(param)
        else if (weakTypeOf[T] <:< weakTypeOf[Result[_]]) {
          if (weakTypeOf[T].typeArgs.head =:= param.actualType.typeArgs.head) Some(param)
          else None
        } else c.abort(c.enclosingPosition, domala.message.Message.DOMALA4222
          .getMessage(traitName.tree.toString().tail.init, methodName.tree.toString().tail.init))
      case _ => None
    }.collectFirst {
      case Some(param) =>
        reify {
          Some(EntityAndEntityTypeInternal(param.splice).asInstanceOf[EntityAndEntityType[Any]])
        }
    }.getOrElse(
      if(weakTypeOf[T] =:= weakTypeOf[Int]) reify(None)
      else c.abort(c.enclosingPosition, domala.message.Message.DOMALA4001
        .getMessage(traitName.tree.toString().tail.init, methodName.tree.toString().tail.init))
    )
  }
  def getEntityAndEntityType[T](traitName: String, methodName: String, resultClass: Class[T], params: (DaoParam[_])*): Option[EntityAndEntityType[Any]] = macro getEntityAndEntityTypeImpl[T]

}
