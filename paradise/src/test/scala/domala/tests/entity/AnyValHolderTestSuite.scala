package domala.tests.entity

import domala._
import domala.jdbc.{BatchResult, Config, Result}
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}

class AnyValHolderTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: AnyValDao = AnyValDao.impl

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

  test("insert & update AnyVal") {
    Required {
      val newEntity = Values(id = IDValue(1), name = NameValue("AAA"), optionName = Some(NameValue("BBB")), weight1 = Some(WeightValue(1)),  weight2 = WeightValue(1000), nested = Values.Inner("DDD"), enum = EnumHolderA, version = None)
      dao.insert(newEntity)
      val selected1 = dao.selectAll
      assert(selected1 == Seq(
        Values(IDValue(1), NameValue("AAA"), Some(NameValue("BBB")), Some(WeightValue(1)), WeightValue(1000), Values.Inner("DDD"), EnumHolderA, Some(VersionValue(1)))
      ))
      dao.update(selected1.head.copy(name = NameValue("CCC"), weight2 = WeightValue(1002), nested = Values.Inner("EEE"), enum = EnumHolderB))
      val selected2 = dao.selectAll
      assert(selected2 == Seq(
        Values(IDValue(1), NameValue("CCC"), Some(NameValue("BBB")), Some(WeightValue(1)), WeightValue(1002), Values.Inner("EEE"), EnumHolderB, Some(VersionValue(2)))
      ))
    }
  }

  test("select AnyVal result") {
    Required {
      val newEntities = Seq(
        Values(id = IDValue(1), name = NameValue("AAA"), optionName = Some(NameValue("BBB")), weight1 = Some(WeightValue(1)), weight2 = WeightValue(1000), nested = Values.Inner("DDD"), enum = EnumHolderA, version = None),
        Values(id = IDValue(2), name = NameValue("EEE"), optionName = Some(NameValue("FFF")), weight1 = Some(WeightValue(2)), weight2 = WeightValue(2000), nested = Values.Inner("GGG"), enum = EnumHolderB, version = None)
      )
      dao.insertList(newEntities)
      val w = dao.selectSumKg
      assert(w == WeightValue(3))
    }
  }

  test("select AnyVal typed parameter") {
    Required {
      val newEntities = Seq(
        Values(id = IDValue(1), name = NameValue("AAA"), optionName = Some(NameValue("BBB")), weight1 = Some(WeightValue(1)), weight2 = WeightValue(1000), nested = Values.Inner("DDD"), enum = EnumHolderA, version = None),
        Values(id = IDValue(2), name = NameValue("EEE"), optionName = Some(NameValue("FFF")), weight1 = Some(WeightValue(2)), weight2 = WeightValue(2000), nested = Values.Inner("GGG"), enum = EnumHolderA, version = None),
        Values(id = IDValue(3), name = NameValue("HHH"), optionName = Some(NameValue("III")), weight1 = Some(WeightValue(3)), weight2 = WeightValue(3000), nested = Values.Inner("JJJ"), enum = EnumHolderA, version = None)
      )
      dao.insertList(newEntities)
      assert(dao.selectById(IDValue(1)) == Some(Values(id = IDValue(1), name = NameValue("AAA"), optionName = Some(NameValue("BBB")), weight1 = Some(WeightValue(1)),  weight2 = WeightValue(1000), nested = Values.Inner("DDD"), enum = EnumHolderA, version = Some(VersionValue(1)))))
      assert(dao.selectHeavierThan(WeightValue(1)) == Seq(NameValue("EEE"), NameValue("HHH")))
    }
  }

  test("select AnyVal primitive parameter") {
    Required {
      val newEntities = Seq(
        Values(id = IDValue(1), name = NameValue("AAA"), optionName = Some(NameValue("BBB")), weight1 = Some(WeightValue(1)), weight2 = WeightValue(1000), nested = Values.Inner("DDD"), enum = EnumHolderA, version = None),
        Values(id = IDValue(2), name = NameValue("EEE"), optionName = Some(NameValue("FFF")), weight1 = Some(WeightValue(2)), weight2 = WeightValue(2000), nested = Values.Inner("GGG"), enum = EnumHolderA, version = None),
        Values(id = IDValue(3), name = NameValue("HHH"), optionName = Some(NameValue("III")), weight1 = Some(WeightValue(3)), weight2 = WeightValue(3000), nested = Values.Inner("JJJ"), enum = EnumHolderA, version = None)
      )
      dao.insertList(newEntities)
      assert(dao.selectByVersion(VersionValue(1)) == Seq(NameValue("AAA"), NameValue("EEE"), NameValue("HHH")))
    }
  }

  test("select AnyVal Iterator") {
    Required {
      val newEntities = Seq(
        Values(id = IDValue(1), name = NameValue("AAA"), optionName = Some(NameValue("BBB")), weight1 = Some(WeightValue(1)), weight2 = WeightValue(1000), nested = Values.Inner("DDD"), enum = EnumHolderA, version = None),
        Values(id = IDValue(2), name = NameValue("EEE"), optionName = Some(NameValue("FFF")), weight1 = Some(WeightValue(2)), weight2 = WeightValue(2000), nested = Values.Inner("GGG"), enum = EnumHolderA, version = None),
        Values(id = IDValue(3), name = NameValue("HHH"), optionName = Some(NameValue("III")), weight1 = Some(WeightValue(3)), weight2 = WeightValue(3000), nested = Values.Inner("JJJ"), enum = EnumHolderA, version = None)
      )
      dao.insertList(newEntities)
      assert(dao.selectByIds(Seq(IDValue[Values](1), IDValue[Values](3))) {
        _.foldLeft(0) { (acc, x) =>
          acc + {
            val WeightValue(weight) = x.weight2
            weight
          }
        }
      } == 4000)
    }
  }

}

