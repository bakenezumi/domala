package domala.jdbc

/**
  * A processing result for an immutable entity.
  *
  * @tparam ENTITY
  *            the entity type
  * @param count
  *            the affected row count
  * @param entity
  *            the entity
  */
case class Result[ENTITY](count: Int, entity: ENTITY)
