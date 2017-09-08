package domala.tests

import domala._

@Entity
case class PersonDepartmentEmbedded(
  id: Int,
  name: String,
  @Embedded
  department: Department
)

@Embeddable
case class Department(
  departmentId: Int,
  departmentName: String
)