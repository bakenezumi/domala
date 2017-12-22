package example

import domala._
import domala.jdbc.Result

@Dao
trait PersonDao {
  @Script(sql = """
create table department(
    id int not null primary key,
    name varchar(20),
    version int not null
);

create table person(
    id int not null primary key,
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
create sequence person_id_seq start with 3;

  """)
  def create(): Unit

  @Select(sql = """
select *
from person
where id = /*id*/0
  """)
  def selectById(id: Int): Option[Person]

  @Select(sql = """
select *
from person where
/*%if name != null */
  name like /* @prefix(name) */'%' escape '$'
/*%end*/
  """)
  def selectByName(name: String): Seq[Person]

  @Select(sql = """
select *
from person
  """)
  def selectAll(): Seq[Person]

  @Select
  def selectWithDepartmentById(id: Int): Option[PersonDepartment]

  @Insert
  def insert(person: Person): Result[Person]

  @Update
  def update(person: Person): Result[Person]

  @Delete
  def delete(person: Person): Result[Person]
}
