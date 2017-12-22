package example

import domala._

@Entity
case class PersonDepartment(
  @Id
  id: ID[Person],
  name: Name,
  @Column(name = "department_id")
  departmentId: ID[Department],
  @Column(name = "department_name")
  departmentName: Name
)
