package domala.jdbc.entity

import java.util.function.Supplier
import java.util.{Optional, OptionalDouble, OptionalInt, OptionalLong}

import domala.internal.jdbc.scalar.{OptionBasicScalar, OptionDomainBridgeScalar}
import domala.jdbc.entity
import domala.jdbc.holder.HolderDesc
import org.seasar.doma.internal.jdbc.entity.PropertyField
import org.seasar.doma.internal.jdbc.scalar._
import org.seasar.doma.internal.jdbc.sql.ScalarInParameter
import org.seasar.doma.jdbc.InParameter
import org.seasar.doma.wrapper.Wrapper

class DefaultPropertyDesc[PARENT, ENTITY <: PARENT, BASIC, HOLDER] private (
  descParam: EntityPropertyDescParam[ENTITY, BASIC, HOLDER]
) extends org.seasar.doma.jdbc.entity.DefaultPropertyType[
  PARENT,
  ENTITY,
  BASIC,
  HOLDER](
  descParam.entityClass,
  descParam.entityPropertyClass,
  descParam.typeDesc.getBasicClass,
  descParam.typeDesc.wrapperProvider,
  null,
  HolderDesc.of(descParam.typeDesc),
  descParam.name,
  descParam.column.name,
  descParam.namingType,
  descParam.column.insertable,
  descParam.column.updatable,
  descParam.column.quote
) {

  override def createProperty: entity.DefaultProperty[_, ENTITY, BASIC] =
    DefaultPropertyDesc.createPropertySupplier[ENTITY, BASIC, HOLDER](
      field,
      entityPropertyClass,
      wrapperSupplier,
      HolderDesc.of(descParam.typeDesc))()

}

object DefaultPropertyDesc {
  def createPropertySupplier[ENTITY, BASIC, HOLDER](
      field: PropertyField[ENTITY],
      entityPropertyClass: Class[_],
      wrapperSupplier: Supplier[Wrapper[BASIC]],
      holderDesc: HolderDesc[BASIC, HOLDER]
  ): () => DefaultProperty[_, ENTITY, BASIC] =
    () =>
      if (holderDesc != null) {
        entityPropertyClass match {
          case x if x == classOf[Optional[_]] =>
            new DefaultProperty[Optional[HOLDER], ENTITY, BASIC](
              field,
              holderDesc.createOptionalScalar())
          case x if x == classOf[Option[_]] =>
            new DefaultProperty[Option[HOLDER], ENTITY, BASIC](
              field,
              new OptionDomainBridgeScalar(holderDesc.createOptionalScalar()))
          case _ =>
            new DefaultProperty[HOLDER, ENTITY, BASIC](
              field,
              holderDesc.createScalar())
        }
      } else {
        entityPropertyClass match {
          case x if x == classOf[Optional[_]] =>
            new DefaultProperty[Optional[BASIC], ENTITY, BASIC](
              field,
              new OptionalBasicScalar(wrapperSupplier))
          case x if x == classOf[OptionalInt] =>
            new DefaultProperty[OptionalInt, ENTITY, BASIC](
              field,
              new OptionalIntScalar().asInstanceOf[Scalar[BASIC, OptionalInt]])
          case x if x == classOf[OptionalLong] =>
            new DefaultProperty[OptionalLong, ENTITY, BASIC](
              field,
              new OptionalLongScalar()
                .asInstanceOf[Scalar[BASIC, OptionalLong]])
          case x if x == classOf[OptionalDouble] =>
            new DefaultProperty[OptionalDouble, ENTITY, BASIC](
              field,
              new OptionalDoubleScalar()
                .asInstanceOf[Scalar[BASIC, OptionalDouble]])
          case x if x == classOf[Option[_]] =>
            new DefaultProperty[Option[BASIC], ENTITY, BASIC](
              field,
              new OptionBasicScalar(wrapperSupplier))
          case _ =>
            new DefaultProperty[BASIC, ENTITY, BASIC](
              field,
              new BasicScalar(wrapperSupplier, field.isPrimitive))
        }
    }

  def apply[ENTITY, BASIC, HOLDER](
    descParam: EntityPropertyDescParam[ENTITY, BASIC, HOLDER]
  ): DefaultPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER] =
    new DefaultPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER](
      descParam
    )

}

class DefaultProperty[CONTAINER, ENTITY, BASIC](
    field: PropertyField[ENTITY],
    private val scalar: Scalar[BASIC, CONTAINER])
    extends Property[ENTITY, BASIC] {

  override def get(): Object = scalar.get.asInstanceOf[Object]

  override def load(entity: ENTITY): Property[ENTITY, BASIC] = {
    val value: AnyRef = field.getValue(entity)
    scalar.set(scalar.cast(value))
    this
  }

  override def save(entity: ENTITY): Property[ENTITY, BASIC] = {
    field.setValue(entity, scalar.get)
    this
  }

  override def asInParameter(): InParameter[BASIC] =
    new ScalarInParameter(scalar)

  override def getWrapper: Wrapper[BASIC] = scalar.getWrapper

  override def getDomainClass: Optional[Class[_]] = scalar.getDomainClass

}
