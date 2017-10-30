package sample

import domala.Entity

@Entity
case class PersonDepartment(
  id: Int,
  name: Name,
  departmentId: ID,
  departmentName: Name
)
