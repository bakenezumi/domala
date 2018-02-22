package domala.async.jdbc

import java.lang.reflect.Method

import domala.internal.jdbc.dao.DaoUtil
import domala.internal.macros.reflect.AsyncEntityManagerMacros

import scala.language.experimental.macros

/** Generate and execute SQL automatically from a entity type
  *
  * {{{
  * implicit val config: AsyncConfig with AsyncWritable = ...
  * val employee: Employee = ...
  * Async {
  *   AsyncEntityManager.insert(employee)
  * }
  * }}}
  *
  */
object AsyncEntityManager {

  /** Generate and execute a insert SQL automatically from a entity type.
    *
    * @tparam ENTITY a entity type
    * @param entity a entity
    * @param config a DB connection configuration
    */
  def insert[ENTITY](entity: ENTITY)(implicit config: AsyncConfig with AsyncWritable): AsyncResult[ENTITY] = macro AsyncEntityManagerMacros.insert[ENTITY]

  /** Generate and execute a update SQL automatically from a entity type. A entity type requires @ID annotation.
    *
    * @tparam ENTITY a entity type
    * @param entity a entity
    * @param config a DB connection configuration
    */
  def update[ENTITY](entity: ENTITY)(implicit config: AsyncConfig with AsyncWritable): AsyncResult[ENTITY] = macro AsyncEntityManagerMacros.update[ENTITY]

  /** Generate and execute a delete SQL automatically from a entity type. A entity type requires @ID annotation.
    *
    * @tparam ENTITY a entity type
    * @param entity a entity
    * @param config a DB connection configuration
    */
  def delete[ENTITY](entity: ENTITY)(implicit config: AsyncConfig with AsyncWritable): AsyncResult[ENTITY] = macro AsyncEntityManagerMacros.delete[ENTITY]

  /** Generate and execute a batch insert SQL automatically from a entity type.
    *
    * @tparam ENTITY a entity type
    * @param entities iterable entities
    * @param config a DB connection configuration
    */
  def batchInsert[ENTITY](entities: Iterable[ENTITY])(implicit config: AsyncConfig with AsyncWritable): AsyncBatchResult[ENTITY] = macro AsyncEntityManagerMacros.batchInsert[ENTITY]

  /** Generate and execute a batch update SQL automatically from a entity type. A entity type requires @ID annotation.
    *
    * @tparam ENTITY a entity type
    * @param entities iterable entities
    * @param config a DB connection configuration
    */
  def batchUpdate[ENTITY](entities: Iterable[ENTITY])(implicit config: AsyncConfig with AsyncWritable): AsyncBatchResult[ENTITY] = macro AsyncEntityManagerMacros.batchUpdate[ENTITY]

  /** Generate and execute a batch delete SQL automatically from a entity type. A entity type requires @ID annotation.
    *
    * @tparam ENTITY a entity type
    * @param entities iterable entities
    * @param config a DB connection configuration
    */
  def batchDelete[ENTITY](entities: Iterable[ENTITY])(implicit config: AsyncConfig with AsyncWritable): AsyncBatchResult[ENTITY] = macro AsyncEntityManagerMacros.batchDelete[ENTITY]
}

object AsyncEntityManagerMethods {
  val insertMethod: Method = DaoUtil.getDeclaredMethod(classOf[AsyncEntityManager], "insert", classOf[Any], classOf[AsyncConfig])
  val updateMethod: Method = DaoUtil.getDeclaredMethod(classOf[AsyncEntityManager], "update", classOf[Any], classOf[AsyncConfig])
  val deleteMethod: Method = DaoUtil.getDeclaredMethod(classOf[AsyncEntityManager], "delete", classOf[Any], classOf[AsyncConfig])
  val batchInsertMethod: Method = DaoUtil.getDeclaredMethod(classOf[AsyncEntityManager], "batchInsert", classOf[Any], classOf[AsyncConfig])
  val batchUpdateMethod: Method = DaoUtil.getDeclaredMethod(classOf[AsyncEntityManager], "batchUpdate", classOf[Any], classOf[AsyncConfig])
  val batchDeleteMethod: Method = DaoUtil.getDeclaredMethod(classOf[AsyncEntityManager], "batchDelete", classOf[Any], classOf[AsyncConfig])
}

// Dummy trait for logging
trait AsyncEntityManager {
  def insert[ENTITY](entity: ENTITY)(implicit config: AsyncConfig with AsyncWritable): AsyncResult[ENTITY]
  def update[ENTITY](entity: ENTITY)(implicit config: AsyncConfig with AsyncWritable): AsyncResult[ENTITY]
  def delete[ENTITY](entity: ENTITY)(implicit config: AsyncConfig with AsyncWritable): AsyncResult[ENTITY]

  def batchInsert[ENTITY](entities: Iterable[ENTITY])(implicit config: AsyncConfig with AsyncWritable): AsyncBatchResult[ENTITY]
  def batchUpdate[ENTITY](entities: Iterable[ENTITY])(implicit config: AsyncConfig with AsyncWritable): AsyncBatchResult[ENTITY]
  def batchDelete[ENTITY](entities: Iterable[ENTITY])(implicit config: AsyncConfig with AsyncWritable): AsyncBatchResult[ENTITY]
}
