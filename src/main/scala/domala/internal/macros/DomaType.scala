package domala.internal.macros

import scala.meta.{Term, Type}

sealed trait DomaType

object DomaType {
  case object Map extends DomaType
  case class Seq(elementType: DomaType) extends DomaType
  case class Option(elementType: DomaType) extends DomaType
  case class Basic(originalType: Type, convertedType: Type, wrapperSupplier: Term.Function) extends DomaType
  case class Domain(value: Basic) extends DomaType
  case object Embeddable extends DomaType
  case class Entity(tpe: Type) extends DomaType
  case class EntityOrDomain(tpe: Type) extends DomaType
  case object Unknown extends DomaType
}