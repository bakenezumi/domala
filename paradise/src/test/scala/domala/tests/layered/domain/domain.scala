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
case class ID[T](value: Int) extends AnyVal
case class Name[T](value: String) extends AnyVal
case class Age(value: Int) extends AnyVal
case class Version(value: Int) extends AnyVal

class EmpService(empRepo: EmpRepository) {
  def findByID(id: ID[Emp]): Option[Emp] = {
    empRepo.findByIds(Seq(id)) {
      _.toStream.headOption
    }
  }

  def findAll: List[Emp] = {
    empRepo.findAll { _.toList }
  }

  def entry(entity: Emp): Int = empRepo.entry(entity)

}
