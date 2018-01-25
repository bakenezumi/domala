package domala.jdbc

/**
  * A batch processing result for immutable entities.
  *
  * @tparam ENTITY
  *            the entity type
  * @param counts
  *            the array of the affected row count
  * @param entities
  *            the entity list
  */
case class BatchResult[ENTITY](counts: Array[Int], entities: Seq[ENTITY])
