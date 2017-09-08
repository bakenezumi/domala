package domala

import domala.jdbc.Config

object Required {
  def apply[T](body: => T)(implicit config: Config): T =
    config.getTransactionManager.required(() => body)
}
