package domala.internal.reflect.util

import domala.internal.macros.reflect.ReflectAbortException
import domala.jdbc.entity.{EntityCompanion, EntityDesc}
import domala.jdbc.holder.{HolderCompanion, HolderDesc}
import org.seasar.doma.message.MessageResource

import scala.reflect.ClassTag

object ReflectionUtil {

  def getCompanion[T](classTag: ClassTag[T]): Any = {
    Class
      .forName(classTag.runtimeClass.getName + "$", false, classTag.runtimeClass.getClassLoader)
      .getField("MODULE$")
      .get(null)
//    import scala.reflect.runtime.{currentMirror => cm}
//    val classSymbol = cm.classSymbol(classTag.runtimeClass)
//    val moduleSymbol = classSymbol.companion.asModule
//    val moduleMirror = cm.reflectModule(moduleSymbol)
//    moduleMirror.instance
  }

  def getEntityDesc[T](implicit classTag: ClassTag[T]): EntityDesc[T] = {
    getCompanion(classTag).asInstanceOf[EntityCompanion[T]].entityDesc
  }

  def getHolderDesc[T](classTag: ClassTag[T]): HolderDesc[Any, T] = {
    getCompanion(classTag).asInstanceOf[HolderCompanion[Any, T]].holderDesc
  }

  def getCompanion[T](clazz: Class[T]): Any = {
    Class
      .forName(clazz.getName + "$", false, clazz.getClassLoader)
      .getField("MODULE$")
      .get(null)
  }

  def getEntityDesc[T](clazz: Class[T]): EntityDesc[T] = {
    getCompanion(clazz).asInstanceOf[EntityCompanion[T]].entityDesc
  }

  def getHolderDesc[T](clazz: Class[T]): HolderDesc[Any, T] = {
    getCompanion(clazz).asInstanceOf[HolderCompanion[Any, T]].holderDesc
  }


  def extractionClassString(str: String): String = {
    val r = ".*\\[(.*)\\].*".r
    str match {
      case r(x) => x
      case _    => str
    }
  }
  def extractionQuotedString(str: String): String = {
    val r = """.*"(.*)".*""".r
    str match {
      case r(x) => x
      case _    => str
    }
  }

  def abort(message: MessageResource, args: AnyRef*): Nothing = throw new ReflectAbortException(message, null, args: _*)
}
