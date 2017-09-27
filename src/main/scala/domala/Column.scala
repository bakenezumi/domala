package domala

class Column(
  name: String = "",
  insertable: Boolean = true,
  updatable: Boolean = true,
  quote: Boolean = false
) extends scala.annotation.StaticAnnotation
