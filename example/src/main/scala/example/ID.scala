package example

import domala.Holder

@Holder
case class ID[+ENTITY](value: Int)

object ID {
  def notAssigned: ID[Nothing] = NOT_ASSIGNED
  object NOT_ASSIGNED extends ID[Nothing](-1)
}
