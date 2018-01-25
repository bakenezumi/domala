package domala.tests.entity.simple

import java.time.{LocalDate, LocalDateTime}

import domala._
import domala.jdbc.{BatchResult, Config, Result}
import domala.tests.H2TestConfigTemplate
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.BatchInsert

class RuntimeEntityTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = new H2TestConfigTemplate("runtime-entity"){}
  val dao: RuntimeEntityDao = RuntimeEntityDao.impl

  val entity = RuntimeEntity(Some(ID(1)), Name("foo"), MyTime(LocalDateTime.of(2017, 1, 12, 12, 59, 59, 999999999)), 123.456, BigDecimal(987.654), LocalDate.of(2018, 1, 12))

  val entities = Seq(
    RuntimeEntity(Some(ID(2)), Name("bar"), MyTime(LocalDateTime.of(2017, 1, 13,  0,  0, 0, 0)), 234.567, BigDecimal(876.543), LocalDate.of(2018, 1, 13)),
    RuntimeEntity(Some(ID(3)), Name("baz"), MyTime(LocalDateTime.of(2017, 1, 13,  0,  0, 0, 1)), 345.678, BigDecimal(765.432), LocalDate.of(2018, 1, 13))
  )

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

  test("insert & select all") {
    Required {
      val Result(cnt, inserted) = dao.insert(entity)
      assert(cnt == 1)
      assert(inserted == entity)
      dao.batchInsert(entities)
      assert(dao.selectAll() == entity +: entities)
    }
  }

  test("update") {
    Required {
      dao.batchInsert(entities)
      val modified = entities.head.copy(name = Name("xxx"))
      val Result(cnt, updated) = dao.update(modified)
      assert(cnt == 1)
      assert(updated == modified)
      assert(dao.selectAll() == modified +: entities.drop(1))
    }
  }

  test("delete") {
    Required {
      dao.batchInsert(entities)
      val Result(cnt, deleted) = dao.delete(entities.head)
      assert(cnt == 1)
      assert(deleted == entities.head)
      assert(dao.selectAll() == entities.drop(1))

    }
  }

  test("insert with sql") {
    Required {
      val Result(cnt, inserted) = dao.insertWithSql(entity)
      assert(cnt == 1)
      assert(inserted == entity)
      assert(dao.selectAll() == inserted.copy(value2 = null) :: Nil)
    }
  }

  test("update with sql") {
    Required {
      dao.batchInsert(entities)
      val modified = entities.drop(1).head.copy(name = Name("xxx"))
      val Result(cnt, updated) = dao.updateWithSql(modified, Name("baz"))
      assert(cnt == 1)
      assert(updated == modified)
      assert(dao.selectAll() == entities.head :: modified  :: Nil)

    }
  }

  test("delete with sql") {
    Required {
      dao.batchInsert(entities)
      val cnt = dao.deleteWithSql(Name("baz"))
      assert(cnt == 1)
      assert(dao.selectAll() == entities.head :: Nil)

    }
  }

  test("batch insert with sql") {
    Required {
      val BatchResult(cnt, inserted) = dao.batchInsertWithSql(entities)
      assert(cnt sameElements Array(1, 1))
      assert(inserted == entities)
      assert(dao.selectAll() == entities.map(_.copy(value2 = null)))
    }
  }

  test("batch update with sql") {
    Required {
      dao.batchInsert(entities)
      val BatchResult(cnt, updated) = dao.batchUpdateWithSql(entities.map(e => e.copy(value2 = e.value)))
      assert(cnt sameElements Array(1, 1))
      assert(updated == entities.map(e => e.copy(value2 = e.value)))
      assert(dao.selectAll() == entities.map(e => e.copy(value2 = e.value)))
    }
  }

  test("batch delete with sql") {
    Required {
      dao.batchInsert(entities)
      val BatchResult(cnt, deleted) = dao.batchDeleteWithSql(entities)
      assert(cnt sameElements Array(1, 1))
      assert(deleted == entities)
      assert(dao.selectAll() == Nil)
    }
  }

  test("sql interpolation") {
    Required {
      val insert = (entity: RuntimeEntity) =>
        update"""
        insert into runtime_entity (id, name, time, value, value2, date) values(
        ${entity.id}, ${entity.name}, ${entity.time}, ${entity.value}, ${entity.value2}, ${entity.date},
        )"""
      insert(entity).execute()
      entities.foreach(insert andThen(_.execute()))

      val update = (id: ID[RuntimeEntity], name: Name) =>
        update"""
        update runtime_entity set name = $name where id = $id
        """
      update(ID(2), Name("hoge")).execute()
      val selectByIds = (ids: Seq[ID[RuntimeEntity]]) =>
        select"""
          select /*%expand*/* from runtime_entity where id in ($ids)
        """
      assert(selectByIds(Seq(ID(1), ID(2), ID(3))).getList[RuntimeEntity] == entity +: entities.head.copy(name = Name("hoge")) +: entities.drop(1))

      val delete = (ids: Seq[ID[RuntimeEntity]]) =>
        update"""
        delete runtime_entity where id in ($ids)
        """
      delete(Seq(ID(2))).execute()
      assert(selectByIds(Seq(ID(1), ID(2), ID(3))).getList[RuntimeEntity] == entity +: entities.drop(1))

    }

  }

}


