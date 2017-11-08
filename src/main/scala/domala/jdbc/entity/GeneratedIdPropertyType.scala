package domala.jdbc.entity

import java.util.function.Supplier

import domala.jdbc.entity
import domala.jdbc.holder.AbstractHolderDesc
import org.seasar.doma.jdbc.domain.DomainType
import org.seasar.doma.jdbc.entity.EntityPropertyType
import org.seasar.doma.jdbc.entity.NamingType
import org.seasar.doma.jdbc.id.IdGenerator
import org.seasar.doma.wrapper.Wrapper

class GeneratedIdPropertyType[PARENT, ENTITY <: PARENT, BASIC <: Number, HOLDER] private (
  entityClass: Class[ENTITY],
  entityPropertyClass: Class[_],
  basicClass: Class[BASIC],
  wrapperSupplier: Supplier[Wrapper[BASIC]],
  parentEntityPropertyType: EntityPropertyType[PARENT, BASIC],
  holderType: DomainType[BASIC, HOLDER],
  name: String,
  columnName: String,
  namingType: NamingType,
  quoteRequired: Boolean,
  idGenerator: IdGenerator
) extends org.seasar.doma.jdbc.entity.GeneratedIdPropertyType[PARENT, ENTITY, BASIC, HOLDER] (
  entityClass,
  entityPropertyClass,
  basicClass,
  wrapperSupplier,
  parentEntityPropertyType,
  holderType,
  name,
  columnName,
  namingType,
  quoteRequired,
  idGenerator) {

 override def createProperty: entity.DefaultProperty[_, ENTITY, BASIC] = DefaultPropertyType.createPropertySupplier[ENTITY, BASIC, HOLDER](field, entityPropertyClass, wrapperSupplier, holderType)()

}

object GeneratedIdPropertyType {
  def ofBasic[ENTITY, BASIC <: Number, HOLDER] (
    entityClass: Class[ENTITY],
    entityPropertyClass: Class[_],
    basicClass: Class[BASIC],
    wrapperSupplier: Supplier[Wrapper[BASIC]],
    name: String,
    columnName: String,
    namingType: NamingType,
    quoteRequired: Boolean,
    idGenerator: IdGenerator
  ) : GeneratedIdPropertyType[ENTITY, ENTITY, BASIC, HOLDER] =
    new GeneratedIdPropertyType[ENTITY, ENTITY, BASIC, HOLDER](
    entityClass,
    entityPropertyClass,
    basicClass,
    wrapperSupplier,
    null,
    null,
    name,
    columnName,
    namingType,
    quoteRequired,
    idGenerator)

  def ofHolder[ENTITY, BASIC <: Number, HOLDER](
    entityClass: Class[ENTITY],
    entityPropertyClass: Class[_],
    holderType: AbstractHolderDesc[BASIC, HOLDER],
    name: String,
    columnName: String,
    namingType: NamingType,
    quoteRequired: Boolean,
    idGenerator: IdGenerator
  ): GeneratedIdPropertyType[ENTITY, ENTITY, BASIC, HOLDER] = {
    new GeneratedIdPropertyType[ENTITY, ENTITY, BASIC, HOLDER](
      entityClass,
      entityPropertyClass,
      holderType.getBasicClass.asInstanceOf[Class[BASIC]],
      holderType.wrapper,
      null,
      holderType,
      name,
      columnName,
      namingType,
      quoteRequired,
      idGenerator
    )
  }
}