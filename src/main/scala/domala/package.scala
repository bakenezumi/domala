import domala.jdbc.Config

package object domala {
  object Required {
    def apply[T](body: => T)(implicit config: Config): T =
      config.getTransactionManager.required(() => body)
  }

  object RequiresNew {
    def apply[T](body: => T)(implicit config: Config): T =
      config.getTransactionManager.requiresNew(() => body)
  }

  object NotSupported {
    def apply[T](body: => T)(implicit config: Config): T =
      config.getTransactionManager.notSupported(() => body)
  }

}