case class ID[E](v: Long) extends AnyVal
case class Name(v: String) extends AnyVal
case class MyTime(v: LocalDateTime) extends AnyVal

case class RuntimeEntity (
  @Id
  id: Option[ID[RuntimeEntity]],
  name: Name,
  time: MyTime,
  value: Double,
  value2: BigDecimal,
  date: LocalDate
)

@Dao
trait RuntimeEntityDao {
  @Script(sql =
    """
create table runtime_entity(
  id int not null identity primary key,
  name varchar(20),
  time timestamp,
  value double,
  value2 double,
  date date
);
    """)
  def create()

  @Script(sql = "drop table runtime_entity")
  def drop()

  @Select("select * from runtime_entity order by id")
  def selectAll(): Seq[RuntimeEntity]

  @Insert
  def insert(entity: RuntimeEntity): Result[RuntimeEntity]

  @Update
  def update(entity: RuntimeEntity): Result[RuntimeEntity]

  @Delete
  def delete(entity: RuntimeEntity): Result[RuntimeEntity]

  @Insert(sql = """
insert into runtime_entity (id, name, time, value, date)
values(
  /* entity.id */0,
  /* entity.name */'foo',
  /* entity.time */'2018-01-01 23:59:59.999999',
  /* entity.value */0.0,
  /* entity.date */'2018-01-01'
)
  """)
  def insertWithSql(entity: RuntimeEntity): Result[RuntimeEntity]

  @Update(sql = """
update runtime_entity
set /*%populate*/id
where name = /* name */'hoge'
  """)
  def updateWithSql(entity: RuntimeEntity, name: Name): Result[RuntimeEntity]

  @Update(sql = """
delete from runtime_entity
where name = /* name */'hoge'
  """)
  def deleteWithSql(name: Name): Int

  @BatchInsert
  def batchInsert(entity: Seq[RuntimeEntity]): BatchResult[RuntimeEntity]


  @BatchInsert("""
insert into runtime_entity (id, name, time, value, date)
values(
  /* entity.id */0,
  /* entity.name */'foo',
  /* entity.time */'2018-01-01 23:59:59.999999',
  /* entity.value */0.0,
  /* entity.date */'2018-01-01'
)
  """)
  def batchInsertWithSql(entity: Seq[RuntimeEntity]): BatchResult[RuntimeEntity]


  @BatchUpdate(sql = """
update runtime_entity
set /*%populate*/id
where id = /* entity.id */0
  """)
  def batchUpdateWithSql(entity: Seq[RuntimeEntity]): BatchResult[RuntimeEntity]

  @BatchDelete(sql = """
delete runtime_entity
where id = /* entity.id */0
  """)
  def batchDeleteWithSql(entity: Seq[RuntimeEntity]): BatchResult[RuntimeEntity]

}
