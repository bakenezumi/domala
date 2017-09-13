package domala.tests

import domala._
import domala.jdbc.Result
import org.seasar.doma.jdbc.builder.SelectBuilder

@Dao(config = TestConfig)
trait PersonDao {
  @Script(sql = """
create table department(
    id int not null identity primary key,
    name varchar(20),
    version int not null
);

create table person(
    id int not null identity primary key,
    name varchar(20),
    age int,
    city varchar(20) not null,
    street varchar(20) not null,
    department_id int not null,
    version int not null,
    constraint fk_department_id foreign key(department_id) references department(id)
);

insert into department (id, name, version) values(1, 'ACCOUNTING', 0);
insert into department (id, name, version) values(2, 'SALES', 0);

insert into person (id, name, age, city, street, department_id, version) values(1, 'SMITH', 10, 'Tokyo', 'Yaesu', 1, 0);
insert into person (id, name, age, city, street, department_id, version) values(2, 'ALLEN', 20, 'Kyoto', 'Karasuma', 2, 0);
  """)
  def create(): Unit

  @Script(sql = """
drop table person;
drop table department;
  """)
  def drop(): Unit

  @Select(sql = """
select *
from person
where id = /*id*/0
  """)
  def selectById(id: Int): Option[Person]

  @Select(sql = """
select count(*)
from person
  """)
  def selectCount(): Int

  @Select(sql = """
select *
from person
  """)
  def selectAll(): Seq[Person]

  @Select(sql = """
select *
from person
where id = /*id*/0
  """)
  def selectByIdNullable(id: Int): Person

  @Select(sql = """
select
    p.id,
    p.name,
    d.id as department_id,
    d.name as department_name
from
    person p
    inner join
    department d
    on (p.department_id = d.id)
where
    p.id = /*id*/0  
  """)
  def selectWithDepartmentById(id: Int): Option[PersonDepartment]

  @Select(sql = """
select
    p.id,
    p.name,
    d.id as department_id,
    d.name as department_name
from
    person p
    inner join
    department d
    on (p.department_id = d.id)
where
    p.id = /*id*/0  
  """)
  def selectWithDepartmentEmbeddedById(id: Int): Option[PersonDepartmentEmbedded]

  @Select(sql = """
select *
from person
  """, strategy = SelectType.STREAM)
  def selectAllStream(f: Stream[Person] => Int): Int

  @Select(sql = """
select *
from person
where
    id = /*id*/0
  """, strategy = SelectType.STREAM)
  def selectByIdStream(id: Int)(f: Stream[Person] => Address): Address

  @Select(sql = """
select *
from person
  """)
  def selectAllSeqMap(): Seq[Map[String, Any]]

  @Select(sql = """
select *
from person
where
    id = /*id*/0
  """)
  def selectByIdMap(id: Int): Map[String, Any]

  @Select(sql = """
select *
from person
where
    id = /*id*/0
  """)
  def selectByIdOptionMap(id: Int): Option[Map[String, Any]]

  @Select(sql = """
select *
from person
  """, strategy = SelectType.STREAM)
  def selectAllStreamMap(f: Stream[Map[String, Any]] => Int): Int

  @Select(sql = """
select name
from person
where
    id = /*id*/0
  """)
  def selectNameById(id: Int): Option[Name]

  @Select(sql = """
select name
from person
where
    id = /*id*/0
  """)
  def selectNameByIdNullable(id: Int): Name

  @Select(sql = """
select name
from person
  """)
  def selectNames: Seq[Name]

  @Select(sql = """
select name
from person
  """, strategy = SelectType.STREAM)
  def selectNameStream(f: Stream[Name] => Int): Int

  def selectByIDBuilder(id: Int): String = {
    val builder = SelectBuilder.newInstance(TestConfig)
    builder.sql("select")
    builder.sql("name")
    builder.sql("from person")
    builder.sql("where")
    builder.sql("id =").param(classOf[Int], id)
    builder.getScalarSingleResult(classOf[String])
  }


  @Insert
  def insert(person: Person): Result[Person]

  @Update
  def update(person: Person): Result[Person]

  @Delete
  def delete(person: Person): Result[Person]
}
