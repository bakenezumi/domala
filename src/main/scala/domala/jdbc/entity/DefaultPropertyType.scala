package domala.jdbc.entity

import java.util.function.Supplier
import java.util.Optional
import java.util.OptionalDouble
import java.util.OptionalInt
import java.util.OptionalLong

import org.seasar.doma.internal.jdbc.entity.PropertyField
import org.seasar.doma.internal.jdbc.scalar.BasicScalar
import org.seasar.doma.internal.jdbc.scalar.OptionalBasicScalar
import org.seasar.doma.internal.jdbc.scalar.OptionalDoubleScalar
import org.seasar.doma.internal.jdbc.scalar.OptionalIntScalar
import org.seasar.doma.internal.jdbc.scalar.OptionalLongScalar
import org.seasar.doma.internal.jdbc.scalar.Scalar
import org.seasar.doma.internal.jdbc.sql.ScalarInParameter
import org.seasar.doma.jdbc.InParameter
import org.seasar.doma.jdbc.domain.DomainType
import org.seasar.doma.jdbc.entity.EntityPropertyType
import org.seasar.doma.jdbc.entity.NamingType
import org.seasar.doma.jdbc.entity.Property
import org.seasar.doma.wrapper.Wrapper
import domala.internal.jdbc.scalar.{OptionBasicScalar, OptionDomainBridgeScalar}
import domala.jdbc.entity
import domala.jdbc.holder.AbstractHolderDesc

class DefaultPropertyType[PARENT, ENTITY <: PARENT, BASIC, HOLDER](
  entityClass: Class[ENTITY],
  entityPropertyClass: Class[_],
  basicClassClass: Class[BASIC],
  wrapperSupplier: Supplier[Wrapper[BASIC]],
  parentEntityPropertyType: EntityPropertyType[PARENT, BASIC],
  holderType: DomainType[BASIC, HOLDER],
  name: String,
  columnName: String,
  namingType: NamingType,
  insertable: Boolean,
  updatable: Boolean,
  quoteRequired: Boolean
) extends org.seasar.doma.jdbc.entity.DefaultPropertyType[
  PARENT,
  ENTITY,
  BASIC,
  HOLDER](
  entityClass,
  entityPropertyClass,
  basicClassClass,
  wrapperSupplier,
  parentEntityPropertyType,
  holderType,
  name,
  columnName,
  namingType,
  insertable,
  updatable,
  quoteRequired
) {

  override def createProperty: entity.DefaultProperty[_, ENTITY, BASIC] =
    DefaultPropertyType.createPropertySupplier[ENTITY, BASIC, HOLDER](
      field,
      entityPropertyClass,
      wrapperSupplier,
      holderType)()

}

object DefaultPropertyType {
  def createPropertySupplier[ENTITY, BASIC, HOLDER](
      field: PropertyField[ENTITY],
      entityPropertyClass: Class[_],
      wrapperSupplier: Supplier[Wrapper[BASIC]],
      holderType: DomainType[BASIC, HOLDER]
  ): () => DefaultProperty[_, ENTITY, BASIC] =
    () =>
      if (holderType != null) {
        entityPropertyClass match {
          case x if x == classOf[Optional[_]] =>
            new DefaultProperty[Optional[HOLDER], ENTITY, BASIC](
              field,
              holderType.createOptionalScalar())
          case x if x == classOf[Option[_]] =>
            new DefaultProperty[Option[HOLDER], ENTITY, BASIC](
              field,
              new OptionDomainBridgeScalar(holderType.createOptionalScalar()))
          case _ =>
            new DefaultProperty[HOLDER, ENTITY, BASIC](
              field,
              holderType.createScalar())
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

  def ofHolder[ENTITY, BASIC, HOLDER](
    entityClass: Class[ENTITY],
    entityPropertyClass: Class[_],
    holderType: AbstractHolderDesc[BASIC, HOLDER],
    name: String,
    columnName: String,
    namingType: NamingType,
    insertable: Boolean,
    updatable: Boolean,
    quoteRequired: Boolean
  ): DefaultPropertyType[ENTITY, ENTITY, BASIC, HOLDER] = {
    new DefaultPropertyType[ENTITY, ENTITY, BASIC, HOLDER](
      entityClass,
      entityPropertyClass,
      holderType.getBasicClass.asInstanceOf[Class[BASIC]],
      holderType.wrapper,
      null,
      holderType,
      name,
      columnName,
      namingType,
      insertable,
      updatable,
      quoteRequired
    )
  }

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
