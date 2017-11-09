package domala.tests.expr

import domala.jdbc.{BatchResult, SelectOptions}
import domala.tests._
import domala._
import org.scalatest.{BeforeAndAfter, FunSuite}


class FunctionTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: jdbc.Config = FunctionTestConfig

  val dao: FunctionDao = FunctionDao.impl
  val emp1 = Emp(ID(1), Name("SMITH"),  Jpy(10), Some(ID(1)))
  val emp2 = Emp(ID(2), Name("ALLEN"),  Jpy(20), Some(ID(2)))
  val emp3 = Emp(ID(3), Name("WARD"),   Jpy(30), Some(ID(1)))
  val emp4 = Emp(ID(4), Name("JONES"),  Jpy(40), Some(ID(2)))
  val emp5 = Emp(ID(5), Name("MARTIN"), Jpy(50), Some(ID(3)))

  before {
    Required {
      dao.create()
      dao.insert(List(emp1, emp2, emp3, emp4, emp5))
    }
  }

  after {
    Required {
      dao.drop()
    }
  }

  test("2 parameter") {
    Required {
      assert(
        dao.twoParameterFunctionSelect("2", "3",  (x, y) => x.toInt + y.toInt).contains(emp1)
      )
    }
  }

  test("default parameter") {
    Required {
      assert(
        dao.defaultParameterFunctionSelect(1).contains(emp2)
      )
    }
  }

  test("type parameter stream") {
    Required {
      assert(
        dao.selectSalaryStreamByDepartmentId(ID(1), _.sum ) == 40
      )
      assert(
        dao.selectSalaryStreamByDepartmentId(ID(2), _.reduce((x, y) => (x + y) / 2)) == 30.0f
      )
    }
  }

  test("type parameter iterator") {
    Required {
      assert(
        dao.selectSalaryIteratorByDepartmentId(ID(1), _.sum ) == 40
      )
      assert(
        dao.selectSalaryIteratorByDepartmentId(ID(2), _.reduce((x, y) => (x + y) / 2)) == 30.0f
      )
    }
  }

  test("Holder sum") {
    Required {
      assert(
        dao.selectAll(SelectOptions.get, _.map(_.salary).sum) == Jpy(150)
      )
    }
  }
}


@Holder
case class Jpy(value: Int)
object Jpy {

  implicit val __num: Numeric[Jpy] = new Numeric[Jpy] {
    override def plus(x: Jpy, y: Jpy): Jpy = Jpy(x.value + y.value)

    override def minus(x: Jpy, y: Jpy): Jpy = Jpy(x.value - y.value)

    override def times(x: Jpy, y: Jpy): Jpy = Jpy(x.value * y.value)

    override def negate(x: Jpy): Jpy = Jpy(-x.value)

    override def fromInt(x: Int): Jpy = Jpy(x)

    override def toInt(x: Jpy): Int = x.value

    override def toLong(x: Jpy): Long = x.value.toLong

    override def toFloat(x: Jpy): Float = x.value.toFloat

    override def toDouble(x: Jpy): Double = x.value.toDouble

    override def compare(x: Jpy, y: Jpy): Int = x.value compare y.value
  }
}

@Entity
case class Emp(
  @Id
  id: ID[Emp],
  name: Name,
  salary: Jpy,
  departmentId: Option[ID[Department]],
)

@Dao
trait FunctionDao {
  @Script("""
create table emp(
  id int not null identity primary key,
  name varchar(20) not null,
  salary int not null,
  department_id int
);
  """)
  def create(): Unit

  @Script("""
drop table emp;
  """)
  def drop(): Unit

  @BatchInsert(batchSize = 1000)
  def insert(entity: List[Emp]): BatchResult[Emp]

  @Select("""
select * from emp
where
id = /* f(x, y) - 4 */0
  """)
  def twoParameterFunctionSelect(x: String, y: String, f: (String, String) => Int): Option[Emp]

  @Select("""
select * from emp
where
id = /* twice(x) */0
  """)
  def defaultParameterFunctionSelect(x: Integer, twice: Integer => Int = _ * 2): Option[Emp]

  @Select("""
select salary from emp where department_id = /* departmentId */0
  """, strategy = SelectType.STREAM)
  def selectSalaryStreamByDepartmentId[R](departmentId: ID[Department], mapper: Stream[BigDecimal] => R): R

  @Select("""
select salary from emp where department_id = /* departmentId */0
  """, strategy = SelectType.ITERATOR)
  def selectSalaryIteratorByDepartmentId[R](departmentId: ID[Department], mapper: Iterator[BigDecimal] => R): R

  @Select("""
select /*%expand*/* from emp
  """, strategy = SelectType.ITERATOR)
  def selectAll[R](option: SelectOptions, mapper: Iterator[Emp] => R): R

}
