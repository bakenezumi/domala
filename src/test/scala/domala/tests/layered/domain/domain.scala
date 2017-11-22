package domala.tests.layered.domain
import domala.tests.layered.repository.EmpRepository

// entity
trait Emp {
  val id: Option[ID[Emp]]
  val name: Name[Emp]
  val age: Age
  val version: Version
}

// value object
trait ID[T] {
  val value: Int
}

trait  Name[T] {
  val value: String
}

trait Age {
  val value: Int
}

trait Version {
  val value: Int
}

class EmpService(empRepo: EmpRepository) {
  def findByID(id: ID[Emp]): Option[Emp] = {
    empRepo.findByIds(Seq(id)) {
      _.toStream.headOption
    }
  }

  def findAll: List[Emp] = {
    empRepo.findAll {
      _.toList
    }
  }

  def entry(entity: Emp): Int = empRepo.entry(entity)

}
