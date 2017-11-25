package domala.jdbc

import org.seasar.doma.internal.jdbc.sql.SqlParser
import org.seasar.doma.jdbc.SqlNode

/** A repository for parsed result of Sql.
  *
  * The implementation class must be thread safe.
  */
trait SqlNodeRepository {
  def get(sql: String): SqlNode
  def clearCache()
}

object GreedyCacheSqlNodeRepository extends SqlNodeRepository {
  private[this] val cache = scala.collection.concurrent.TrieMap[String, SqlNode]()
  override def get(sql: String): SqlNode = cache.getOrElseUpdate(sql, new SqlParser(sql).parse())
  override def clearCache(): Unit = cache.clear()
}
