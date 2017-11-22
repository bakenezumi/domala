package domala.tests.layered.repository.rdb

import domala._
import domala.tests.layered.domain
import domala.tests.layered.repository.EmpRepository

import scala.reflect.ClassTag

@Entity
case class Emp(
  @Id
  @GeneratedValue(GenerationType.IDENTITY)
  id: Option[ID[domain.Emp]],
  name: Name[domain.Emp],
  age: Age,
  @domala.Version
  version: Version
) extends domain.Emp
object Emp {
  def of(parent: domain.Emp): Emp = if (parent == null) null else Emp(
    Option(ID.of(parent.id.orNull)),
    Name.of(parent.name),
    Age.of(parent.age),
    Version.of(parent.version),
  )
}

@Holder
case class ID[T](value: Int) extends domain.ID[T]

object ID {
  def of[T](parent: domain.ID[T]): ID[T] = if (parent == null) null else ID[T](parent.value)
}

@Holder
case class Name[T](value: String) extends domain.Name[T]
object Name {
  def of[T](parent: domain.Name[T]): Name[T] = if (parent == null) null else Name[T](parent.value)
}

@Holder
case class Age(value: Int) extends domain.Age
object Age {
  def of(parent: domain.Age): Age = if (parent == null) null else Age(parent.value)
}

@Holder
case class Version(value: Int) extends domain.Version
object Version {
  def of(parent: domain.Version): Version = if (parent == null) null else Version(parent.value)
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
    findByIdsImpl[R](id.map(ID.of))(mapper)
  @Select("""
SELECT * FROM emp WHERE id IN /* id */()
  """, strategy = SelectType.ITERATOR)
  def findByIdsImpl[R: ClassTag](id: Seq[ID[domain.Emp]])(mapper: Iterator[Emp] => R): R

  def findAll[R: ClassTag](mapper: Iterator[domain.Emp] => R): R = findAllImpl[R](mapper)
  @Select("""
SELECT * FROM emp
  """, strategy = SelectType.ITERATOR)
  def findAllImpl[R: ClassTag](mapper: Iterator[Emp] => R): R

  def entry(entity: domain.Emp): Int = loadImpl(Seq(Emp.of(entity))).head

}
