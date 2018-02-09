package domala.tests.models

import domala.Holder

@Holder
case class ID[T](value: Int) {
  def isAssigned: Boolean = value > 0
  def >=(that: ID[T]): Boolean = value >= that.value
}

object ID{
  def notAssigned[T]: ID[T] = ID[T](-1)
}
