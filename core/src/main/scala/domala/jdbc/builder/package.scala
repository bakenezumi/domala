package domala.jdbc

import org.seasar.doma.jdbc.builder.{DomalaSelectBuilder, DomalaUpdateBuilder}

package object builder {
  type SelectBuilder = DomalaSelectBuilder
  val SelectBuilder: DomalaSelectBuilder.type = DomalaSelectBuilder
  type UpdateBuilder = DomalaUpdateBuilder
  val UpdateBuilder: DomalaUpdateBuilder.type = DomalaUpdateBuilder

}
