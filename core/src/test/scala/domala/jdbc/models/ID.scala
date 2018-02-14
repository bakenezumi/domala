package domala.jdbc.models

case class ID[T](value: Int) extends AnyVal
object ID {
  def notAssigned[T] : ID[T] = ID[T](-1)
}
