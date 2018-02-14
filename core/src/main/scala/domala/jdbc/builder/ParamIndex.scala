package domala.jdbc.builder

class ParamIndex {
  private var value = 1

  private[builder] def increment(): Unit = {
    value += 1
  }

  private[builder] def getValue = value

}
