package domala.internal.macros.reflect

import scala.reflect.macros.blackbox

object ReflectionUtil {
  def getCompanion[R: c.WeakTypeTag](c: blackbox.Context)(param: c.Expr[Class[_]]): c.Expr[R] = {
    import c.universe._
//    import scala.reflect.runtime.{currentMirror => cm}
    reify {
// too slowly..
//      val classSymbol = cm.classSymbol(param.splice)
//      val moduleSymbol = classSymbol.companion.asModule
//      val moduleMirror = cm.reflectModule(moduleSymbol)
//      moduleMirror.instance.asInstanceOf[R]
      Class.forName(param.splice.getName + "$").getField("MODULE$").get(null).asInstanceOf[R]
    }
  }
}
