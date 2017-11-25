package domala.tests.layered.repository.rdb

import domala._
import domala.tests.layered.domain
import domala.tests.layered.repository.EmpRepository

import scala.reflect.ClassTag

@Entity
case class Emp(
  @Id
  @GeneratedValue(GenerationType.IDENTITY)
  id: Option[domain.ID[domain.Emp]],
  name: domain.Name[domain.Emp],
  age: domain.Age,
  @domala.Version
  version: domain.Version
) extends domain.Emp
object Emp {
  def of(parent: domain.Emp): Emp = if (parent == null) null else Emp(
    parent.id,
    parent.name,
    parent.age,
    parent.version
  )
}


@Dao
trait EmpDao extends EmpRepository {
  @Script(
    """
CREATE TABLE emp(
    id INT NOT NULL IDENTITY PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    age INT NOT NULL,
    version INT NOT NULL
);
    """)
  def create(): Unit

  @Script(
    """
DROP TABLE emp;
    """)
  def drop(): Unit

  def load(entities: Seq[domain.Emp]): Array[Int] = loadImpl(entities.map(Emp.of))
  @BatchInsert
  def loadImpl(entities: Seq[Emp]): Array[Int]

  def findByIds[R: ClassTag](id: Seq[domain.ID[domain.Emp]])(mapper: Iterator[domain.Emp] => R): R =
    findByIdsImpl[R](id)(mapper)
  @Select("""
SELECT * FROM emp WHERE id IN /* id */()
  """, strategy = SelectType.ITERATOR)
  def findByIdsImpl[R: ClassTag](id: Seq[domain.ID[domain.Emp]])(mapper: Iterator[Emp] => R): R

  def findAll[R: ClassTag](mapper: Iterator[domain.Emp] => R): R = findAllImpl[R](mapper)
  @Select("""
SELECT * FROM emp
  """, strategy = SelectType.ITERATOR)
  def findAllImpl[R: ClassTag](mapper: Iterator[Emp] => R): R

  def entry(entity: domain.Emp): Int = loadImpl(Seq(Emp.of(entity))).head

}
