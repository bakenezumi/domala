package example

import domala._
import example.util.prettyPrint

object ExampleApp extends App {
  implicit val config: jdbc.Config = ExampleConfig

  val dao: PersonDao = PersonDao.impl

  def myPrint(title: String, x: Any): Unit = println(s"[$title]: ${prettyPrint(x)}")

  Required {
    dao.create()

    myPrint("initial data", dao.selectAll())

    // new person insert
    val newPerson = Person(
      ID.notAssigned,
      Name("name1"),
      Some(10),
      Address("city1", "street1"),
      Some(ID(1)),
      -1
    )
    myPrint("insert entity", newPerson)
    val inserted = dao.insert(newPerson)
    myPrint("inserted entity", inserted)
    myPrint("data after insert", dao.selectAll())

    // Person(id = 2) update to (age + 1)
    dao.selectById(2).foreach(entity =>
      dao.update(entity.copy(age = entity.age.map(_ + 1)))
    )
    // Person(id = 1) delete
    dao.selectById(1).foreach(entity =>
      dao.delete(entity)
    )
    myPrint("data after modify", dao.selectAll())

    myPrint("like select result", dao.selectByName("n"))
    myPrint("join select result", dao.selectWithDepartmentById(2))

    myPrint("sql interpolation", select"select * from person where id = 2".getMapList)
    myPrint("sql interpolation", select"select /*%expand*/* from person where id = 2".getList[Person])

  }

}
