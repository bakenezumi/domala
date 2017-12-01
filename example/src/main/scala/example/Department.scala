package example

import domala._

@Entity
case class Department(
  @Id
  id: ID[Department],
  name: Name,
  @Version
  version: Int = -1
)
