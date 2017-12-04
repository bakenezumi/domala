Domala: Doma for Scala
======================

Domala is a database access library for Scala. This wraps [Doma2](https://github.com/domaframework/doma).

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.domala/domala_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.domala/domala_2.12)
[![CircleCI](https://circleci.com/gh/bakenezumi/domala.svg?style=svg)](https://circleci.com/gh/bakenezumi/domala)

- Domala uses [scalameta](http://scalameta.org/paradise/) to generate code and validate sql mappings at **compile time**.

- Select statements are write by yourself. It is automatically mapped to `Option[`*`Entity`*`]`, `Seq[`*`Entity`*`]`, `Stream[`*`Entity`*`]`, `Seq[Map[String, Any]]`, and [more](./notes/specification.md#mapable-types).

- Other statements are automatically generated from Entity. It can also write SQL.


### Setup build

```scala
lazy val metaMacroSettings: Seq[Def.Setting[_]] = Seq(
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise",
  scalacOptions in (Compile, console) ~= (_ filterNot (_ contains "paradise")) // macroparadise plugin doesn't work in repl yet.
)

lazy val yourProject = project.settings(
  metaMacroSettings,
  libraryDependencies ++= Seq(
    "com.github.domala" %% "domala" % "0.1.0-beta.5",
    // ... your other library dependencies
  ),
  // ... your other project settings
)

```

### Example

#### Holder
A value holder

```scala
@Holder
case class Name(value: String)
```

#### Embeddable

Partial class to embed in entity

```scala
@Embeddable
case class Address(city: String, street: String)
```

#### Entity
Classes whose instances can be stored in a database

```scala
@Entity
case class Person(
  @Id
  id: Int,
  name: Name,
  age: Option[Int],
  address: Address,
  departmentId: Option[Int],
  @Version
  version: Option[Int] = None
)
```

#### Dao
Trait of data access object

```scala
@Dao
trait PersonDao {

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

  @Insert
  def insert(person: Person): Result[Person]

  @Update
  def update(person: Person): Result[Person]
}
```

#### Config
A configuration of a database

```scala
object ExampleConfig extends LocalTransactionConfig(
  dataSource = new LocalTransactionDataSource(
    "jdbc:h2:mem:example;DB_CLOSE_DELAY=-1", "sa", null),
  dialect = new H2Dialect,
  naming = Naming.SNAKE_LOWER_CASE
) {
  Class.forName("org.h2.Driver")
}
```

#### Usage
```scala  
implicit val config = ExampleConfig

// Dao implementation is auto generated.
val dao: PersonDao = PersonDao.impl 

val entity = Person(
  id = 1,
  name = Name("SMITH"),
  age = Some(10),
  address = Address("TOKYO", "YAESU"),
  departmentId = Some(1)
)

// `Required` is Executes the transaction.
// If processing in block ends normally it commit.
// If Exception occurs it rollback.
Required {
  dao.insert(entity)
  dao.selectById(1).foreach(e =>
    dao.update(e.copy(age = e.age.map(_ + 1)))
  )
}
```

### Run example

```sh
sbt
>example/run
```

### In REPL

Annotation macro doesn't work in repl yet.
So, compile separately with `sbt ~console` or use the following SQL interpolations. 

```scala
import domala._
import domala.jdbc.LocalTransactionConfig
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource
import org.seasar.doma.jdbc.dialect.H2Dialect

// A configuration of database
implicit object ExampleConfig extends LocalTransactionConfig(
  dataSource = new LocalTransactionDataSource(
    "jdbc:h2:mem:example;DB_CLOSE_DELAY=-1", "sa", null),
  dialect = new H2Dialect
) {
  Class.forName("org.h2.Driver")
}

Required {
  // `script"..." ` builds a script query statement
  script"""
    create table emp(
      id int serial primary key,
      name varchar(20)
    );
  """.execute()
}

Required {
  // `update"..." ` builds a `INSERT`, `UPDATE`, `DELETE` query statement
  Seq("Scott", "Allen").map { name =>
    update"""
      insert into emp(name) values($name)
    """.execute()
  }
}

// `select"..." ` builds a `SELECT` query statement
val query =
  select"""
    select id, name
    from emp
    order by id
  """

Required {
  query.getMapList
} // => List(Map("ID" -> 1, "NAME" -> "Scott"), Map("ID" -> 2, "NAME" -> "Allen"))

Required {
  select"select id from emp".getList[Int]
} // => List(1, 2)

// If the `Entity` class has already been compiled
// then can the following 
// @Holder case class ID[E] (value: Int)
// @Entity case class Emp(id: ID[Emp], name: String) 
Required {
  query.getList[Emp]
} // => List(Emp(ID(1), "Scott"), Emp(ID(2), "Allen"))

```

### Play integration sample

[domala-play-sample](https://github.com/bakenezumi/domala-play-sample)

License
--------
Apache License, Version 2.0
