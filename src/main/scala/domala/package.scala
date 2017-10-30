import domala.jdbc.Config
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
      config.getTransactionManager.required(() => supplier)
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
      config.getTransactionManager.requiresNew(() => supplier)
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
      config.getTransactionManager.notSupported(() => supplier)
  }

}
