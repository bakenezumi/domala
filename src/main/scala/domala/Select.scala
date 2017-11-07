package domala

import org.seasar.doma.jdbc.SqlLogType
import org.seasar.doma.{FetchType, MapKeyNamingType}

/** Indicates a select.
  *
  * The annotated method must be a member of a [[domala.Dao Dao]] annotated trait.
  *
  * {{{
  *@literal @Entity
  * case class Employee(
  *   ...
  * )
  *
  *@literal @Dao
  * trait EmployeeDao {
  *
  *  @literal @Select("""
  *   select name from employee
  *   where id = |* id *| 0 -- Scaladoc can not nest comments, actually replace `|` with `/`.
  *   """)
  *   def selectNameById(id: Int): String
  *
  *  @literal @Select("""
  *   select name from employee
  *   where age >= |* age *|0  -- Scaladoc can not nest comments, actually replace `|` with `/`.
  *   and salary >= |* salary *|0
  *   """)
  *   def selectNamesByAgeAndSalary(age: Integer, salary[BigDecimal]): List[Employee]
  *
  *  @literal @Select("""
  *   select name from employee
  *   where age >= |* age *|0  -- Scaladoc can not nest comments, actually replace `|` with `/`.
  *   and salary >= |* salary *|0
  *   """)
  *   def selectById(id: Int): Employee
  *
  *  @literal @Select("""
  *   select |*%expand *|* from employee -- Scaladoc can not nest comments, actually replace `|` with `/`.
  *   where
  *   |*%If example.id != null *|
  *     id = |* id *|0
  *   |*%end *|
  *   |*%If example.name != null *|
  *     and
  *     name = |* example.name *|''
  *   |*%end *|
  *   """)
  *   def selectByExample(example: Employee): List[Employee];
  *
  *  @literal @Select(sql = """
  *   select salary from employee
  *   where
  *   department_id = |* departmentId *|0  -- Scaladoc can not nest comments, actually replace `|` with `/`.
  *   """, strategy = SelectStrategyType.STREAM)
  *   def selectSalary[R](departmentId: Int, mapper: Stream[BigDecimal] => R): R
  * }
  * }}}
  *
  * @param sql a execution SQL.
  * @param queryTimeOut The query timeout in seconds.
  *  If not specified, [[domala.jdbc.Config Config#getQueryTimeout]] is used.
  * @param fetchSize The fetch size. If not specified,
  *  [[domala.jdbc.Config Config#getFetchSize]] is used.
  * @param maxRows The maximum number of rows. If not specified,
  *  [[domala.jdbc.Config Config#getMaxRows]] is used.
  * @param strategy The strategy for handling an object that is mapped to
  *  a result set.
  * @param fetch The fetch type.
  * @param ensureResult Whether to ensure that one or more rows are found in
  *  a result set.
  *  if `true` and no row is found,
  *  [[org.seasar.doma.jdbc.NoResultException jdbcNoResultException]] is thrown
  *  @param ensureResultMapping Whether to ensure that all entity
  *   properties are mapped to columns of a result set.
  *   This value is used only if the result set is fetched as an entity or a
  *   entity list.
  *   If `true` and there are some unmapped properties,
  *   [[org.seasar.doma.jdbc.ResultMappingException ResultMappingException]] is thrown from the method.
  * @param mapKeyNaming The naming convention for keys of `Map[String, Any]`.
  *  This value is used only if a result set is fetched as `Map[String, Any]` or `Seq[ Map[String, Any] ]`.
  * @param sqlLog The output format of SQL logs.
  * @throws org.seasar.doma.DomaNullPointerException if any of the method parameters are
  * `null`
  * @throws org.seasar.doma.DomaNullPointerException if any of the method parameters are
  * `null`
  * @throws org.seasar.doma.jdbc.UnknownColumnException if a property whose mapped column is
  * included in a result set is not found
  * @throws org.seasar.doma.jdbc.NonUniqueResultException if an unique row is expected but two or
  * more rows are found in a result set
  * @throws org.seasar.doma.jdbc.NonSingleColumnException if a single column is expected but two
  * or more columns are found in a result set
  * @throws org.seasar.doma.jdbc.NoResultException if `ensureResult` is `true` and no
  * row is found in a result set
  * @throws org.seasar.doma.jdbc.ResultMappingException if `ensureResultMapping` is
  * `true` and all entity properties are not mapped to columns of a result
  * set
  * @throws org.seasar.doma.jdbc.JdbcException if a JDBC related error occurs
  *
  * @see [[domala.jdbc.SelectOptions]]
  */
class Select(
  sql: String,
  queryTimeOut: Int = -1,
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
  case object ITERATOR extends SelectType
}
