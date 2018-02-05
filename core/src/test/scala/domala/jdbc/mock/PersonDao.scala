package domala.jdbc.mock


trait PersonDao {
  def create(): Unit
  def drop(): Unit
  def findAll: List[Person]
  def findById(id: ID[Person]): Option[Person]
}


import domala.jdbc.Config
object PersonDao {
  def impl(implicit config: Config): PersonDao = new PersonDaoImpl
}

import domala._
import domala.jdbc.Config

class PersonDaoImpl(implicit config: Config) extends PersonDao {
  def create(): Unit = script"""
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
        department_id int,
        version int not null,
        constraint fk_department_id foreign key(department_id) references department(id)
    );

    insert into department (id, name, version) values(1, 'ACCOUNTING', 0);
    insert into department (id, name, version) values(2, 'SALES', 0);

    insert into person (id, name, age, city, street, department_id, version) values(1, 'SMITH', 10, 'Tokyo', 'Yaesu', 2, 0);
    insert into person (id, name, age, city, street, department_id, version) values(2, 'ALLEN', 20, 'Kyoto', 'Karasuma', 1, 0);
  """.execute()

  def drop(): Unit = script"""
      drop table person;
      drop table department;
    """.execute()

  def findAll: List[Person] = select"select /*%expand*/* from person".getList[Person]

  def findByIds[R](ids: Iterable[ID[Person]], mapper: Iterator[Person] => R): R = select"select /*%expand*/* from person where id in ($ids)".apply(mapper)

  def findById(id: ID[Person]): Option[Person] = findByIds(Seq(id), _.toStream.headOption)
}
