package domala.internal.jdbc.dao

import java.lang.reflect.Method

import domala.internal.reflect.util.ClassUtil
import org.seasar.doma.internal.WrapException
import org.seasar.doma.internal.util.MethodUtil
import org.seasar.doma.jdbc.DaoMethodNotFoundException

object DaoUtil {
  def getDeclaredMethod[T](clazz: Class[T], name: String, parameterTypes: Class[_]*): Method = try {
    ClassUtil.getDeclaredMethod(clazz, name, parameterTypes: _*)
  } catch {
    case e: WrapException =>
      val signature = MethodUtil.createSignature(name, parameterTypes.toArray)
      throw new DaoMethodNotFoundException(e.getCause, clazz.getName, signature)
  }
}
