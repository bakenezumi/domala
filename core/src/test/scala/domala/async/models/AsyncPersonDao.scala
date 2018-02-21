package domala.async.models

import domala.async.AsyncAction
import domala.async.jdbc.{AsyncConfig, AsyncEntityManager, AsyncResult}
import domala.jdbc.models._

trait AsyncPersonDao extends PersonDao {

  def findAll[R](mapper: Iterator[Person] => R): AsyncAction[R]

  def findById[R](id: ID[Person], mapper: Iterator[Person] => R): AsyncAction[R]

  def add(entity: Person): AsyncResult[Person]
}

object AsyncPersonDao {
  def impl(implicit config: AsyncConfig): AsyncPersonDao = new AsyncPersonDaoReadImpl with AsyncPersonDaoWriteImpl {
    override implicit val _config: AsyncConfig = config
  }
  def readOnlyImpl(implicit config: AsyncConfig): AsyncPersonDao = new AsyncPersonDaoReadImpl {
    override implicit val _config: AsyncConfig = config
    override def add(entity: Person): AsyncResult[Person] = throw new NotImplementedError
  }
}

import domala._

trait AsyncPersonDaoReadImpl extends PersonDaoImpl with AsyncPersonDao {
  implicit val _config: AsyncConfig

  def findAll[R](mapper: Iterator[Person] => R): AsyncAction[R] = select"select /*%expand*/* from person".async(mapper)

  def findById[R](id: ID[Person], mapper: Iterator[Person] => R): AsyncAction[R] = select"select /*%expand*/* from person where id = $id".async(mapper)

}

trait AsyncPersonDaoWriteImpl extends PersonDaoImpl with AsyncPersonDao {
  implicit val _config: AsyncConfig

  def add(entity: Person): AsyncResult[Person] = AsyncEntityManager.insert(entity)

}
