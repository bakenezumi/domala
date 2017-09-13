package sample

import domala._

@Entity
case class Person(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  id: Option[Int] = None,
  name: Name,
  age: Option[Int],
  @Embedded
  address: Address,
  departmentId: Option[Int],
  @Version
  version: Option[Int] = Option(-1)
)
