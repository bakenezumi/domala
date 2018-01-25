package domala.jdbc.interpolation

import domala.jdbc.builder.UpdateBuilder

/** The object used for executing a update SQL statement and returning the results it produces.
  *
  * @param builder a builder for update SQL already built
  */
class UpdateStatement(builder: UpdateBuilder) {
  def execute(): Int = {
    builder.execute()
  }

}

object UpdateStatement {
  def apply(builder: UpdateBuilder): UpdateStatement = new UpdateStatement(builder)
}
