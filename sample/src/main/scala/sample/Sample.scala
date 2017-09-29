package sample

import domala.Required
import domala.jdbc.Config

object Sample extends App {
  implicit val config: Config = SampleConfig

  val dao: PersonDao = PersonDao.impl

  Required {
    dao.create()
    println(dao.selectAll())
    // new person insert
    val newPerson = Person(
      name = Name("name1"),
      age = Some(10),
      address = Address("city1", "street1"),
      departmentId = Some(1)
    )
    dao.insert(newPerson)
    // Person(id = 2) update to (age + 1)
    dao.selectById(2).foreach(entity =>
      dao.update(entity.copy(age = entity.age.map(_ + 1)))
    )
    // Person(id = 1) delete
    dao.selectById(1).foreach(entity =>
      dao.delete(entity)
    )
    println(dao.selectAll())
    println(dao.selectWithDepartmentById(2))
  }

}
