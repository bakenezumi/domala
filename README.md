Domala: Doma for Scala
======================

Domala is a database access library for Scala. This wraps [Doma2](https://github.com/domaframework/doma).

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.domala/domala_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.domala/domala_2.12)
[![CircleCI](https://circleci.com/gh/bakenezumi/domala.svg?style=svg)](https://circleci.com/gh/bakenezumi/domala)

- Domala uses [scalameta](http://scalameta.org/paradise/) to generate code and validate sql mappings at **compile time**.

- Select statements are write by yourself. It is automatically mapped to `Option[`*`Entity`*`]`, `Seq[`*`Entity`*`]`, `Stream[`*`Entity`*`]`, `Seq[Map[String, Any]]`, and [more](./notes/specification.md#mapable-types).

- Other statements are automatically generated from Entity. It can also write SQL.


### Setup build

#### when use annotation macro

All functions are available under the setting below.

```scala
lazy val metaMacroSettings: Seq[Def.Setting[_]] = Seq(
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise",
  scalacOptions in (Compile, console) ~= (_ filterNot (_ contains "paradise")) // macroparadise plugin doesn't work in repl yet.
)

lazy val yourProject = project.settings(
  metaMacroSettings,
  // required to validate SQL files at compile time
  compile in Compile := ((compile in Compile) dependsOn (copyResources in Compile)).value,
  libraryDependencies ++= Seq(
    "com.github.domala" %% "domala" % "0.1.0-beta.9",
    "com.github.domala" %% "domala-paradise" % "0.1.0-beta.9" % Provided,
    "org.scalameta" %% "scalameta" % "1.8.0" % Provided,    
    // ... your other library dependencies
  ),
  // ... your other project settings
)
```

#### when not use annotation macro

`@Dao`, `@Entity`, `@Holder` can not be used under the setting below.
```scala

lazy val yourProject = project.settings(
  libraryDependencies ++= Seq(
    "com.github.domala" %% "domala" % "0.1.0-beta.9"
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
case class Address(city: String, street: String)
```

#### Entity
Classes whose instances can be stored in a database

`@Entity` can be omitted, but [some functions are restricted]((./notes/specification.md#entity-class)).

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

  @Insert
  def insert(person: Person): Result[Person]

  @Update
  def update(person: Person): Result[Person]
}
```

SQL can also be described in an external file.

When use external file, the file name must be as follows, and put it on the class path.

META-INF/_Dao package name_/_Dao class name_/_Dao method name_.sql



```scala
package example
trait PersonDao {
  @Select
  def selectByName(name: String): Seq[Person]
}
```
External SQL file for this method is as follows.

META-INF/example/PersonDao/selectByName.sql

```sql
select *
from person where
/*%if name != null */
  name like /* @prefix(name) */'%' escape '$'
/*%end*/
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
// load jdbc driver
Class.forName("org.h2.Driver")

// A configuration of database
// domala.jdbc.AdHocConfig works with AutoCommit mode.
implicit val config = jdbc.AdHocConfig("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")

// `script"..." ` builds a script query statement
script"create table emp(id int serial primary key, name varchar(20))".execute()

// `update"..." ` builds a `INSERT`, `UPDATE`, `DELETE` query statement
Seq("Scott", "Allen").map { name =>
  update"""
    insert into emp(name) values($name)
  """.execute()
}

// `select"..." ` builds a `SELECT` query statement
select"select * from emp order by id".getMapList
// => List(Map("ID" -> 1, "NAME" -> "Scott"), Map("ID" -> 2, "NAME" -> "Allen"))

select"select id from emp".getList[Int]
// => List(1, 2)

case class ID[E] (value: Int) extends AnyVal
case class Emp(id: ID[Emp], name: String) 
select"select * from emp order by id".getList[Emp]
// => List(Emp(ID(1),Scott), Emp(ID(2),Allen))

// EntityManager is assemble and execute SQL automatically from a entity type
jdbc.EntityManager.insert(Emp(ID(3), "Smith"))

select"select * from emp order by id".getList[Emp]
// => List(Emp(ID(1),Scott), Emp(ID(2),Allen), Emp(ID(3),Smith))

```

### Play integration sample

[domala-play-sample](https://github.com/bakenezumi/domala-play-sample)

License
--------
Apache License, Version 2.0
