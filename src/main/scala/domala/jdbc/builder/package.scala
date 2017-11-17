package domala.jdbc

import org.seasar.doma.jdbc.builder.DomalaSelectBuilder

package object builder {
  type SelectBuilder = DomalaSelectBuilder
  val SelectBuilder: DomalaSelectBuilder.type = DomalaSelectBuilder
}
