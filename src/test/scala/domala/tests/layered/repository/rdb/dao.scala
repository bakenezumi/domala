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
  def of(source: domain.Emp): Emp = if (source == null) null else Emp(
    source.id,
    source.name,
    source.age,
    source.version
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

  def save(entities: Seq[domain.Emp]): Array[Int] = saveImpl(entities.map(Emp.of))
  @BatchInsert
  protected def saveImpl(entities: Seq[Emp]): Array[Int]

  def findByIds[R: ClassTag](id: Seq[domain.ID[domain.Emp]])(mapper: Iterator[domain.Emp] => R): R =
    findByIdsImpl[R](id)(mapper)
  @Select("""
SELECT * FROM emp WHERE id IN /* id */()
  """, strategy = SelectType.ITERATOR)
  protected def findByIdsImpl[R: ClassTag](id: Seq[domain.ID[domain.Emp]])(mapper: Iterator[Emp] => R): R

  def findAll[R: ClassTag](mapper: Iterator[domain.Emp] => R): R = findAllImpl[R](mapper)
  @Select("""
SELECT * FROM emp
  """, strategy = SelectType.ITERATOR)
  protected def findAllImpl[R: ClassTag](mapper: Iterator[Emp] => R): R

  def entry(entity: domain.Emp): Int = saveImpl(Seq(Emp.of(entity))).head

}
