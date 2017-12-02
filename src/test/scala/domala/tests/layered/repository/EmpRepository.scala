package domala.tests.layered.repository
import domala.tests.layered.domain.{Emp, ID}

import scala.reflect.ClassTag

trait EmpRepository {
  def create(): Unit
  def drop(): Unit
  def save(entities: Seq[Emp]): Array[Int]
  def findByIds[R: ClassTag](id: Seq[ID[Emp]])(mapper: Iterator[Emp] => R): R
  def findAll[R: ClassTag](mapper: Iterator[Emp] => R): R
  def entry(entity: Emp): Int
}
