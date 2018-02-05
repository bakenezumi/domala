package domala.jdbc.builder

import domala._
import domala.jdbc.dialect.H2Dialect
import domala.jdbc.entity.EntityDesc
import domala.jdbc.mock._
import domala.jdbc.tx.LocalTransactionDataSource
import domala.jdbc.{Config, EntityDescProvider, LocalTransactionConfig, Naming}
import org.scalatest.{BeforeAndAfter, FunSuite}

class SelectBuilderTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = SelectBuilderTestConfig
  implicit val desc: EntityDesc[Person] = EntityDescProvider.get[Person]
  val testEnvDao: PersonDao = PersonDao.impl

  val initialPersons =
    Seq(
      Person(Some(ID(1)),Some(Name("SMITH")),Some(10),Address("Tokyo","Yaesu"),Some(2),Some(0)),
      Person(Some(ID(2)),Some(Name("ALLEN")),Some(20),Address("Kyoto","Karasuma"),Some(1),Some(0)))

  before {
    Required {
      testEnvDao.create()
    }
  }

  after {
    Required {
      testEnvDao.drop()
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
