package domala.jdbc.builder

import org.seasar.doma.internal.jdbc.sql.SqlParser
import org.seasar.doma.internal.util.AssertionUtil.assertUnreachable

import scala.collection.mutable.ArrayBuffer

class BuildingHelper {
  private val lineSeparator = System.getProperty("line.separator")

  private val items = ArrayBuffer[Item]()

  private[builder] def appendSql(sql: String): Unit = {
    items += Item.sql(sql)
  }

  private[builder] def appendSqlWithLineSeparator(sql: String): Unit = {
    if (items.isEmpty) items += Item.sql(sql)
    else items += Item.sql(lineSeparator + sql)
  }

  private[builder] def appendParam(param: Param): Unit = {
    items += Item.param(param)
  }

  private[builder] def removeLast(): Unit = {
    if (items.nonEmpty) items.remove(items.size - 1)
  }

  private[builder] def getParams = {
    items.filter(_.kind == ItemKind.PARAM).map(_.param)
  }

  private[builder] def getSqlNode = {
    val buf = new StringBuilder(200)
    for (item <- items) {
      item.kind match {
        case ItemKind.SQL =>
          buf.append(item.sql)
        case ItemKind.PARAM =>
          buf.append("/*")
          if (item.param.literal) buf.append("^")
          buf.append(item.param.name)
          buf.append("*/0")
        case _ =>
          assertUnreachable
      }
    }
    val parser = new SqlParser(buf.toString)
    parser.parse
  }

  private case class Item(kind: ItemKind, sql: String, param: Param)
  private object Item {
    def sql(sql: String): Item = Item(ItemKind.SQL, sql, null)
    def param(param: Param): Item = Item(ItemKind.PARAM, null, param)
  }


  private[builder] sealed class ItemKind
  private object ItemKind {

    object SQL extends ItemKind

    object PARAM extends ItemKind

  }

}
