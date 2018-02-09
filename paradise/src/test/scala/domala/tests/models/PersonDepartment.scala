package domala.tests.models

case class PersonDepartment(
  id: ID[Person],
  name: Name,
  departmentId: Option[ID[Department]],
  departmentName:Option[Name]
)
