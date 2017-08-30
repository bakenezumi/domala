package domala

class Column(
    name: String = "",
    insertable: Boolean = true,
    updatable: Boolean = true
) extends scala.annotation.StaticAnnotation
