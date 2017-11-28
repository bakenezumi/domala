package example

import domala._

@Entity
case class PersonDepartment(
  @Id
  id: ID[Person],
  name: Name,
  departmentId: ID[Department],
  departmentName: Name
)
