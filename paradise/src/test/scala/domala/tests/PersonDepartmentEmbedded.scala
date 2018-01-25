package domala.tests

import domala._

@Entity
case class PersonDepartmentEmbedded(
  id: Int,
  name: String,
  department: Department
)
