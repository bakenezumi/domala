package domala.jdbc.builder

import domala._
import domala.jdbc.builder.mock._
import domala.jdbc.entity.EntityDesc
import domala.jdbc.{Config, EntityDescProvider, LocalTransactionConfig}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.seasar.doma.jdbc.Naming
import org.seasar.doma.jdbc.dialect.H2Dialect
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource

class SelectBuilderTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = SelectBuilderTestConfig
  implicit val desc: EntityDesc[Person] = EntityDescProvider.get[Person]

  val initialPersons =
    Seq(
      Person(Some(ID(1)),Some(Name("SMITH")),Some(10),Address("Tokyo","Yaesu"),Some(2),Some(0)),
      Person(Some(ID(2)),Some(Name("ALLEN")),Some(20),Address("Kyoto","Karasuma"),Some(1),Some(0)))

  before {
    Required {
      script"""
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

        insert into person (id, name, age, city, street, department_id, version) values(1, 'SMITH', 10, 'Tokyo', 'Yaesu', 2, 0);
        insert into person (id, name, age, city, street, department_id, version) values(2, 'ALLEN', 20, 'Kyoto', 'Karasuma', 1, 0);
      """.execute()
    }
  }

  after {
    Required {
      script"""
        drop table person;
        drop table department;
      """.execute()
    }
  }

  test("getEntityResultSeq") {
    Required {
      val result = SelectBuilder.newInstance(config).sql("select /*%expand*/* from person").getEntityResultSeq[Person]
      assert(result == initialPersons)
    }
  }

  test("getEntitySingleResult") {
    Required {
      val result = SelectBuilder.newInstance(config)
        .sql("select /*%expand*/* from person where id = ")
        .param(classOf[ID[Person]], ID[Person](2))
        .getEntitySingleResult[Person]
      assert(result == initialPersons(1))
    }
  }

  test("getOptionEntitySingleResult") {
    Required {
      val result = SelectBuilder.newInstance(config)
        .sql("select /*%expand*/* from person where id = ")
        .param(classOf[ID[Person]], ID[Person](2))
        .getOptionEntitySingleResult[Person]
      assert(result == Some(initialPersons(1)))
    }
  }

  test("getOptionEntitySingleResult None") {
    Required {
      val result = SelectBuilder.newInstance(config)
        .sql("select /*%expand*/* from person where id = ")
        .param(classOf[ID[Person]], ID[Person](99))
        .getOptionEntitySingleResult[Person]
      assert(result == None)
    }
  }

  test("iteratorEntity") {
    Required {
      val result = SelectBuilder.newInstance(config)
        .sql("select /*%expand*/* from person")
        .iteratorEntity((it: Iterator[Person]) => it.map(_.name).toList)
      assert(result == initialPersons.map(_.name))
    }
  }
}

object SelectBuilderTestConfig extends LocalTransactionConfig(
  dataSource =  new LocalTransactionDataSource(
    "jdbc:h2:mem:select-builder;DB_CLOSE_DELAY=-1", "sa", null),
  dialect = new H2Dialect,
  naming = Naming.SNAKE_LOWER_CASE
) {
  Class.forName("org.h2.Driver")
}
