package domala.tests.dao

import domala._
import domala.jdbc.Config
import domala.tests._
import org.scalatest.{BeforeAndAfter, FunSuite}

class ProtectedDefTestSuite extends FunSuite with BeforeAndAfter {
  private implicit val config: Config = new H2TestConfigTemplate("private_def_test"){}
  val envDao: PersonDao = PersonDao.impl
  val dao: ProtectedDao = ProtectedDao.impl

  test("use internal dao") {
    Required {
      envDao.create()
      val inserted = envDao.batchInsert((10 to 20)
          .map(i => Person(Some(ID(i)), Some(Name("name" + i)), Some(i * 3), Address("city" + i, "street" + i), Some(1), None)))
      val selected = dao.findByIds((10 to 20).map(ID[Person]))
      assert(selected == inserted.entities)
    }
  }
}

@Dao
trait ProtectedDao {

  @Select("select /*%expand*/* from person where id in /* ids */()", strategy = SelectType.ITERATOR)
  protected def findByIdsInternally[R](ids: Seq[ID[Person]])(f: Iterator[Person] => R): R

  def findByIds(ids: Seq[ID[Person]]): List[Person] = {
    ids.grouped(3).flatMap(groupedIds => findByIdsInternally(groupedIds)(_.toList)).toList
  }
}
