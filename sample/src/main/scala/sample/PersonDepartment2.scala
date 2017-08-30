package sample

import domala._

@Entity
case class PersonDepartment2(
  id: Int,
  name: Name,
  @Embedded
  department: Depertment
)

@Embeddable
case class Depertment(
  departmentId: Option[Int],
  departmentName: String
)