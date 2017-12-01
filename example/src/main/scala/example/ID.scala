package example

import domala.Holder

@Holder
case class ID[T] private (value: Int)

object ID {
  def apply[T](value: Int): ID[T] = {
    if (value < 0) throw new IllegalArgumentException (
      "value should be positive. " + value
    )
    new ID[T](value)
  }
  def notAssigned[T]: ID[T] = new ID[T](-1)
}
