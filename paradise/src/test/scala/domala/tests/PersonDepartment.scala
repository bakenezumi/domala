package domala.tests

import domala.Entity

@Entity
case class PersonDepartment(
  id: ID[Person],
  name: Name,
  departmentId: Option[ID[Department]],
  departmentName:Option[Name]
)
