package domala

/** Indicate a version property that is used for optimistic locking.
  *
  * The annotated field must be a member of an [[domala.Entity Entity]] annotated class.
  *
  * {{{
  * @Entity
  * case class Employee(
  *   ...
  *
  *   @Version
  *   @Column(name = "VERSION_NO")
  *   versionNo: Int
  * )
  * }}}
  */
class Version extends scala.annotation.StaticAnnotation
