package domala.tests.entity

import domala._
import domala.jdbc.{BatchResult, Config, Result}
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
      val newEntity = Holders(id = None, name = Name("AAA"), optionName = Some(Name("BBB")), weight1 = Some(Weight(1)), weight2 = Weight(1000), nested = Holders.Inner("DDD"), enum = EnumA, version = None)
      dao.insert(newEntity)
      val selected1 = dao.selectAll
      assert(selected1 == Seq(
        Holders(Some(ID(1)), Name("AAA"), Some(Name("BBB")), Some(Weight(1)), Weight(1000), Holders.Inner("DDD"), EnumA, Some(Version(1)))
      ))
      dao.update(selected1.head.copy(name = Name("CCC"), weight2 = Weight(1002), nested = Holders.Inner("EEE"), enum = EnumB))
      val selected2 = dao.selectAll
      assert(selected2 == Seq(
        Holders(Some(ID(1)), Name("CCC"), Some(Name("BBB")), Some(Weight(1)), Weight(1002), Holders.Inner("EEE"), EnumB, Some(Version(2)))
      ))
    }
  }

  test("select Holder result") {
    Required {
      val newEntities = Seq(
        Holders(id = None, name = Name("AAA"), optionName = Some(Name("BBB")), weight1 = Some(Weight(1)), weight2 = Weight(1000), nested = Holders.Inner("DDD"), enum = EnumA, version = None),
        Holders(id = None, name = Name("EEE"), optionName = Some(Name("FFF")), weight1 = Some(Weight(2)), weight2 = Weight(2000), nested = Holders.Inner("GGG"), enum = EnumB, version = None)
      )
      dao.insertList(newEntities)
      val w = dao.selectSumKg
      assert(w == Weight(3))
    }
  }

  test("select Holder parameter") {
    Required {
      val newEntities = Seq(
        Holders(id = None, name = Name("AAA"), optionName = Some(Name("BBB")), weight1 = Some(Weight(1)), weight2 = Weight(1000), nested = Holders.Inner("DDD"), enum = EnumA, version = None),
        Holders(id = None, name = Name("EEE"), optionName = Some(Name("FFF")), weight1 = Some(Weight(2)), weight2 = Weight(2000), nested = Holders.Inner("GGG"), enum = EnumA, version = None),
        Holders(id = None, name = Name("HHH"), optionName = Some(Name("III")), weight1 = Some(Weight(3)), weight2 = Weight(3000), nested = Holders.Inner("JJJ"), enum = EnumA, version = None)
      )
      dao.insertList(newEntities)
      assert(dao.selectHeavierThan(Weight(1)) == Seq(Name("EEE"), Name("HHH")))
    }
  }

  test("Numeric holder calculate") {
    Required {
      val newEntities = Seq(
        Holders(id = None, name = Name("AAA"), optionName = Some(Name("BBB")), weight1 = Some(Weight(1)), weight2 = Weight(1000), nested = Holders.Inner("DDD"), enum = EnumA, version = None),
        Holders(id = None, name = Name("EEE"), optionName = Some(Name("FFF")), weight1 = Some(Weight(2)), weight2 = Weight(2000), nested = Holders.Inner("GGG"), enum = EnumA, version = None),
        Holders(id = None, name = Name("HHH"), optionName = Some(Name("III")), weight1 = Some(Weight(3)), weight2 = Weight(3000), nested = Holders.Inner("JJJ"), enum = EnumA, version = None)
      )
      dao.insertList(newEntities)
      val selected = dao.selectAll
      assert(selected.map(_.weight2).sum == Weight(6000))

      // use Ops
      import Numeric.Implicits._
      val sum: Option[Weight[WeightType.Kg]] = for {
        x <- selected(1).weight1
        y <- selected(2).weight1
      } yield x + y

      assert(sum == Some(Weight(5)))
    }
  }

}

@Entity
case class Holders(
  @domala.Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  id : Option[ID] = None,
  name: Name,
  optionName: Option[Name],
  weight1: Option[Weight[WeightType.Kg]],
  weight2: Weight[WeightType.G],
  nested: Holders.Inner,
  enum: LikeEnum,
  @domala.Version
  version: Option[Version] = None
)

object Holders {
  @Holder
  case class Inner(value: String)
}

@Holder
case class ID(value: Int)

@Holder
case class Name(value: String)

@Holder
case class Version(value: Long)

sealed trait WeightType
object WeightType {
  final class Kg extends WeightType
  final class G extends WeightType
}
@Holder
case class Weight[T <: WeightType] private (underlying: BigInt)

@Holder
sealed abstract class LikeEnum(val underlying: String)

case object EnumA extends LikeEnum("A")
case object EnumB extends LikeEnum("B")

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
  enum char(1),
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
select * from holders order by id
  """)
  def selectAll: Seq[Holders]

  @Select(sql="""
select sum(weight1) from holders
  """)
  def selectSumKg: Weight[WeightType.Kg]

  @Select(sql="""
select name from holders where weight1 > /* weight */0 order by name
  """)
  def selectHeavierThan(weight: Weight[WeightType.Kg]): Seq[Name]

  @Insert
  def insert(entity: Holders): Result[Holders]

  @Update
  def update(entity: Holders): Result[Holders]

  @BatchInsert
  def insertList(entity: Seq[Holders]): BatchResult[Holders]
}
