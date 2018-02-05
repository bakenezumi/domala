- [Config class](#config-class)
- [Mapable types](#mapable-types)
- [Basic types](#basic-types)
- [Holder class](#holder-class)
- [Embeddable class](#embeddable-class)
- [Entity class](#entity-class)
- [Dao trait](#dao-trait)

## Config class

The configration for Domala is represented by the implementation class of the `domala.jdbc.Confing` trait.

### Configuration items
[See to the Doma guide.](http://doma.readthedocs.io/ja/2.19.0/config/)

### Example of definition
#### Simple case
This case is suitable for the following cases.
- Do not manage with DI container
- Use local transaction

Implementation example is follow.
```scala
object AppConfig extends LocalTransactionConfig (
  dataSource = new LocalTransactionDataSource(
    "jdbc:h2:mem:tutorial;DB_CLOSE_DELAY=-1", "sa", null),
  dialect = new H2Dialect,
  naming = Naming.SNAKE_LOWER_CASE
) {
  Class.forName("org.h2.Driver")
}
```

Using example is follow. The configuration object as `implicit val` in the scope is passed to the instance of [Dao](#dao-trait).

```scala
implicit val config: Config = AppConfig
val employeeDao EmployeeDao = EmployeeDao.impl
```

Can also pass it explicitly.

```scala
val employeeDao EmployeeDao = EmployeeDao.impl(AppConfig)
```

#### Advanced case
This case is suitable for the following cases.
- Manage as a singleton in the DI container
- Use transaction management function provided by other framework

Implementation example is follow.
```scala
@Singleton
class AppConfig @Inject() (dataSource: DataSource, dialect: Dialect) extends 
Config {
  override def getDialect: Dialect = dialect
  override def getDataSource: DataSource = dataSource  
}

@Singleton
class DaoProvider @Inject()(implicit config: Config) {
  lazy val employeeDao: EmployeeDao = EmployeeDao.impl
}
```
And register in the DI container.
Example for Play 2.6 is follow.

```scala
class AppModule extends Module {
  override def bindings(env: Environment, conf: Configuration) =
    Seq(
      bind[Dialect].toInstance(new H2Dialect),
      bind[Config].to(classOf[AppConfig]),
      bind[DaoProvider].toSelf
    )
}
```

Using example is follow. 

```scala
@Singleton
class App @Inject()(daoProvider: DaoProvider) {
  lazy val employeeDao: EmployeeDao = daoProvider.employeeDao
}
```


## Mapable types

> #### Select results
>> - [Basic types](#basic-types)
>> - [Holder class](#holder-class)
>> - [Entity class](#entity-class)
>> - Map [String, Any]
>> - Option [[Basic types](#basic-types)]
>> - Option [[Holder class](#holder-class)]
>> - Option [[Entity class](#entity-class)]
>> - Option [Map [String, Any]]
>> - Seq [[Basic types](#basic-types)]
>> - Seq [[Holder class](#holder-class)]
>> - Seq [[Entity class](#entity-class)]
>> - Seq [Map [String, Any]]
>> - Iterator [[Basic types](#basic-types)]
>> - Iterator [[Holder class](#holder-class)]
>> - Iterator [[Entity class](#entity-class)]
>> - Iterator [Map [String, Any]]
> #### SQL statement parameters
>> - [Basic types](#basic-types)
>> - [Holder class](#holder-class)
>> - Option [[Basic types](#basic-types)]
>> - Option [[Holder class](#holder-class)]
>> - Iterable [[Basic types](#basic-types)] // for like phrase
>> - Iterable [[Holder class](#holder-class)] // for like phrase
> #### Parameters of [Dao](#dao-trait)'s method for automatically generating SQL(Insert, Update, Delete)
>> - [Entity class](#entity-class)
>> - Iterable [[Entity class](#entity-class)]  // for batch


## Basic types

In Doma and Domala, define the Java type that can be mapped to a database column is `Basic types`.

In Domala, The following types are support.

type                   |Domala     |notes
-----------------------|-----------|-----
Boolean                |support    |
java.lang.Boolean      |support    |
~~Char~~               |-          |
~~java.lang.Char~~     |-          |
Short                  |support    |
java.lang.Short        |support    |
Int                    |support    |
javal.lang.Integer     |support    |
Long                   |support    |
java.lang.Long         |support    |
Float                  |support    |
java.lang.Float        |support    |
Double                 |support    |
java.lang.Double       |support    |
Double                 |support    |
~~java.lang.Enum~~     |-          |use seald abstruct [Holder](#holder-class) instead
Array[Byte]            |support|
String                 |support    |
AnyRef                 |support    |
java.lang.Object       |support    |
BigDecimal             |support    |added by Domala
java.math.BigDecimal   |support    |
BigInt                 |support    |added by Domala
java.math.BigInteger   |support    |
java.time.LocalDate    |support    |
java.time.LocalTime    |support    |
java.time.LocalDateTime|support    |
java.sql.Date          |support    |
java.sql.Time          |support    |
java.sql.Timestamp     |support    |
java.sql.Array         |support    |
java.sql.Blob          |support    |
java.sql.Clob          |support    |
java.sql.SQLXML        |support    |
java.util.Date         |support    |

## Holder class

`Doma` can define a class wrapping a Basic type value as `Holder`.

Using `Holder` can defines type for each semantic.

There are two ways to define a `Holder`:
  - to annotate `@Holder` to  `case class`
  - using [`value classes`](https://docs.scala-lang.org/overviews/core/value-classes.html).

### Example of definition
  ```scala
  // using the annotation
  @Holder
  case class PhoneNumber(value: String) {
    assert(value != null)
    def getAreaCode: String = ...
    def validate: Either[Cause, PhoneNumber] = ...
  }

  // using value classes
  case class Identity[ENTITY](value: Int) extends AnyVal

  ```

Domala can not use `java.lang.Enum` as a basic type, but can be expressed pseudo by `seald abstract class`.

  ```scala
  // a value of a element is persisted in the database
  @Holder
  sealed abstract class Sex(val value: String)
  object Sex {
    case object Male extends Sex("M")
    case object Female extends Sex("F")
    case object Other extends Sex("?")
  }
  ```

### Example of using
If the Holder class has a type parameter, the type parameter requires a concrete type. Specification of wildcards and type variables is not supported.

  ```scala
  @Entity
  case class Employee (

    @Id
    employeeId: Identity[Employee],

    employeeName: String,

    phoneNumber: PhoneNumber,

    sex: Sex,

    @Version
    versionNo: Int,
    ...
  )
  ```

  ```scala
  @Dao
  trait EmployeeDao {

      @Select(sql="...")
      def selectById(employeeId: Identity[Employee]): Employee

      @Select(sql="...")
      def selectByPhoneNumber(phoneNumber: PhoneNumber): Employee
      ...
  }  
  ```
  
  
If the element of the `Holder` is `Numeric`, the `Holder` can also be used as `Numeric`.
  ```scala
  @Holder
  case class IntHolder(value: Int)
  val holders = (1 to 10).map(v => IntHolder(v))
  holders.sum // => IntHolder(55)
  // use operators
  import Numeric.Implicits._
  IntHolder(10) - IntHolder(1) // IntHolder(9)
  ```


### Restrictions
  - One constructor, and one element.
  - Only [`Basic types`](#basic-types) can be defined for element type.
  - Doma's `External holder` is currently not supported.

## Embeddable class

`Embeddable` is that groups database columns and result sets of queries into multiple columns.

`Embeddable` can be defined by `case class`.

### Example of definition

  ```scala
  case class Address(
    city: String,
    street: String,
    @Column(name = "ZIP_CODE")
    zip: String
  )
  ```

### Example of using
`Embeddable` classes are used as fields of `Entity` classes.

  ```scala
  @Entity
  case class Employee(
    @Id
    id: Integer,
    address: Address,
  )
  ```

### Restrictions

The type of the field must be one of the following:

  - [`Basic types`](#basic-types)
  - [`Holder class`](#holder-class)
  - `Option` with element of either [`Basic type`](#basic-types) or [`Holder class`](#holder-class)

### Further specification of Embeddable
[See to the Doma guide.](http://doma.readthedocs.io/ja/2.19.0/embeddable/)

## Entity class

An `Entity` corresponds to a database table or a query result set.

`Entity` can be defined by to annote `@Entity` to `case class`.

`@Entity` can be omitted, but some functions are restricted.

### Example of definition

  ```scala
  @Entity
  case class Employee(
    @Id
    employeeId: Identity[Employee],
    ...
  )
  ```

### Restrictions
The type of the field must be one of the following:

  - [`Basic types`](#basic-types)
  - [`Holder class`](#holder-class)
  - [`Embeddable class`](#embeddable-class)
  - `Option` with element of either [`Basic type`](#basic-types) or [`Holder class`](#holder-class)

The following functions of Doma can not be used by Domala.
  - [`@Transient`](http://doma.readthedocs.io/ja/2.19.0/entity/#id16)
  - [`@OriginalStates`](http://doma.readthedocs.io/ja/2.19.0/entity/#id17)
  - Definition of an `Entity` that inherits an `Entity` class

When @Entity annotation is omitted, the following functions can not be used.
  - Using `EntityListener`
  - Individual `NamingType` changes
  - Specify `GenerationType.SEQUENCE` with `GeneratedValue` annotation
  - Specify `GenerationType.TABLE` with `GeneratedValue` annotation


### Further specification of Entity
[See to the Doma guide.](http://doma.readthedocs.io/ja/2.19.0/entity/)


## Dao trait

Data Access Object (Dao) is an interface for database access.

`Dao` can be defined by to annote `@Dao` to `trait`.

The implementation class of trait is automatically generated at compile time by the annotation macro.

### Example of definition

  ```scala
  @Dao
  trait EmployeeDao {

    @Select(sql = """
  select *
  from employee
  where employeeId = /*employeeId*/0
    """)
    def selectById(employeeId: Identity[Employee]): Employee

    @Insert
    def insert(employee: Employee): Result[Employee]

    @Update
    def update(employee: Employee): Result[Employee]

  }
  ```

### Example of using
When compiled, an implementation class is generated by the annotation macro. Instantiate the implementation class and use it. However, when managing the `Config class` with the DI container, please control the instantiation with the DI container.

  ```scala
  implicit val config: Config = AppConfig
  val employeeDao: EmployeeDao = EmployeeDao.impl
  val employee = employeeDao.selectById(Identity(1))
  ```

The implementation class is included in the companion object of the trait annotating `@Dao`. Instantiate it using the `impl` method of companion object. This `impl` accepts the [Config class](#config-class) instance as an implicit parameter.

It is also possible to instantiate by specifying a specific `javax.sql.DataSource`.

  ```scala
  val dataSource: DataSource = ...
  val employeeDao: EmployeeDao = EmployeeDao.impl(dataSource)
  val employee = employeeDao.selectById(Identity(1))
  ```

Likewise, it is possible to instantiate it by specifying `java.sql.Connection`.

  ```scala
  val connection: Connection = ...
  val employeeDao: EmployeeDao = EmployeeDao.impl(connection)
  val employee = employeeDao.selectById(Identity(1))
  ```

`Dao trait` is not tied to entity class on a one-on-one basis. A single `Dao trait` can handle multiple `Entity` classes.
