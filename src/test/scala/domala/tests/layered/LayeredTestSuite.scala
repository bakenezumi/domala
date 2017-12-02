package domala.tests.layered

import domala.Required
import domala.jdbc.Config
import domala.tests.TestConfig
import domala.tests.layered.repository.EmpRepository
import org.scalatest.{BeforeAndAfter, FunSuite}
import domala.tests.layered.repository.rdb.EmpDao
import domala.tests.layered.domain.{ID, Name, Age, Version}

class LayeredTestSuite  extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig
  val repo: EmpRepository = EmpDao.impl
  val service: domain.EmpService = new domain.EmpService(repo)

  before {
    Required {
      repo.create()
      repo.save(Seq(
        Emp(None, Name("AAA"), Age(10), Version(1)),
        Emp(None, Name("BBB"), Age(15), Version(1)),
        Emp(None, Name("CCC"), Age(20), Version(1)),
      ))
    }
  }

  after {
    Required {
      repo.drop()
    }
  }

  test("layered") {
    Required {
      assert(service.findByID(ID[domain.Emp](2)).toString == Some(Emp(Some(ID(2)), Name("BBB"), Age(15), Version(1))).toString)
      assert(service.findByID(ID[domain.Emp](99)) == None)
      service.entry(Emp(None, Name("DDD"), Age(25), Version(1)))
      assert(service.findAll.toString == Seq(
        Emp(Some(ID(1)), Name("AAA"), Age(10), Version(1)),
        Emp(Some(ID(2)), Name("BBB"), Age(15), Version(1)),
        Emp(Some(ID(3)), Name("CCC"), Age(20), Version(1)),
        Emp(Some(ID(4)), Name("DDD"), Age(25), Version(1)),
      ).toString)
    }
  }

}

case class Emp(
  id: Option[ID[domain.Emp]],
  name: Name[domain.Emp],
  age: Age,
  version: Version
) extends domain.Emp
