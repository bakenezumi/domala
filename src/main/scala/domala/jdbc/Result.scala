package domala.jdbc

case class Result[ENTITY](count: Int, entity: ENTITY)
