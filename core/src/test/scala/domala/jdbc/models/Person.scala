package domala.jdbc.models

import domala._

case class Person(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  id: ID[Person] = ID.notAssigned,
  name: Option[Name],
  age: Option[Int],
  address: Address,
  departmentId: Option[Int],
  @Version
  version: Int = -1
)
