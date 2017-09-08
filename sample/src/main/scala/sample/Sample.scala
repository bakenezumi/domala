package sample

import domala.Required
import domala.jdbc.Config

object Sample extends App {
  implicit val config: Config = SampleConfig

  val dao: PersonDao = PersonDao

  Required {
    dao.create()
    println(dao.selectById(1))
    dao.insert(Person(
      name = Name("name1"),
      age = Some(10),
      address = Address("city1", "street1"),
      departmentId = Some(1)
    ))
    println(dao.selectAll())
    println(dao.selectWithDepartmentById(2))
  }

}
