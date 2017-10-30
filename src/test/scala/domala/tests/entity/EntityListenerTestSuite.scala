package domala.tests.entity

import domala._
import domala.jdbc.Config
import domala.jdbc.Result
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.jdbc.entity._

//noinspection SpellCheckingInspection
class EntityListenerTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig
  val dao: ListenedDao = ListenedDao.impl
  val logDao: ListenLogDao = ListenLogDao.impl
  test("listener test") {
    Required {
      dao.create()
      val result = dao.insert(Listened(None, name = "A")) //preInsert twice
      assert(result.entity == Listened(Some(1),"AA"))
      dao.update (dao.select) //preUpdate thrice
      val entity = dao.select
      assert(entity.name === "AAAAAA")
      dao.delete(entity)
      val log = logDao.select
      assert(
        log === Seq(
          ListenLog(Some(1),"preInsert",None,"A"),
          ListenLog(Some(2),"postInsert",Some(1),"AA"),
          ListenLog(Some(3),"preUpdate",Some(1),"AA"),
          ListenLog(Some(4),"postUpdate",Some(1),"AAAAAA"),
          ListenLog(Some(5),"preDelete",Some(1),"AAAAAA"),
          ListenLog(Some(6),"postDelete",Some(1),"AAAAAA")
        )
      )
      dao.drop()
    }
  }

}

@Entity(classOf[MyListener])
case class Listened(
  @domala.Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  id: Option[Int] = None,
  name: String,
)

@Dao
trait ListenedDao {
  @Script(sql = """
create table listened(
    id int not null identity primary key,
    name varchar(20)
);

create table listen_log(
    id int not null identity primary key,
    operation varchar(20),
    entity_id  varchar(1),
    entity_name  varchar(20)
);
  """)
  def create(): Unit

  @Script("""
drop table listen_log;
drop table listened;
  """)
  def drop()

  @Insert
  def insert(entity: Listened): Result[Listened]

  @Update
  def update(entity: Listened): Result[Listened]

  @Select(sql="""
select * from listened
  """)
  def select: Listened

  @Delete
  def delete(entity: Listened): Result[Listened]

}

@Entity
case class ListenLog(
  @domala.Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  id: Option[Int] = None,
  operation: String,
  entityId: Option[Int],
  entityName: String,
)

@Dao
trait ListenLogDao {
  @Insert
  def log(entity: ListenLog): Result[ListenLog]

  @Select(sql=
  """
select * from listen_log
  """)
  def select: Seq[ListenLog]
}

class MyListener extends EntityListener[Listened] {
  implicit val config: Config = TestConfig
  val dao: ListenLogDao = ListenLogDao.impl

  override def preInsert(entity: Listened, context: PreInsertContext[Listened]): Unit = {
   val log = ListenLog(None, operation = "preInsert", entityId = entity.id, entityName = entity.name)
    dao.log(log)
    context.setNewEntity(entity.copy(name = entity.name * 2))
  }

  override def preUpdate(entity: Listened, context: PreUpdateContext[Listened]): Unit = {
    val log = ListenLog(None, operation = "preUpdate", entityId = entity.id, entityName = entity.name)
    dao.log(log)
    context.setNewEntity(entity.copy(name = entity.name * 3))
  }

  override def postDelete(entity: Listened, context: PostDeleteContext[Listened]): Unit = {
    val log = ListenLog(None, operation = "postDelete", entityId = entity.id, entityName = entity.name)
    dao.log(log)
  }

  override def postInsert(entity: Listened, context: PostInsertContext[Listened]): Unit = {
    val log = ListenLog(None, operation = "postInsert", entityId = entity.id, entityName = entity.name)
    dao.log(log)
  }

  override def postUpdate(entity: Listened, context: PostUpdateContext[Listened]): Unit = {
    val log = ListenLog(None, operation = "postUpdate", entityId = entity.id, entityName = entity.name)
    dao.log(log)
  }

  override def preDelete(entity: Listened, context: PreDeleteContext[Listened]): Unit = {
    val log = ListenLog(None, operation = "preDelete", entityId = entity.id, entityName = entity.name)
    dao.log(log)
  }
}