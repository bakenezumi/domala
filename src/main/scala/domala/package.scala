import domala.jdbc.Config
import domala.jdbc.interpolation.{SQLInterpolator, ScriptStatement, SelectStatement, UpdateStatement}
import domala.jdbc.tx.TransactionIsolationLevel

package object domala {

  object Required {
    /** Executes the transaction whose attribute is REQUIRED and returns the
      * result.
      *
      * @tparam RESULT the result type
      * @param supplier the code that is executed in the transaction
      * @param config the runtime configuration
      * @return the result
      */
    def apply[RESULT](supplier: => RESULT)(implicit config: Config): RESULT =
      config.getTransactionManager.required(() => supplier)

    /** Executes the transaction whose attribute is REQUIRED with the specified
      * transaction isolation level and return the result.
      *
      * @tparam RESULT the result type
      * @param isolationLevel the transaction isolation level
      * @param supplier the code that is executed in the transaction
      * @param config the runtime configuration
      * @return the result
      */
    def apply[RESULT](isolationLevel: TransactionIsolationLevel)(supplier: => RESULT)(implicit config: Config): RESULT =
      config.getTransactionManager.required(isolationLevel, () => supplier)
  }

  object RequiresNew {

    /** Executes the transaction whose attribute is REQUIRES_NEW and returns the
      * result.
      *
      * @tparam RESULT the result type
      * @param supplier the code that is executed in the transaction
      * @param config the runtime configuration
      * @return the result
      */
    def apply[RESULT](supplier: => RESULT)(implicit config: Config): RESULT =
      config.getTransactionManager.requiresNew(() => supplier)

    /** Executes the transaction whose attribute is REQUIRES_NEW with the specified
      * transaction isolation level and return the result.
      *
      * @tparam RESULT the result type
      * @param isolationLevel the transaction isolation level
      * @param supplier the code that is executed in the transaction
      * @param config the runtime configuration
      * @return the result
      */
    def apply[RESULT](isolationLevel: TransactionIsolationLevel)(supplier: => RESULT)(implicit config: Config): RESULT =
      config.getTransactionManager.requiresNew(isolationLevel, () => supplier)
  }

  object NotSupported {
    /** Executes the transaction whose attribute is NOT_SUPPORTED and returns the
      * result.
      *
      * @tparam RESULT the result type
      * @param supplier the code that is executed in the transaction
      * @param config the runtime configuration
      * @return the result
      */
    def apply[RESULT](supplier: => RESULT)(implicit config: Config): RESULT =
      config.getTransactionManager.notSupported(() => supplier)

    /** Executes the transaction whose attribute is NOT_SUPPORTED with the
      * specified transaction isolation level and return the result.
      *
      * @tparam RESULT the result type
      * @param isolationLevel the transaction isolation level
      * @param supplier the code that is executed in the transaction
      * @param config the runtime configuration
      * @return the result
      */
    def apply[RESULT](isolationLevel: TransactionIsolationLevel)(supplier: => RESULT)(implicit config: Config): RESULT =
      config.getTransactionManager.notSupported(isolationLevel, () => supplier)
  }

  implicit class Interpolator(val context: StringContext) extends AnyVal {
    /** a String interpolator for SELECT SQL literals.
      *
      * {{{
      *   val id = 1
      *   val query = select"""
      *     select id, name
      *     from employee
      *     where id = $id
      *   ""
      *   val employee = query.getSingle[Employee]
      *
      *   val employees = "select"select id, name from employee".getList[Employee]
      * }}}
      *
      * @param params value parameters for SQL
      * @param config the runtime configuration
      * @return a executor for select statement
      * @see [[domala.jdbc.interpolation.SelectStatement SelectStatement]]
      */
    def select(params: Any*)(implicit config: Config): SelectStatement = SQLInterpolator.select(context, params, config)

    /** a String interpolator for INSERT, UPDATE, DELETE SQL literals.
      *
      * {{{
      *   Seq("Scott", "Allen").map { name =>
      *     update"""
      *       insert into emp(name) values($name)
      *     """.execute()
      *   }
      *
      *   val (id, name) = (1, "Smith")
      *   update"""
      *     update emp set name = $name where id = $id
      *   """.execute()
      * }}}
      *
      * @param params value parameters for SQL
      * @param config the runtime configuration
      * @return a executor for update statement
      * @see [[domala.jdbc.interpolation.UpdateStatement UpdateStatement]]
      */
    def update(params: Any*)(implicit config: Config): UpdateStatement = SQLInterpolator.update(context, params, config)

    /** a String interpolator for SQL scripts.
      *
      * {{{
      *   script"""
      *     create table emp(
      *       id int serial primary key,
      *       name varchar(20)
      *     );
      *   """.execute()
      * }}}
      *
      * @param params must be Nil
      * @param config the runtime configuration
      * @return a executor for SQL scripts
      * @see [[domala.jdbc.interpolation.ScriptStatement ScriptStatement]]
      */
    def script(params: Any*)(implicit config: Config): ScriptStatement = SQLInterpolator.script(context, params, config)

  }

  // Alias of Doma type
  type FetchType = org.seasar.doma.FetchType
  object FetchType {
    val EAGER = org.seasar.doma.FetchType.EAGER
    val LAZY = org.seasar.doma.FetchType.LAZY
  }
  type MapKeyNamingType = org.seasar.doma.MapKeyNamingType
  object MapKeyNamingType {
    val NONE = org.seasar.doma.MapKeyNamingType.NONE
    val CAMEL_CASE = org.seasar.doma.MapKeyNamingType.CAMEL_CASE
    val UPPER_CASE = org.seasar.doma.MapKeyNamingType.UPPER_CASE
    val LOWER_CASE = org.seasar.doma.MapKeyNamingType.LOWER_CASE
  }

}
