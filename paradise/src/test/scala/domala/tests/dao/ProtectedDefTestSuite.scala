package domala.tests.dao

import domala._
import domala.jdbc.Config
import domala.tests._
import domala.tests.models._
import org.scalatest.{BeforeAndAfter, FunSuite}

class ProtectedDefTestSuite extends FunSuite with BeforeAndAfter {
  private implicit val config: Config = new H2TestConfigTemplate("protected_def_test"){}
  val envDao: PersonDao = PersonDao.impl
  val dao: ProtectedDao = ProtectedDao.impl

  test("use internal dao") {
    Required {
      envDao.create()
      envDao.registerInitialDepartment()
      val inserted = envDao.batchInsert((1 to 20)
          .map(i => Person(ID(i), Some(Name("name" + i)), Some(i * 3), Address("city" + i, "street" + i), Some(1), None)))
      val selected = dao.findByIds((10 to 20).map(ID[Person]))
      assert(selected == inserted.entities.filter(_.id >= ID(10)))
    }
  }
}

@Dao
trait ProtectedDao {

  @Select("select /*%expand*/* from person where id in /* ids */() order by id", strategy = SelectType.ITERATOR)
  protected def findByIdsInternally[R](ids: Seq[ID[Person]])(f: Iterator[Person] => R): R

  def findByIds(ids: Seq[ID[Person]]): Seq[Person] = {
    ids.grouped(3).flatMap(groupedIds => findByIdsInternally(groupedIds)(_.toList)).toSeq
  }
}