@Entity
case class Values(
  @domala.Id()
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  id: IDValue[Values],
  name: NameValue,
  optionName: Option[NameValue],
  weight1: Option[WeightValue[WeightValueType.Kg]],
  weight2: WeightValue[WeightValueType.G],
  nested: Values.Inner,
  enum: LikeEnumHolder,
  @domala.Version() version: Option[VersionValue]
)
object Values {
  case class Inner(value: String) extends AnyVal
}
case class IDValue[E](value: Long) extends AnyVal

case class NameValue(value: String) extends AnyVal

case class VersionValue(value: Long) extends AnyVal

sealed trait WeightValueType
object WeightValueType {
  final class Kg extends WeightValueType
  final class G extends WeightValueType
}
case class WeightValue[T <: WeightValueType](value: Int) extends AnyVal

@Holder
sealed abstract class LikeEnumHolder(val underlying: String)

case object EnumHolderA extends LikeEnumHolder("A")
case object EnumHolderB extends LikeEnumHolder("B")

@Dao
trait AnyValDao {
  @Script(sql =
    """
create table values(
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
drop table values
    """)
  def drop()

  @Select(sql="""
select * from `values` order by id
  """)
  def selectAll: Seq[Values]

  @Select(sql="""
select * from `values` where id = /*id*/0
  """)
  def selectById(id: IDValue[Values]): Option[Values]


  @Select(sql="""
select sum(weight1) from `values`
  """)
  def selectSumKg: WeightValue[WeightValueType.Kg]

  @Select(sql="""
select name from `values` where weight1 > /* weight */0 order by name
  """)
  def selectHeavierThan(weight: WeightValue[WeightValueType.Kg]): Seq[NameValue]

  @Select(sql="""
select name from `values` where version = /* version */'' order by name
  """)
  def selectByVersion(version: VersionValue): Seq[NameValue]

  @Select(sql="""
select /*%expand*/* from `values` where id in /* id */() order by name
  """, strategy = SelectType.ITERATOR)
  def selectByIds[R](id: Seq[IDValue[Values]])(mapper: Iterator[Values] => R): R

  @Insert
  def insert(entity: Values): Result[Values]

  @Update
  def update(entity: Values): Result[Values]

  @BatchInsert
  def insertList(entity: Seq[Values]): BatchResult[Values]
}
