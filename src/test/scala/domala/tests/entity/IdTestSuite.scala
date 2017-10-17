package domala.tests.entity

import domala._
import domala.jdbc.{BatchResult, Config, Result}
import domala.tests.TestConfig
import org.scalatest.{BeforeAndAfter, FunSuite}

class IdTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = TestConfig

  val dao: IdDao = IdDao.impl

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

  test("strategy = GenerationType.IDENTITY") {
    Required {
      dao.insertIdentity(GeneratedIdentity (data = "aaa"))
      dao.batchInsertIdentity(Seq(GeneratedIdentity(data = "bbb"), GeneratedIdentity(data = "ccc")))
      assert(dao.selectIdentityAll == Seq(
        GeneratedIdentity(Some(1L), "aaa"),
        GeneratedIdentity(Some(2L), "bbb"),
        GeneratedIdentity(Some(3L), "ccc")
      ))
    }
  }

  test("strategy = GenerationType.SEQUENCE") {
    Required {
      dao.insertSequence(GeneratedSequence(data = "aaa"))
      dao.batchInsertSequence(Seq(GeneratedSequence(data = "bbb"), GeneratedSequence(data = "ccc")))
      assert(dao.selectSequenceAll == Seq(
        GeneratedSequence(Some(5L), "aaa"), // sequence start with 5
        GeneratedSequence(Some(6L), "bbb"),
        GeneratedSequence(Some(7L), "ccc")
      ))
    }
  }

  test("strategy = GenerationType.TABLE") {
    Required {
      dao.insertTable(GeneratedTable(data = "aaa"))
      dao.batchInsertTable(Seq(GeneratedTable(data = "bbb"), GeneratedTable(data = "ccc")))
      assert(dao.selectTableAll == Seq(
        GeneratedTable(Some(100L), "aaa"), // initial value is 100
        GeneratedTable(Some(101L), "bbb"),
        GeneratedTable(Some(102L), "ccc")
      ))
      assert(dao.selectIdGenerator == 110) // allocationSize = 10
    }
  }

  test("no generate") {
    Required {
      dao.insert(NoGenerate(10, "aaa"))
      dao.batchInsert(Seq(NoGenerate(20, "bbb"), NoGenerate(30, "ccc")))
      assert(dao.selectAll == Seq(
        NoGenerate(10, "aaa"),
        NoGenerate(20, "bbb"),
        NoGenerate(30, "ccc")
      ))
    }
  }
}

@Entity
@Table(name = "id_test")
case class GeneratedIdentity (
  @domala.Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  id: Option[Long] = None,
  data: String
)

@Entity
@Table(name = "id_test")
case class GeneratedSequence (
  @domala.Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(sequence = "seq")
  id: Option[Long] = None,
  data: String
)

@Entity
@Table(name = "id_test")
case class GeneratedTable (
  @domala.Id
  @GeneratedValue(strategy = GenerationType.TABLE)
  @TableGenerator(pkColumnValue = "GeneratedTable", allocationSize = 10)
  id: Option[Long] = None,
  data: String
)

@Entity
@Table(name = "id_test")
case class NoGenerate (
  @domala.Id
  id: Long,
  @domala.Id
  data: String
)

@Dao(config = TestConfig)
trait IdDao {

  @Script(sql = """
create table id_test(
  id long not null identity primary key,
  data varchar(20)
);

create sequence seq start with 5;

create table ID_GENERATOR(
  PK char(20) not null primary key,
  VALUE long
);

insert into ID_GENERATOR values('GeneratedTable', 100);
    """)
  def create()

  @Script(sql = """
drop table ID_GENERATOR;
drop sequence seq;
drop table id_test;
    """)
  def drop()

  @Select(sql="""
select * from id_test
  """)
  def selectIdentityAll: Seq[GeneratedIdentity]

  @Insert
  def insertIdentity(entity: GeneratedIdentity): Result[GeneratedIdentity]

  @BatchInsert
  def batchInsertIdentity(entity: Seq[GeneratedIdentity]): BatchResult[GeneratedIdentity]

  @Select(sql="""
select * from id_test
  """)
  def selectSequenceAll: Seq[GeneratedSequence]

  @Insert
  def insertSequence(entity: GeneratedSequence): Result[GeneratedSequence]

  @BatchInsert
  def batchInsertSequence(entity: Seq[GeneratedSequence]): BatchResult[GeneratedSequence]

  @Select(sql="""
select * from id_test
  """)
  def selectTableAll: Seq[GeneratedTable]

  @Insert
  def insertTable(entity: GeneratedTable): Result[GeneratedTable]

  @BatchInsert
  def batchInsertTable(entity: Seq[GeneratedTable]): BatchResult[GeneratedTable]

  @Select(sql="""
select value from ID_GENERATOR where PK = 'GeneratedTable'
  """)
  def selectIdGenerator: Long

  @Select(sql="""
select * from id_test
  """)
  def selectAll: Seq[NoGenerate]

  @Insert
  def insert(entity: NoGenerate): Result[NoGenerate]

  @BatchInsert
  def batchInsert(entity: Seq[NoGenerate]): BatchResult[NoGenerate]
}
