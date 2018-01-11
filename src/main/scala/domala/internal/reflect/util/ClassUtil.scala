package domala.internal.reflect.util

import java.lang.reflect.Method

import org.seasar.doma.internal.WrapException
import org.seasar.doma.internal.util.AssertionUtil.assertNotNull

object ClassUtil {
  def getDeclaredMethod[T](clazz: Class[T], name: String, parameterTypes: Class[_]*): Method = {
    assertNotNull(clazz, name, parameterTypes, "", "", "")
    try {
      clazz.getDeclaredMethod(name, parameterTypes: _*)
    } catch {
      case e: SecurityException =>
        throw new WrapException(e)
      case e: NoSuchMethodException =>
        val methods = clazz.getDeclaredMethods.filter(m => m.getName == name && m.getParameters.length == parameterTypes.length)
        if(methods.isEmpty || methods.length > 1) throw new WrapException(e)
        else methods.head
    }
  }

}
