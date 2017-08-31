package domala.Integration

import domala.Entity

@Entity
case class PersonDepartment(
  id: Int,
  name: Name,
  departmentId: Option[Int],
  departmentName:Option[String]
)
