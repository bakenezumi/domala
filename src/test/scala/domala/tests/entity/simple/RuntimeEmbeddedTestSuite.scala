package domala.tests.entity.simple

import java.time.{LocalDate, LocalDateTime}

import domala._
import domala.jdbc.{BatchResult, Config, Result}
import domala.tests.H2TestConfigTemplate
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.BatchInsert

class RuntimeEmbeddedTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = new H2TestConfigTemplate("runtime-embedded"){}
  val dao: RuntimeEmbeddedDao = RuntimeEmbeddedDao.impl

  val entity = RuntimeEmbedded(Some(ID(1)), Name("foo"), Times(Some(MyTime(LocalDateTime.of(2017, 1, 12, 12, 59, 59, 999999999))), LocalDate.of(2018, 1, 12)), Values(123.456, Some(BigDecimal(987.654))))

  val entities = Seq(
    RuntimeEmbedded(Some(ID(2)), Name("bar"), Times(None, LocalDate.of(2018, 1, 13)), Values(234.567, Some(BigDecimal(876.543)))),
    RuntimeEmbedded(Some(ID(3)), Name("baz"), Times(Some(MyTime(LocalDateTime.of(2017, 1, 13,  0,  0, 0, 1))), LocalDate.of(2018, 1, 13)), Values(345.678, Some(BigDecimal(765.432))))
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
      assert(dao.selectAll() == inserted.copy(values = inserted.values.copy(value2 = None)) :: Nil)
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
      assert(dao.selectAll() == entities.map(e => e.copy(values = e.values.copy(value2 = None))))
    }
  }

  test("batch update with sql") {
    Required {
      dao.batchInsert(entities)
      val BatchResult(cnt, updated) = dao.batchUpdateWithSql(entities.map(e => e.copy(values = e.values)))
      assert(cnt sameElements Array(1, 1))
      assert(updated == entities.map(e => e.copy(values = e.values)))
      assert(dao.selectAll() == entities.map(e => e.copy(values = e.values)))
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
      val insert = (entity: RuntimeEmbedded) =>
        update"""
        insert into runtime_entity (id, name, time, value, value2, date) values(
        ${entity.id}, ${entity.name}, ${entity.times.time}, ${entity.values.value}, ${entity.values.value2}, ${entity.times.date},
        )"""
      insert(entity).execute()
      entities.foreach(insert andThen(_.execute()))

      val update = (id: ID[RuntimeEmbedded], name: Name) =>
        update"""
        update runtime_entity set name = $name where id = $id
        """
      update(ID(2), Name("hoge")).execute()
      val selectByIds = (ids: Seq[ID[RuntimeEmbedded]]) =>
        select"""
          select /*%expand*/* from runtime_entity where id in ($ids)
        """
      assert(selectByIds(Seq(ID(1), ID(2), ID(3))).getList[RuntimeEmbedded] == entity +: entities.head.copy(name = Name("hoge")) +: entities.drop(1))

      val delete = (ids: Seq[ID[RuntimeEmbedded]]) =>
        update"""
        delete runtime_entity where id in ($ids)
        """
      delete(Seq(ID(2))).execute()
      assert(selectByIds(Seq(ID(1), ID(2), ID(3))).getList[RuntimeEmbedded] == entity +: entities.drop(1))

    }

  }

}

@Table("runtime_entity")
case class RuntimeEmbedded(
  @Id
  id: Option[ID[RuntimeEmbedded]],
  name: Name,
  times: Times,
  values: Values
)

case class Times(
  time: Option[MyTime],
  date: LocalDate
)
case class Values(
  value: Double,
  value2: Option[BigDecimal]
)



@Dao
trait RuntimeEmbeddedDao {
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
  def selectAll(): Seq[RuntimeEmbedded]

  @Insert
  def insert(entity: RuntimeEmbedded): Result[RuntimeEmbedded]

  @Update
  def update(entity: RuntimeEmbedded): Result[RuntimeEmbedded]

  @Delete
  def delete(entity: RuntimeEmbedded): Result[RuntimeEmbedded]

  @Insert(sql = """
insert into runtime_entity (id, name, time, value, date)
values(
  /* entity.id */0,
  /* entity.name */'foo',
  /* entity.times.time */'2018-01-01 23:59:59.999999',
  /* entity.values.value */0.0,
  /* entity.times.date */'2018-01-01'
)
  """)
  def insertWithSql(entity: RuntimeEmbedded): Result[RuntimeEmbedded]

  @Update(sql = """
update runtime_entity
set /*%populate*/id
where name = /* name */'hoge'
  """)
  def updateWithSql(entity: RuntimeEmbedded, name: Name): Result[RuntimeEmbedded]

  @Update(sql = """
delete from runtime_entity
where name = /* name */'hoge'
  """)
  def deleteWithSql(name: Name): Int

  @BatchInsert
  def batchInsert(entity: Seq[RuntimeEmbedded]): BatchResult[RuntimeEmbedded]


  @BatchInsert("""
insert into runtime_entity (id, name, time, value, date)
values(
  /* entity.id */0,
  /* entity.name */'foo',
  /* entity.times.time */'2018-01-01 23:59:59.999999',
  /* entity.values.value */0.0,
  /* entity.times.date */'2018-01-01'
)
  """)
  def batchInsertWithSql(entity: Seq[RuntimeEmbedded]): BatchResult[RuntimeEmbedded]


  @BatchUpdate(sql = """
update runtime_entity
set /*%populate*/id
where id = /* entity.id */0
  """)
  def batchUpdateWithSql(entity: Seq[RuntimeEmbedded]): BatchResult[RuntimeEmbedded]

  @BatchDelete(sql = """
delete runtime_entity
where id = /* entity.id */0
  """)
  def batchDeleteWithSql(entity: Seq[RuntimeEmbedded]): BatchResult[RuntimeEmbedded]

}
