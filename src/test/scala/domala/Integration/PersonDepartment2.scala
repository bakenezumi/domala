package domala.Integration

import domala._

@Entity
case class PersonDepartment2(
  id: Int,
  name: Name,
  @Embedded
  department: Department
)

@Embeddable
case class Department(
  departmentId: Option[Int],
  departmentName: String
)