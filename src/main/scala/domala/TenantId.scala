package domala

/** Indicates that the annotated field is not mapped to a column.
  *
  * The annotated field must be a member of an [[domala.Entity Entity]] annotated class.
  *
  * {{{
  *@literal @Entity
  * case class Employee(
  *   ...
  *  @literal @Transient
  *   tempNumber: Int,
  *
  *   ...
  * )
  * }}}
  */
class TenantId extends scala.annotation.StaticAnnotation
