package domala.tests

import domala._

@Entity
case class Person(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  id: Option[ID[Person]] = None,
  @Column(updatable = false)
  name: Option[Name],
  age: Option[Int],
  address: Address,
  departmentId: Option[Int],
  @Version
  version: Option[Int] = Option(-1)
)
