package domala.internal.macros.reflect

import java.sql.Connection
import javax.sql.DataSource

import domala.internal.reflect.util.ReflectionUtil
import domala.jdbc.Config
import domala.message.Message

import scala.reflect.ClassTag
import scala.reflect.macros.blackbox

object DaoProviderMacro {

  def get[T: c.WeakTypeTag](c: blackbox.Context)(
    config: c.Expr[Config], classTag: c.Expr[ClassTag[T]]): c.Expr[T] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val companion = tpe.companion
    companion.members
      .find(m =>
        m.isMethod && m.asMethod.name.toString == "impl" && m.asMethod.returnType <:< tpe
          && m.asMethod.paramLists.flatten.head.typeSignature <:< typeOf[Config])
      .getOrElse(ReflectionUtil.abort(
        Message.DOMALA6018, tpe))
    reify {
      val companion = ReflectionUtil.getCompanion(classTag.splice)
      val implMethod = companion.getClass.getMethod("impl", classOf[Config])
      implMethod.invoke(companion, config.splice).asInstanceOf[T]
    }
  }


  def getByConfig[T: c.WeakTypeTag](c: blackbox.Context)(
      config: c.Expr[Config])(classTag: c.Expr[ClassTag[T]]): c.Expr[T] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val companion = tpe.companion
    companion.members
      .find(m =>
        m.isMethod && m.asMethod.name.toString == "impl" && m.asMethod.returnType <:< tpe
          && m.asMethod.paramLists.flatten.head.typeSignature <:< typeOf[Config])
      .getOrElse(ReflectionUtil.abort(
                         Message.DOMALA6018, tpe))
    reify {
      val companion = ReflectionUtil.getCompanion(classTag.splice)
      val implMethod = companion.getClass.getMethod("impl", classOf[Config])
      implMethod.invoke(companion, config.splice).asInstanceOf[T]
    }
  }

  def getByConnection[T: c.WeakTypeTag](c: blackbox.Context)(
      connection: c.Expr[Connection])(
      config: c.Expr[Config],
      classTag: c.Expr[ClassTag[T]]): c.Expr[T] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val companion = tpe.companion
    companion.members
      .find(m =>
        m.isMethod && m.asMethod.name.toString == "impl" && m.asMethod.returnType <:< tpe
          && m.asMethod.paramLists.flatten.head.typeSignature <:< typeOf[Connection])
      .getOrElse(ReflectionUtil.abort(Message.DOMALA6018, tpe))
    reify {
      val companion =
        ReflectionUtil.getCompanion(classTag.splice)
      val implMethod = companion.getClass.getMethod("impl",
                                                    classOf[Connection],
                                                    classOf[Config])
      implMethod
        .invoke(companion, connection.splice, config.splice)
        .asInstanceOf[T]
    }
  }

  def getByDataSource[T: c.WeakTypeTag](c: blackbox.Context)(
      dataSource: c.Expr[DataSource])(
      config: c.Expr[Config],
      classTag: c.Expr[ClassTag[T]]): c.Expr[T] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val companion = tpe.companion
    companion.members
      .find(m =>
        m.isMethod && m.asMethod.name.toString == "impl" && m.asMethod.returnType <:< tpe
          && m.asMethod.paramLists.flatten.head.typeSignature <:< typeOf[DataSource])
      .getOrElse(ReflectionUtil.abort(
        Message.DOMALA6018,tpe))
    reify {
      val companion =
        ReflectionUtil.getCompanion(classTag.splice)
      val implMethod = companion.getClass.getMethod("impl",
                                                    classOf[DataSource],
                                                    classOf[Config])
      implMethod
        .invoke(companion, dataSource.splice, config.splice)
        .asInstanceOf[T]
    }
  }

}
