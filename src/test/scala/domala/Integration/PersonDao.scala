package domala.Integration

import domala._

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
  def selectById2(id: Int): Person

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
  def selectWithDepartmentById2(id: Int): Option[PersonDepartment2]

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

  @Insert
  def insert(person: Person): org.seasar.doma.jdbc.Result[Person]

  @Update
  def update(person: Person): org.seasar.doma.jdbc.Result[Person]

  @Delete
  def delete(person: Person): org.seasar.doma.jdbc.Result[Person]
}
