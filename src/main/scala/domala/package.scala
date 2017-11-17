import domala.jdbc.Config
import domala.jdbc.interpolation.{SQLInterpolator, SelectStatement}
import org.seasar.doma.jdbc.tx.TransactionIsolationLevel

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
      *   val query = select"select id, name from employee where id = $id"
      *   val singleEntity = query.getSingle[Employee]
      *   val entities = "select"select id, name from employee".getList[Employee]
      * }}}
      *
      * @param params value parameters for SQL
      * @param config the runtime configuration
      * @return a executor for select statement
      * @see [[domala.jdbc.interpolation.SelectStatement SelectExecutor]]
      */
    def select(params: Any*)(implicit config: Config): SelectStatement = SQLInterpolator.select(context, params, config)
  }

}
