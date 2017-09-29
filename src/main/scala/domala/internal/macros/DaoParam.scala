package domala.internal.macros

case class DaoParam[T](name: String, value: T, clazz: Class[T])
