package domala

class Table(
  catalog: String = "",
  schema: String = "",
  name: String = "",
  quote: Boolean = false
) extends scala.annotation.StaticAnnotation
