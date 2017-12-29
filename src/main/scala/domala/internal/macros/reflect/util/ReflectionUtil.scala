package domala.internal.macros.reflect.util

import domala.internal.macros.reflect.ReflectAbortException
import domala.jdbc.entity.{AbstractEntityDesc, EntityCompanion}
import domala.jdbc.holder.AbstractHolderDesc
import org.seasar.doma.jdbc.entity.EmbeddableType
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

  def getEntityDesc[T](classTag: ClassTag[T]): AbstractEntityDesc[T] = {
    getCompanion(classTag).asInstanceOf[EntityCompanion].entityDesc.asInstanceOf[AbstractEntityDesc[T]]
  }

  def getHolderCompanion[T](
      classTag: ClassTag[T]): AbstractHolderDesc[Any, T] = {
    getCompanion(classTag).asInstanceOf[AbstractHolderDesc[Any, T]]
  }

  def getEmbeddableCompanion[T](classTag: ClassTag[T]): EmbeddableType[T] = {
    getCompanion(classTag).asInstanceOf[EmbeddableType[T]]
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
