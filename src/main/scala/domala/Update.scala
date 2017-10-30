package domala

import org.seasar.doma.jdbc.SqlLogType

import org.seasar.doma.DomaNullPointerException
import org.seasar.doma.jdbc.JdbcException
import org.seasar.doma.jdbc.OptimisticLockException
import org.seasar.doma.jdbc.UniqueConstraintException

/** Indicates an update.
  *
  * The annotated method must be a member of a [[Dao]] annotated trait.
  *
  * {{{
  * @Entity
  * case class Employee {
  * ...
  * }
  *
  * @Dao
  * trait EmployeeDao {
  *
  *   @Update
  *   def update(Employee employee): Result[Employee]
  * }
  * }}}
  *
  * The method may throw following exceptions:
  * @throws DomaNullPointerException if any of the method parameters are
  * `null`
  * @throws OptimisticLockException if optimistic locking is enabled and an
  * update count is 0 for each entity
  * @throws UniqueConstraintException if an unique constraint is violated
  * @throws JdbcException if a JDBC related error occurs
  */
class Update(
  sql: String = "",
  queryTimeOut: Int = -1,
  excludeNull: Boolean = false,
  ignoreVersion: Boolean = false,
  include: collection.Seq[String] = Nil,
  exclude: collection.Seq[String] = Nil,
  suppressOptimisticLockException: Boolean = false,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation
