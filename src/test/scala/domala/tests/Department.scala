package domala.tests

import domala.Embeddable

@Embeddable
case class Department(
  departmentId: ID[Department],
  departmentName: String
)
