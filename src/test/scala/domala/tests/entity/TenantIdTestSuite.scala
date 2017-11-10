package domala.tests.entity

import domala._
import domala.jdbc.{BatchResult, Config, Result}
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}

class TenantIdTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig
  val dao: TenantIdDao = TenantIdDao.impl
  val initial = Seq(
    HasTenant(1, 1, "AAA"),
    HasTenant(2, 2, "BBB"),
    HasTenant(3, 3, "CCC"),
    HasTenant(4, 1, "DDD"),
    HasTenant(5, 2, "EEE"),
  )

  before {
    Required {
      dao.create()
      dao.batchInsert(initial)
    }
  }

  after {
    Required {
      dao.drop()
    }
  }

  test("update") {
    Required {
      val entity = HasNoTenant(3, 2, "FFF")
      val Result(count, _) = dao.update(entity)
      assert(count == 1)
      assert(dao.selectAll == Seq(
        HasTenant(1, 1, "AAA"),
        HasTenant(2, 2, "BBB"),
        HasTenant(3, 2, "FFF"),
        HasTenant(4, 1, "DDD"),
        HasTenant(5, 2, "EEE"),
      ))
    }
  }

  test("update by tenant") {
    Required {
      val entity = HasTenant(3, 2, "FFF")
      val Result(count, _) = dao.updateByTenant(entity)
      assert(count == 0)
      val entity2 = HasTenant(3, 3, "FFF")
      val Result(count2, _) = dao.updateByTenant(entity2)
      assert(count2 == 1)
    }
  }

  test("delete") {
    Required {
      val entity = HasNoTenant(3, 2, "FFF")
      val Result(count, _) = dao.delete(entity)
      assert(count == 1)
      assert(dao.selectAll == Seq(
        HasTenant(1, 1, "AAA"),
        HasTenant(2, 2, "BBB"),
        HasTenant(4, 1, "DDD"),
        HasTenant(5, 2, "EEE"),
      ))
    }
  }

  test("delete by tenant") {
    Required {
      val entity = HasTenant(3, 2, "FFF")
      val Result(count, _) = dao.deleteByTenant(entity)
      assert(count == 0)
      val entity2 = HasTenant(3, 3, "FFF")
      val Result(count2, _) = dao.deleteByTenant(entity2)
      assert(count2 == 1)
    }
  }

}

@Entity
@Table(name = "tenant_id_test")
case class HasTenant (
  @Id
  id: Int,
  @TenantId
  tenant: Int,
  data: String
)

@Entity
@Table(name = "tenant_id_test")
case class HasNoTenant (
  @Id
  id: Int,
  tenant: Int,
  data: String
)

@Dao(config = TestConfig)
trait TenantIdDao {

  @Script(sql = """
create table tenant_id_test(
  id int not null identity primary key,
  tenant int not null,
  data varchar(20)
);

    """)
  def create()

  @Script(sql = """
drop table tenant_id_test;
    """)
  def drop()

  @Select(sql="""
select * from tenant_id_test order by id
  """)
  def selectAll: Seq[HasTenant]

  @BatchInsert
  def batchInsert(entity: Seq[HasTenant]): BatchResult[HasTenant]

  @Update
  def updateByTenant(entity: HasTenant): Result[HasTenant]

  @Delete
  def deleteByTenant(entity: HasTenant): Result[HasTenant]

  @Update
  def update(entity: HasNoTenant): Result[HasNoTenant]

  @Delete
  def delete(entity: HasNoTenant): Result[HasNoTenant]

}
