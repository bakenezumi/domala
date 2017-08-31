package domala.Integration

import domala._

@Entity
case class PersonDepartment2(
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