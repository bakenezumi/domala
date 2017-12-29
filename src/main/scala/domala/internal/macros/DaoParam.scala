package domala.internal.macros

import scala.reflect.ClassTag

case class DaoParam[T](name: String, value: T, clazz: Class[T])(implicit val tag: ClassTag[T])
