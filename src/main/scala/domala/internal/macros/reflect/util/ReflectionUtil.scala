package domala.internal.macros.reflect.util

import domala.internal.macros.reflect.ReflectAbortException
import domala.jdbc.entity.{EmbeddableCompanion, EntityCompanion, EntityDesc}
import domala.jdbc.holder.{HolderCompanion, HolderDesc}
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

  def getEntityDesc[T](classTag: ClassTag[T]): EntityDesc[T] = {
    getCompanion(classTag).asInstanceOf[EntityCompanion[T]].entityDesc
  }

  def getHolderDesc[T](classTag: ClassTag[T]): HolderDesc[Any, T] = {
    getCompanion(classTag).asInstanceOf[HolderCompanion[Any, T]].holderDesc
  }

  def getEmbeddableDesc[T](classTag: ClassTag[T]): EmbeddableType[T] = {
    getCompanion(classTag).asInstanceOf[EmbeddableCompanion[T]].embeddableDesc
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
