package domala.tests.entity.simple

import domala._
import domala.jdbc.{BatchResult, Config}
import domala.tests.H2TestConfigTemplate
import org.scalatest.{BeforeAndAfter, FunSuite}

class EmbeddedJoinTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = new H2TestConfigTemplate("embedded-join"){}
  val deptDao: DeptDao = DeptDao.impl
  val empDao: EmpDao = EmpDao.impl

  val departments = Seq(
    Dept(ID(1), Name("Salary")),
    Dept(ID(2), Name("Marketing"))
  )

  val employees = Seq(
    EmpEntity(ID(1), Name("foo"), ID(2)),
    EmpEntity(ID(2), Name("bar"), ID(1)),
    EmpEntity(ID(3), Name("baz"), ID(2))
  )

  before {
    Required {
      empDao.create()
    }
  }

  after {
    Required {
      empDao.drop()
    }
  }

  test("select all") {
    Required {
      deptDao.insert(departments)
      empDao.insert(employees)
      assert(empDao.selectAll() == employees.map(e => Emp(e.id, e.name, departments.find(_.id == e.deptId).get)))
    }
  }

}

case class Dept(
  id: ID[Dept],
  name: Name
)

case class Emp(
  id: ID[Emp],
  name: Name,
  dept: Dept
)

@Table(name = "Emp")
case class EmpEntity(
  id: ID[Emp],
  name: Name,
  deptId: ID[Dept]
)


@Dao
trait DeptDao {
  @BatchInsert
  def insert(entity: Seq[Dept]): BatchResult[Dept]
}


@Dao
trait EmpDao {
  @Script(sql = """
create table dept(
  id int serial primary key,
  name varchar(20));

create table emp(
  id int serial primary key,
  name varchar(20),
  dept_id int,
  foreign key(dept_id) references dept(id)
  );
  """)
  def create()

  @Script(sql = """
drop table dept;
drop table emp;
  """)
  def drop()

  @Select("""
select
  emp.id,
  emp.name,
  dept.id as `dept.id`,
  dept.name as `dept.name`
from emp inner join dept
on emp.dept_id = dept.id
order by emp.id
""")
  def selectAll(): Seq[Emp]

  @BatchInsert
  def insert(entity: Seq[EmpEntity]): BatchResult[EmpEntity]

}
