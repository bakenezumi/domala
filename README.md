Domala: Doma for Scala
======================

Domala is a database access library for Scala. This wraps [Doma2](https://github.com/domaframework/doma).

- Domala uses [scala meta](http://scalameta.org/paradise/) to generate code and validate sql mappings at **compile time**.

- Select statements are write by yourself. It is automatically mapped to `Option[`*`Entity`*`]`, `Seq[`*`Entity`*`]`, `Stream[`*`Entity`*`]`, `Seq[Map[String, Any]]`, and more.

- Other statements are automatically generated from Entity. It can also write SQL.

### Example

#### Config

```scala
object SampleConfig extends Config(
  dataSource = new LocalTransactionDataSource(
    "jdbc:h2:mem:sample;DB_CLOSE_DELAY=-1", "sa", null),
  dialect = new H2Dialect,
  naming = Naming.SNAKE_LOWER_CASE
) {
  Class.forName("org.h2.Driver")
}
```

#### Entity

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

#### Holder

```scala
@Holder
case class Name(value: String)
```

#### Embeddable

```scala
@Embeddable
case class Address(city: String, street: String)
```

#### Dao

```scala
@Dao(config = SampleConfig)
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

#### Usage
```scala
implicit val config = SampleConfig

// Dao implementation is auto generated.
val dao: PersonDao = PersonDao.impl 

val entity = Person(
  id = 1,
  name = Name("SMITH"),
  age = Some(10),
  address = Address("TOKYO", "YAESU"),
  departmentId = Some(1)
)
Required {
  dao.insert(entity)
  dao.selectById(1).foreach(e =>
    dao.update(e.copy(age = e.age.map(_ + 1)))
  )
}
```

### Run sample

```sh
sbt
>sample/run
```

License
--------
Apache License, Version 2.0