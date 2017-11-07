package domala.jdbc

import org.seasar.doma.internal.jdbc.sql.SqlParser
import org.seasar.doma.jdbc.SqlNode

trait SqlNodeRepository {
  def get(sql: String): SqlNode
  def clearCache()
}

object GreedyCacheSqlFileRepository extends SqlNodeRepository {
  private val cache = scala.collection.concurrent.TrieMap[String, SqlNode]()
  override def get(sql: String): SqlNode = cache.getOrElseUpdate(sql, new SqlParser(sql).parse())
  override def clearCache(): Unit = cache.clear()
}
