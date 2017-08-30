package domala

object Required {
  def apply[T](body: => T)(implicit config: Config): T =
    config.getTransactionManager.required(() => body)
}
