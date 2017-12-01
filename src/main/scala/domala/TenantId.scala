package domala

/** Indicates the tenant's identifier.
  *
  * The annotated field must be a member of an [[domala.Entity Entity]] annotated class.
  *
  * In queries of the type where SQL is generated, columns mapped to annotated fields
  * are included in the WHERE clause as search condition.
  *
  * {{{
  * @Entity
  * case class Employee(
  *   ...
  *   @TenantId
  *   tenantId: String,
  *
  *   ...
  * )
  * }}}
  */
class TenantId extends scala.annotation.StaticAnnotation
