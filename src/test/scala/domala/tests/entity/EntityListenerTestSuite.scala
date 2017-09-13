package domala.tests.entity

import domala._
import domala.jdbc.Config
import domala.jdbc.Result
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.jdbc.entity._

class EntityListenerTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig
  val dao: ListenedDao = ListenedDao
  val logDao: ListenLogDao = ListenLogDao
  test("listener test") {
    Required {
      dao.create()
      dao.insert(Listened(name = "A")) //preInsert twice
      dao.update(dao.select) //preUpdate thrice
      val entity = dao.select
      assert(entity.name === "AAAAAA")
      dao.delete(entity)
      val log = logDao.select
      assert(
        log === Seq(
          ListenLog(Some(1),"preInsert","A"),
          ListenLog(Some(2),"postInsert","AA"),
          ListenLog(Some(3),"preUpdate","AA"),
          ListenLog(Some(4),"postUpdate","AAAAAA"),
          ListenLog(Some(5),"preDelete","AAAAAA"),
          ListenLog(Some(6),"postDelete","AAAAAA")
        )
      )
    }
  }
}

@Entity(listener = classOf[MyListener])
case class Listened(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  id: Option[Int] = None,
  name: String,
)

@Dao(config = TestConfig)
trait ListenedDao {
  @Script(sql = """
create table listened(
    id int not null identity primary key,
    name varchar(20)
);

create table listen_log(
    id int not null identity primary key,
    operation varchar(20),
    entity_name  varchar(20)
);
  """)
  def create(): Unit

  @Insert
  def insert(entity: Listened): Result[Listened]

  @Update
  def update(entity: Listened): Result[Listened]

  @Select(sql=
  """
select * from listened
  """
  )
  def select: Listened


  @Delete
  def delete(entity: Listened): Result[Listened]
}

@Entity
case class ListenLog(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  id: Option[Int] = None,
  operation: String,
  entityName: String,
)


@Dao(config = TestConfig)
trait ListenLogDao {
  @Insert
  def log(entity: ListenLog): Result[ListenLog]

  @Select(sql=
    """
select * from listen_log
  """
  )
  def select: Seq[ListenLog]
}

class MyListener extends EntityListener[Listened] {
  val dao: ListenLogDao = ListenLogDao

  override def preInsert(entity: Listened, context: PreInsertContext[Listened]): Unit = {
   val log = ListenLog(operation = "preInsert", entityName = entity.name)
    dao.log(log)
    context.setNewEntity(entity.copy(name = entity.name * 2))
  }

  override def preUpdate(entity: Listened, context: PreUpdateContext[Listened]): Unit = {
    val log = ListenLog(operation = "preUpdate", entityName = entity.name)
    dao.log(log)
    context.setNewEntity(entity.copy(name = entity.name * 3))
  }

  override def postDelete(entity: Listened, context: PostDeleteContext[Listened]): Unit = {
    val log = ListenLog(operation = "postDelete", entityName = entity.name)
    dao.log(log)
  }

  override def postInsert(entity: Listened, context: PostInsertContext[Listened]): Unit = {
    val log = ListenLog(operation = "postInsert", entityName = entity.name)
    dao.log(log)
  }

  override def postUpdate(entity: Listened, context: PostUpdateContext[Listened]): Unit = {
    val log = ListenLog(operation = "postUpdate", entityName = entity.name)
    dao.log(log)
  }

  override def preDelete(entity: Listened, context: PreDeleteContext[Listened]): Unit = {
    val log = ListenLog(operation = "preDelete", entityName = entity.name)
    dao.log(log)
  }
}