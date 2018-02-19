package domala.async.models

import domala.async.jdbc.{AsyncAction, AsyncConfig, AsyncEntityManager, AsyncResult}
import domala.jdbc.models._

trait AsyncPersonDao extends PersonDao {

  def findAll[R](mapper: Iterator[Person] => R): AsyncAction[R]

  def findById[R](id: ID[Person], mapper: Iterator[Person] => R): AsyncAction[R]

  def add(entity: Person): AsyncResult[Person]
}

object AsyncPersonDao {
  def impl(implicit config: AsyncConfig): AsyncPersonDao = new AsyncPersonDaoImpl
}

import domala._

class AsyncPersonDaoImpl(implicit config: AsyncConfig) extends PersonDaoImpl with AsyncPersonDao {

  def findAll[R](mapper: Iterator[Person] => R): AsyncAction[R] = select"select /*%expand*/* from person".async(mapper)

  def findById[R](id: ID[Person], mapper: Iterator[Person] => R): AsyncAction[R] = select"select /*%expand*/* from person where id = $id".async(mapper)

  def add(entity: Person): AsyncResult[Person] = AsyncEntityManager.insert(entity)

}
