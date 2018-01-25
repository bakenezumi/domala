package domala.tests.entity

import domala._
import domala.jdbc.Config
import domala.jdbc.Result
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.jdbc.entity._

//noinspection SpellCheckingInspection
class GenericEntityListenerTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig
  val dao: GenericListenedDao = GenericListenedDao.impl
  val logDao: GenericListenLogDao = GenericListenLogDao.impl
  test("listener test") {
    Required {
      dao.create()
      val result = dao.insert(Parent(None, name = "A")) //preInsert twice
      assert(result.entity == Parent(Some(1),"AA"))
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

  test("extended listener test") {
    Required {
      dao.create()
      val result = dao.insert2(new Child(None, "B", 1))
      assert(result.entity == new Child(None,"BB", 1))
      dao.drop()
    }
  }
}

@Entity(classOf[GenericListener[Parent]])
case class Parent(
  @domala.Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  id: Option[Int] = None,
  name: String,
)

//TODO: listener無指定でも引き継げるように
@Entity(classOf[GenericListener[Child]])
class Child(
  override val id: Option[Int],
  override val name: String,
  val age: Int
) extends Parent (id, name) {
  // ケースクラスの自動生成メソッドをオーバーライド
  override def copy(id: Option[Int] = this.id, name: String = this.name): Parent = {
    new Child(id, name, this.age)
  }
}

@Dao
trait GenericListenedDao {
  @Script(sql = """
create table parent(
    id int not null identity primary key,
    name varchar(20)
);

create table child(
    id int not null identity primary key,
    name varchar(20),
    age int
);

create table generic_listen_log(
    id int not null identity primary key,
    operation varchar(20),
    entity_id  varchar(1),
    entity_name  varchar(20)
);
  """)
  def create(): Unit

  @Script("""
drop table generic_listen_log;
drop table child;
drop table parent;
  """)
  def drop()

  @Insert
  def insert(entity: Parent): Result[Parent]

  @Update
  def update(entity: Parent): Result[Parent]

  @Select(sql="""
select * from parent
  """)
  def select: Parent

  @Delete
  def delete(entity: Parent): Result[Parent]

  @Insert
  def insert2(entity: Child): Result[Child]
}

@Entity
case class GenericListenLog(
  @domala.Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  id: Option[Int] = None,
  operation: String,
  entityId: Option[Int],
  entityName: String,
)

@Dao
trait GenericListenLogDao {
  @Insert
  def log(entity: GenericListenLog): Result[GenericListenLog]

  @Select(sql=
  """
select * from generic_listen_log
  """)
  def select: Seq[ListenLog]
}

class GenericListener[E <: Parent] extends EntityListener[E] {
  implicit val config: Config = TestConfig
  val dao: GenericListenLogDao = GenericListenLogDao.impl

  override def preInsert(entity: E, context: PreInsertContext[E]): Unit = {
   val log = GenericListenLog(None, operation = "preInsert", entityId = entity.id, entityName = entity.name)
    dao.log(log)
    entity.copy(name = entity.name * 2) match {
      case e if e.getClass == entity.getClass => context.setNewEntity(e.asInstanceOf[E])
      case _ => ()
    }
  }

  override def preUpdate(entity: E, context: PreUpdateContext[E]): Unit = {
    val log = GenericListenLog(None, operation = "preUpdate", entityId = entity.id, entityName = entity.name)
    dao.log(log)
    entity.copy(name = entity.name * 3) match {
      case e if e.getClass == entity.getClass => context.setNewEntity(e.asInstanceOf[E])
      case _ => ()
    }
  }

  override def postDelete(entity: E, context: PostDeleteContext[E]): Unit = {
    val log = GenericListenLog(None, operation = "postDelete", entityId = entity.id, entityName = entity.name)
    dao.log(log)
  }

  override def postInsert(entity: E, context: PostInsertContext[E]): Unit = {
    val log = GenericListenLog(None, operation = "postInsert", entityId = entity.id, entityName = entity.name)
    dao.log(log)
  }

  override def postUpdate(entity: E, context: PostUpdateContext[E]): Unit = {
    val log = GenericListenLog(None, operation = "postUpdate", entityId = entity.id, entityName = entity.name)
    dao.log(log)
  }

  override def preDelete(entity: E, context: PreDeleteContext[E]): Unit = {
    val log = GenericListenLog(None, operation = "preDelete", entityId = entity.id, entityName = entity.name)
    dao.log(log)
  }
}