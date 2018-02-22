package domala.async.jdbc

/** When write processing to DB asynchronously,
  * It is necessary to mix-in this to Config of DAO */
trait AsyncWritable { self: AsyncConfig => }
