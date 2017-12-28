package domala

import domala.jdbc.SqlLogType

/** Indicates a select.
  *
  * The annotated method must be a member of a [[domala.Dao Dao]] annotated trait.
  *
  * <pre>
  * &#64;Entity
  * case class Employee(
  *   ...
  * )
  *
  * &#64;Dao
  * trait EmployeeDao {
  *
  *  &#64;Select("""
  *   select name from employee
  *   where id = &#47;* id *&#47; 0
  *   """)
  *   def selectNameById(id: Int): String
  *
  *  &#64;Select("""
  *   select name from employee
  *   where age >= &#47;* age *&#47;0
  *   and salary >= &#47;* salary *&#47;0
  *   """)
  *   def selectNamesByAgeAndSalary(age: Integer, salary[BigDecimal]): List[Employee]
  *
  *  &#64;Select("""
  *   select name from employee
  *   where age >= &#47;* age *&#47;0
  *   and salary >= &#47;* salary *&#47;0
  *   """)
  *   def selectById(id: Int): Employee
  *
  *  &#64;Select("""
  *   select &#47;*%expand *&#47;* from employee
  *   where
  *   &#47;*%If example.id != null *&#47;
  *     id = &#47;* id *&#47;0
  *   &#47;*%end *&#47;
  *   &#47;*%If example.name != null *&#47;
  *     and
  *     name = &#47;* example.name *&#47;&#x27;&#x27;
  *   &#47;*%end *&#47;
  *   """)
  *   def selectByExample(example: Employee): List[Employee];
  *
  *  &#64;Select(sql = """
  *   select salary from employee
  *   where
  *   department_id = &#47;* departmentId *&#47;0
  *   """, strategy = SelectStrategyType.STREAM)
  *   def selectSalary[R](departmentId: Int, mapper: Stream[BigDecimal] => R): R
  * }
  * <pre>
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
  sql: String = "",
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
