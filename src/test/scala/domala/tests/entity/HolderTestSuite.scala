package domala.tests.entity


import domala._
import domala.jdbc.{Config, Result}
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}

class HolderTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: HolderDao = HolderDao.impl

  before {
    Required {
      dao.create()
    }
  }

  after {
    Required {
      dao.drop()
    }
  }

  test("insert & update Holder") {
    Required {
      val newEntity = Holders(id = None, name = Name("AAA"), optionName = Some(Name("BBB")), weight1 = Some(Weight(1)), weight2 = Weight(1000), nested = Holders.Inner("DDD"), version = None)
      dao.insert(newEntity)
      val selected1 = dao.selectAll
      assert(selected1 == Seq(
        Holders(Some(Id(1)), Name("AAA"), Some(Name("BBB")), Some(Weight(1)), Weight(1000), Holders.Inner("DDD"), Some(Version(1)))
      ))
      dao.update(selected1.head.copy(name = Name("CCC"), weight2 = Weight(1002), nested = Holders.Inner("EEE")))
      val selected2 = dao.selectAll
      assert(selected2 == Seq(
        Holders(Some(Id(1)), Name("CCC"), Some(Name("BBB")), Some(Weight(1)), Weight(1002), Holders.Inner("EEE"), Some(Version(2)))
      ))
    }
  }
}

@Entity
case class Holders(
  @domala.Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  id : Option[Id] = None,
  name: Name,
  optionName: Option[Name],
  weight1: Option[Weight[Kg]],
  weight2: Weight[G],
  nested: Holders.Inner,
  @domala.Version
  version: Option[Version] = None
)

object Holders {
  @Holder
  case class Inner(value: String)
}


@Holder
case class Id(value: Int)

@Holder
case class Name(value: String)

@Holder
case class Version(value: Long)

sealed trait WeightType
class Kg extends WeightType
class G extends WeightType

@Holder
case class Weight[T <: WeightType](value: Int)

@Dao(config = TestConfig)
trait HolderDao {
  @Script(sql =
    """
create table holders(
  id int not null identity primary key,
  name varchar(20),
  option_name varchar(20),
  weight1 int,
  weight2 int,
  nested varchar(20),
  version long not null
);
    """)
  def create()

  @Script(sql =
    """
drop table holders
    """)
  def drop()

  @Select(sql="""
select * from holders
  """)
  def selectAll: Seq[Holders]

  @Insert
  def insert(entity: Holders): Result[Holders]

  @Update
  def update(entity: Holders): Result[Holders]
}