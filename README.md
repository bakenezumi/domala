# Domala(Doma for Scala)
------------------
DomalaはJavaのDBアクセスフレームワーク[Doma2](https://github.com/domaframework/doma)をScala用にラップしたライブラリです。

### 特徴

基本的には本家Doma2の特徴を引き継いでいますが、下記の拡張を加えています。

- scala metaを使用してコードの自動生成とコードの検証を行う

- Scalaの`scala.Option`や`scala.collection.Seq`、`scala.collection.Stream`を利用できる

- SQLファイルは使用せず、アノテーションパラメータに記述されたSQLを実行する（ヒアドキュメント形式で書けるため）

### 利用例

#### エンティティクラス

```scala
@Entity
case class Person(
  @Id
  id: Option[Int] = None,
  name: Name,
  age: Option[Int],
  @Embedded
  address: Address,
  departmentId: Option[Int],
  @Version
  version: Option[Int] = Some(-1)
)
```

#### ドメインクラス

```scala
@Domain
case class Name(value: String)
```

#### エンベッダブルクラス

```scala
@Embeddable
case class Address(city: String, street: String)
```

#### Daoクラス

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
}
```

#### Daoの利用
```scala
implicit val config = SampleConfig

val dao: PersonDao = PersonDao

val entity = Person(
  id = 1
  name = Name("SMITH"),
  age = Some(10),
  address = Address("TOKYO", "YAESU"),
  departmentId = Some(1)
)
Required {
  dao.insert(entity)
  dao.selectById(1)
}
```


### サンプルアプリの実行方法

```sh
sbt
>sample/run
```
