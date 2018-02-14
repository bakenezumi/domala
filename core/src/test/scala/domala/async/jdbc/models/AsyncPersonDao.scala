package domala.async.jdbc.models

import domala.async.jdbc.AsyncConfig
import domala.jdbc.models._

import scala.concurrent.Future

trait AsyncPersonDao extends PersonDao {

  def findAll[R](mapper: Iterator[Person] => R): Future[R]

  def findById[R](id: ID[Person], mapper: Iterator[Person] => R): Future[R]
}

object AsyncPersonDao {
  def impl(implicit config: AsyncConfig): AsyncPersonDao = new AsyncPersonDaoImpl
}

import domala._

class AsyncPersonDaoImpl(implicit config: AsyncConfig) extends PersonDaoImpl with AsyncPersonDao {

  def findAll[R](mapper: Iterator[Person] => R): Future[R] = select"select /*%expand*/* from person".async(mapper)

  def findById[R](id: ID[Person], mapper: Iterator[Person] => R): Future[R] = select"select /*%expand*/* from person where id = $id".async(mapper)

}
