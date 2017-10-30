package domala

import org.seasar.doma.jdbc.SqlLogType
import org.seasar.doma.{FetchType, MapKeyNamingType}

import org.seasar.doma.DomaNullPointerException
import org.seasar.doma.jdbc.JdbcException
import org.seasar.doma.jdbc.NoResultException
import org.seasar.doma.jdbc.NonSingleColumnException
import org.seasar.doma.jdbc.NonUniqueResultException
import org.seasar.doma.jdbc.ResultMappingException
import org.seasar.doma.jdbc.UnknownColumnException

import domala.jdbc.SelectOptions

/** Indicates a select.
  *
  * The annotated method must be a member of a [[Dao]] annotated trait.
  *
  * {{{
  * @Entity
  * case class Employee(
  *   ...
  * )
  *
  * @Dao
  * trait EmployeeDao {
  *
  *   @Select("""
  *   select name from employee
  *   where id = /* id */0
  *   """)
  *   def selectNameById(id: Int): String
  *
  *   @Select("""
  *   select name from employee
  *   where age >= /* age */0
  *   and salary >= /* salary */0
  *   """)
  *   @Select
  *   def selectNamesByAgeAndSalary(age: Integer, salary[BigDecimal]): List[Employee]
  *
  *   @Select("""
  *   select name from employee
  *   where age >= /* age */0
  *   and salary >= /* salary */0
  *   """)
  *   def selectById(id: Int): Employee
  *
  *   @Select("""
  *   select /*%expand "*/* from employee
  *   where
  *   /*%If example.id != null */
  *     id = /* id */0
  *   /*%end */
  *   /*%If example.name != null */
  *     and
  *     name = /* example.name */''
  *   /*%end */
  *   """)
  *   def selectByExample(example: Employee): List[Employee];
  *
  *   @Select(sql = """
  *   select salary from employee
  *   where
  *   department_id = /* departmentId */0
  *   """, strategy = SelectStrategyType.STREAM)
  *   def selectSalary[R](departmentId: Int, mapper: Stream[BigDecimal] => R): R
  * }
  * }}}
  *
  * The method may throw following exceptions:
  * @throws DomaNullPointerException if any of the method parameters are
  * `null`
  * @throws UnknownColumnException if a property whose mapped column is
  * included in a result set is not found
  * @throws NonUniqueResultException if an unique row is expected but two or
  * more rows are found in a result set
  * @throws NonSingleColumnException if a single column is expected but two
  * or more columns are found in a result set
  * @throws NoResultException if [[ensureResult]] is `true` and no
  * row is found in a result set
  * @throws ResultMappingException if [[ensureResultMapping]] is
  * `true` and all entity properties are not mapped to columns of a result
  * set
  * @throws JdbcException if a JDBC related error occurs
  *
  * @see [[SelectOptions]]
  */
class Select(
  sql: String,
  queryTimeout: Int = -1,
  fetchSize: Int = -1,
  maxRows: Int = -1,
  strategy: SelectType = SelectType.RETURN,
  fetch: FetchType = FetchType.LAZY,
  ensureResult: Boolean = false,
  ensureResultMapping: Boolean = false,
  mapKeyNaming: MapKeyNamingType = MapKeyNamingType.NONE,
  sqlLog: SqlLogType = SqlLogType.FORMATTED
) extends scala.annotation.StaticAnnotation

sealed trait SelectType

object SelectType {
  case object RETURN extends SelectType
  case object STREAM extends SelectType
}
