package domala.jdbc

case class BatchResult[ENTITY](count: Array[Int], entity: Seq[ENTITY])
