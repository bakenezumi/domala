package domala.tests.models

case class PersonDepartmentEmbedded(
  id: ID[Person],
  name: Name,
  department: Department
)
