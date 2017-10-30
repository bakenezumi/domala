package domala.jdbc

/**
  * The options for an SQL SELECT statement.
  *
  * {{{
  * SelectOptions options = SelectOptions.get.offset(10).limit(50).forUpdate
  * }}}
  */
class SelectOptions extends org.seasar.doma.jdbc.SelectOptions

object SelectOptions {
  def get: SelectOptions = {
    new SelectOptions()
  }
}
