package example.guild

import domala._
import domala.jdbc.Config
import org.scalatest.{BeforeAndAfter, FunSuite}

class GuildTestSuite extends FunSuite with BeforeAndAfter {
  implicit val config: Config = GuildAppConfig
  implicit val guildDao: GuildDao = GuildDao.impl
  implicit val characterDao: CharacterDao = CharacterDao.impl
  implicit val guildHouseDao: GuildHouseDao = GuildHouseDao.impl
  val app = new GuildService
  val envDao: GuildApp.EnvDao = GuildApp.EnvDao.impl

  before {
    Required {
      envDao.create()
    }
  }

  after {
    Required {
      envDao.drop()
    }
  }

  test("make guild view") {
    Required {
      val guilds = (1 to 10).map(i => Guild(ID(i), Name("g" + i)))
      val characters = (1 to 50)
        .zip(
          Stream
            .continually(guilds
              .flatMap {
                case Guild(id @ ID(3), _) => Seq(id, id, id)
                case Guild(id @ ID(5), _) => Seq(id, id)
                case Guild(ID(4), _) | Guild(ID(8), _) => Nil
                case Guild(id, _)         => Seq(id)
              })
            .flatten
        )
        .map {
          case (id, gid) => Character(ID(id), Name("c" + id), gid)
        }
      val houses =
        (1 to 5).map(i => GuildHouse(ID(i), Name("gh" + i), ID(i * 2)))
      guildDao.save(guilds)
      characterDao.save(characters)
      guildHouseDao.save(houses)
      val views = app.getAllGuildViews
      assert(
        views == List(
          GuildView(
            Guild(ID(1), Name("g1")),
            List(
              Character(ID(1), Name("c1"), ID(1)),
              Character(ID(12), Name("c12"), ID(1)),
              Character(ID(23), Name("c23"), ID(1)),
              Character(ID(34), Name("c34"), ID(1)),
              Character(ID(45), Name("c45"), ID(1))
            ),
            None
          ),
          GuildView(
            Guild(ID(2), Name("g2")),
            List(
              Character(ID(2), Name("c2"), ID(2)),
              Character(ID(13), Name("c13"), ID(2)),
              Character(ID(24), Name("c24"), ID(2)),
              Character(ID(35), Name("c35"), ID(2)),
              Character(ID(46), Name("c46"), ID(2))
            ),
            Some(GuildHouse(ID(1), Name("gh1"), ID(2)))
          ),
          GuildView(
            Guild(ID(3), Name("g3")),
            List(
              Character(ID(3), Name("c3"), ID(3)),
              Character(ID(4), Name("c4"), ID(3)),
              Character(ID(5), Name("c5"), ID(3)),
              Character(ID(14), Name("c14"), ID(3)),
              Character(ID(15), Name("c15"), ID(3)),
              Character(ID(16), Name("c16"), ID(3)),
              Character(ID(25), Name("c25"), ID(3)),
              Character(ID(26), Name("c26"), ID(3)),
              Character(ID(27), Name("c27"), ID(3)),
              Character(ID(36), Name("c36"), ID(3)),
              Character(ID(37), Name("c37"), ID(3)),
              Character(ID(38), Name("c38"), ID(3)),
              Character(ID(47), Name("c47"), ID(3)),
              Character(ID(48), Name("c48"), ID(3)),
              Character(ID(49), Name("c49"), ID(3))
            ),
            None
          ),
          GuildView(Guild(ID(4), Name("g4")),
                    Nil,
                    Some(GuildHouse(ID(2), Name("gh2"), ID(4)))),
          GuildView(
            Guild(ID(5), Name("g5")),
            List(
              Character(ID(6), Name("c6"), ID(5)),
              Character(ID(7), Name("c7"), ID(5)),
              Character(ID(17), Name("c17"), ID(5)),
              Character(ID(18), Name("c18"), ID(5)),
              Character(ID(28), Name("c28"), ID(5)),
              Character(ID(29), Name("c29"), ID(5)),
              Character(ID(39), Name("c39"), ID(5)),
              Character(ID(40), Name("c40"), ID(5)),
              Character(ID(50), Name("c50"), ID(5))
            ),
            None
          ),
          GuildView(
            Guild(ID(6), Name("g6")),
            List(Character(ID(8), Name("c8"), ID(6)),
                 Character(ID(19), Name("c19"), ID(6)),
                 Character(ID(30), Name("c30"), ID(6)),
                 Character(ID(41), Name("c41"), ID(6))),
            Some(GuildHouse(ID(3), Name("gh3"), ID(6)))
          ),
          GuildView(
            Guild(ID(7), Name("g7")),
            List(Character(ID(9), Name("c9"), ID(7)),
                 Character(ID(20), Name("c20"), ID(7)),
                 Character(ID(31), Name("c31"), ID(7)),
                 Character(ID(42), Name("c42"), ID(7))),
            None
          ),
          GuildView(Guild(ID(8), Name("g8")),
                    Nil,
                    Some(GuildHouse(ID(4), Name("gh4"), ID(8)))),
          GuildView(
            Guild(ID(9), Name("g9")),
            List(Character(ID(10), Name("c10"), ID(9)),
                 Character(ID(21), Name("c21"), ID(9)),
                 Character(ID(32), Name("c32"), ID(9)),
                 Character(ID(43), Name("c43"), ID(9))),
            None
          ),
          GuildView(
            Guild(ID(10), Name("g10")),
            List(Character(ID(11), Name("c11"), ID(10)),
                 Character(ID(22), Name("c22"), ID(10)),
                 Character(ID(33), Name("c33"), ID(10)),
                 Character(ID(44), Name("c44"), ID(10))),
            Some(GuildHouse(ID(5), Name("gh5"), ID(10)))
          )
        ))
    }
  }
  test("nil select") {
    Required {
      assert(guildHouseDao.findByGuildIds(Nil, _.toList).isEmpty)
    }
  }

}
