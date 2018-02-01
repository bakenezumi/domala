package domala.jdbc

import java.lang.reflect.Method

import domala.internal.jdbc.dao.DaoUtil
import domala.internal.macros.reflect.EntityManagerMacros

import scala.language.experimental.macros


object EntityManager {
  val insertMethod: Method = DaoUtil.getDeclaredMethod(classOf[EntityManager], "insert", classOf[Any], classOf[Config])
  val updateMethod: Method = DaoUtil.getDeclaredMethod(classOf[EntityManager], "update", classOf[Any], classOf[Config])
  val deleteMethod: Method = DaoUtil.getDeclaredMethod(classOf[EntityManager], "delete", classOf[Any], classOf[Config])

  def insert[ENTITY](entity: ENTITY)(implicit config: Config): Result[ENTITY] = macro EntityManagerMacros.insert[ENTITY]
  def update[ENTITY](entity: ENTITY)(implicit config: Config): Result[ENTITY] = macro EntityManagerMacros.update[ENTITY]
  def delete[ENTITY](entity: ENTITY)(implicit config: Config): Result[ENTITY] = macro EntityManagerMacros.delete[ENTITY]

  val batchInsertMethod: Method = DaoUtil.getDeclaredMethod(classOf[EntityManager], "batchInsert", classOf[Any], classOf[Config])
  val batchUpdateMethod: Method = DaoUtil.getDeclaredMethod(classOf[EntityManager], "batchUpdate", classOf[Any], classOf[Config])
  val batchDeleteMethod: Method = DaoUtil.getDeclaredMethod(classOf[EntityManager], "batchDelete", classOf[Any], classOf[Config])

  def batchInsert[ENTITY](entities: Iterable[ENTITY])(implicit config: Config): BatchResult[ENTITY] = macro EntityManagerMacros.batchInsert[ENTITY]
  def batchUpdate[ENTITY](entities: Iterable[ENTITY])(implicit config: Config): BatchResult[ENTITY] = macro EntityManagerMacros.batchUpdate[ENTITY]
  def batchDelete[ENTITY](entities: Iterable[ENTITY])(implicit config: Config): BatchResult[ENTITY] = macro EntityManagerMacros.batchDelete[ENTITY]
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
