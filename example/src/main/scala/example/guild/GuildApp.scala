package example.guild

import domala._
import domala.jdbc.{BatchResult, Naming, SelectOptions}


/** Object structure example
  * {{{
  * Guild : 1 ------ N   : Character
  * Guild : 1 ------ 0-1 : GuildHouse
  * }}}
  * @see [[http://gakuzzzz.github.io/slides/doma_practice/#31]]
  * */
case class GuildView(
  meta: Guild,
  members: List[Character],
  house: Option[GuildHouse]
)

class GuildService (implicit guildDao: GuildDao, characterDao: CharacterDao, guildHouseDao: GuildHouseDao) {

  def getAllGuildViews: List[GuildView] = {
    val opt = SelectOptions.get.limit(100).offset(0)
    val guilds = guildDao.findAll[List[Guild]](opt, _.toList)
    val ids = guilds.map(_.id)
    val chars = // 1:N は Map[ID, List] で
      characterDao.findByGuildIds(ids, _.toList.groupBy(_.guildId))
    val houses = // 1:0-1 は Map[ID, Entity] で
      guildHouseDao.findByGuildIds(ids, _.map(house => house.guildId -> house).toMap)
    guilds.map(buildGuild(chars, houses))
  }

  private def buildGuild(characters: Map[ID[Guild], List[Character]], houses: Map[ID[Guild], GuildHouse]): Guild => GuildView = {
    (guild: Guild) =>
      GuildView(
        guild,
        characters.getOrElse(guild.id, Nil),
        houses.get(guild.id)
      )
  }

}

case class ID[ENTITY](value: Int) extends AnyVal

case class Name(value: String) extends AnyVal

case class Guild(
  id: ID[Guild],
  name: Name
)

case class Character(
  id: ID[Character],
  name: Name,
  guildId: ID[Guild]
)

case class GuildHouse(
  id: ID[GuildHouse],
  name: Name,
  guildId: ID[Guild]
)

@Dao
trait GuildDao {
  @Select("""
SELECT /*%expand*/* FROM guild WHERE deleted_time IS NULL
  """, strategy = SelectType.STREAM)
  def findAll[R](opt: SelectOptions, mapper: Stream[Guild] => R): R

  @BatchInsert
  def save(entity: Seq[Guild]): BatchResult[Guild]
}

@Dao
trait CharacterDao {
  @Select("""
SELECT
  /*%expand*/*
FROM
  `character`
WHERE
  guild_id IN /*guildIds*/(1, 2)
  AND deleted_time IS NULL
  """, strategy = SelectType.STREAM)
  def findByGuildIds[R](guildIds: Iterable[ID[Guild]], mapper: Stream[Character] => R): R

  @BatchInsert
  def save(entity: Seq[Character]): BatchResult[Character]
}

@Dao
trait GuildHouseDao {
  @Select("""
SELECT
  /*%expand*/*
FROM
  guild_house
WHERE
  guild_id IN /*guildIds*/(1, 2)
  AND deleted_time IS NULL
  """, strategy = SelectType.STREAM)
  def findByGuildIds[R](guildIds: Iterable[ID[Guild]], mapper: Stream[GuildHouse] => R): R

  @BatchInsert
  def save(entity: Seq[GuildHouse]): BatchResult[GuildHouse]
}

object GuildApp {
  import domala.jdbc.Config
  import example.util.prettyPrint

  implicit val config: Config = GuildAppConfig
  implicit val guildDao: GuildDao = GuildDao.impl
  implicit val characterDao: CharacterDao = CharacterDao.impl
  implicit val guildHouseDao: GuildHouseDao = GuildHouseDao.impl
  val app = new GuildService
  val envDao: EnvDao = EnvDao.impl
  val service = new GuildService()

  def run(): Unit = Required {
    init()
    println(prettyPrint(service.getAllGuildViews))
    terminate()
  }

  def init(): Unit = {
    envDao.create()

    // 4 guilds
    val guilds = (1 to 4).map(i => Guild(ID(i), Name("g" + i)))

    // 10 characters
    val characters = (1 to 10)
      .zip(
        Stream.continually(guilds.flatMap {
          case Guild(id @ ID(2), _) => Seq(id, id)
          case Guild(     ID(3), _) => Nil
          case Guild(id,         _) => Seq(id)
        }).flatten
      ).map {
        case (id, gid) => Character(ID(id), Name("c" + id), gid)
      }

    // 2 houses
    val houses = (1 to 2).map(i => GuildHouse(ID(i), Name("gh" + i), ID(i * 2 - 1)))
    guildDao.save(guilds)
    characterDao.save(characters)
    guildHouseDao.save(houses)
  }

  def terminate(): Unit = {
    envDao.drop()
  }

  @Dao
  trait EnvDao {
    @Script(
      """
CREATE TABLE guild(id INT NOT NULL IDENTITY PRIMARY KEY, name VARCHAR(20), deleted_time timestamp);
CREATE TABLE `character`(id INT NOT NULL IDENTITY PRIMARY KEY, name VARCHAR(20), guild_id INT, deleted_time timestamp,
  constraint fk_character_guild_id foreign key(guild_id) references guild(id));
CREATE TABLE guild_house(id INT NOT NULL IDENTITY PRIMARY KEY, name VARCHAR(20), guild_id INT, deleted_time timestamp,
  constraint fk_guild_house_guild_id foreign key(guild_id) references guild(id));
""")
    def create()
    @Script("""
DROP TABLE guild_house;
DROP TABLE `character`;
DROP TABLE guild;
""")
    def drop()
  }
}

import domala.jdbc.LocalTransactionConfig
import domala.jdbc.dialect.H2Dialect
import domala.jdbc.tx.LocalTransactionDataSource

object GuildAppConfig extends LocalTransactionConfig(
  dataSource =  new LocalTransactionDataSource(
    "jdbc:h2:mem:guild;DB_CLOSE_DELAY=-1", "", ""),
  dialect = new H2Dialect,
  naming = Naming.SNAKE_LOWER_CASE
) {
  Class.forName("org.h2.Driver")
}