package domala

class Id extends scala.annotation.StaticAnnotation

class GeneratedValue(strategy: GenerationType) extends scala.annotation.StaticAnnotation

sealed trait GenerationType

object GenerationType {
    object IDENTITY extends GenerationType
    object SEQUENCE extends GenerationType
}
