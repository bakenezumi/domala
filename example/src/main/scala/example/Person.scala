package example

import domala._

@Entity
case class Person(
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(sequence = "person_id_seq")
  id: ID[Person],
  name: Name,
  age: Option[Int],
  address: Address,
  @Column(name = "department_id")
  departmentId: Option[ID[Department]],
  @Version
  version: Int = -1
)
