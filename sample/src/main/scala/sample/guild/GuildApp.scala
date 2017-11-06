package sample.guild

import domala._
import domala.jdbc.{BatchResult, SelectOptions}

/** Object structure example
  * {{{
  * Guild : 1 ------ N   : Character
  * Guild : 1 ------ 0-1 : GuildHouse
  * }}}
  * @see [[http://gakuzzzz.github.io/slides/doma_practice/#1]]
  * */
class GuildApp (implicit guildDao: GuildDao, characterDao: CharacterDao, guildHouseDao: GuildHouseDao) {

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

case class GuildView(
  meta: Guild,
  members: List[Character],
  house: Option[GuildHouse]
)

@Holder
case class ID[ENTITY](value: Int)

@Holder
case class Name(value: String)

@Entity
case class Guild(
  id: ID[Guild],
  name: Name
)

@Entity
case class Character(
  id: ID[Character],
  name: Name,
  guildId: ID[Guild]
)

@Entity
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
  def load(entity: Seq[Guild]): BatchResult[Guild]
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
  def load(entity: Seq[Character]): BatchResult[Character]
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
  def load(entity: Seq[GuildHouse]): BatchResult[GuildHouse]
}
