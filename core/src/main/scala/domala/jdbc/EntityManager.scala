package domala.jdbc

import java.lang.reflect.Method

import domala.internal.jdbc.dao.DaoUtil
import domala.internal.macros.reflect.EntityManagerMacros

import scala.language.experimental.macros

/** Assemble and execute SQL automatically from a entity type
  *
  * {{{
  * implicit val config: Config = ...
  * val employee: Employee = ...
  * Required {
  *   EntityManager.insert(employee)
  * }
  * }}}
  *
  */
object EntityManager {
  /** Assemble and execute a insert SQL automatically from a entity type.
    *
    * @tparam ENTITY a entity type
    * @param entity a entity
    * @param config a DB connection configuration
    */
  def insert[ENTITY](entity: ENTITY)(implicit config: Config): Result[ENTITY] = macro EntityManagerMacros.insert[ENTITY]
  /** Assemble and execute a update SQL automatically from a entity type. A entity type requires @ID annotation.
    *
    * @tparam ENTITY a entity type
    * @param entity a entity
    * @param config a DB connection configuration
    */
  def update[ENTITY](entity: ENTITY)(implicit config: Config): Result[ENTITY] = macro EntityManagerMacros.update[ENTITY]
  /** Assemble and execute a delete SQL automatically from a entity type. A entity type requires @ID annotation.
    *
    * @tparam ENTITY a entity type
    * @param entity a entity
    * @param config a DB connection configuration
    */
  def delete[ENTITY](entity: ENTITY)(implicit config: Config): Result[ENTITY] = macro EntityManagerMacros.delete[ENTITY]

  /** Assemble and execute a batch insert SQL automatically from a entity type.
    *
    * @tparam ENTITY a entity type
    * @param entities iterable entities
    * @param config a DB connection configuration
    */
  def batchInsert[ENTITY](entities: Iterable[ENTITY])(implicit config: Config): BatchResult[ENTITY] = macro EntityManagerMacros.batchInsert[ENTITY]
  /** Assemble and execute a batch update SQL automatically from a entity type. A entity type requires @ID annotation.
    *
    * @tparam ENTITY a entity type
    * @param entities iterable entities
    * @param config a DB connection configuration
    */
  def batchUpdate[ENTITY](entities: Iterable[ENTITY])(implicit config: Config): BatchResult[ENTITY] = macro EntityManagerMacros.batchUpdate[ENTITY]
  /** Assemble and execute a batch delete SQL automatically from a entity type. A entity type requires @ID annotation.
    *
    * @tparam ENTITY a entity type
    * @param entities iterable entities
    * @param config a DB connection configuration
    */
  def batchDelete[ENTITY](entities: Iterable[ENTITY])(implicit config: Config): BatchResult[ENTITY] = macro EntityManagerMacros.batchDelete[ENTITY]
}

object EntityManagerMethods {
  val insertMethod: Method = DaoUtil.getDeclaredMethod(classOf[EntityManager], "insert", classOf[Any], classOf[Config])
  val updateMethod: Method = DaoUtil.getDeclaredMethod(classOf[EntityManager], "update", classOf[Any], classOf[Config])
  val deleteMethod: Method = DaoUtil.getDeclaredMethod(classOf[EntityManager], "delete", classOf[Any], classOf[Config])
  val batchInsertMethod: Method = DaoUtil.getDeclaredMethod(classOf[EntityManager], "batchInsert", classOf[Any], classOf[Config])
  val batchUpdateMethod: Method = DaoUtil.getDeclaredMethod(classOf[EntityManager], "batchUpdate", classOf[Any], classOf[Config])
  val batchDeleteMethod: Method = DaoUtil.getDeclaredMethod(classOf[EntityManager], "batchDelete", classOf[Any], classOf[Config])
}

// Dummy trait for logging
trait EntityManager {
  def insert[ENTITY](entity: ENTITY)(implicit config: Config): Result[ENTITY]
  def update[ENTITY](entity: ENTITY)(implicit config: Config): Result[ENTITY]
  def delete[ENTITY](entity: ENTITY)(implicit config: Config): Result[ENTITY]

  def batchInsert[ENTITY](entities: Iterable[ENTITY])(implicit config: Config): BatchResult[ENTITY]
  def batchUpdate[ENTITY](entities: Iterable[ENTITY])(implicit config: Config): BatchResult[ENTITY]
  def batchDelete[ENTITY](entities: Iterable[ENTITY])(implicit config: Config): BatchResult[ENTITY]
}
