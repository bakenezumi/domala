package domala.jdbc.entity

import java.util.function.Supplier

import domala.jdbc.entity
import domala.jdbc.holder.{AbstractAnyValHolderDesc, AbstractHolderDesc, HolderDesc}
import org.seasar.doma.jdbc.id.IdGenerator
import org.seasar.doma.wrapper.Wrapper

class GeneratedIdPropertyDesc[PARENT, ENTITY <: PARENT, BASIC <: Number, HOLDER] private (
  entityClass: Class[ENTITY],
  entityPropertyClass: Class[_],
  basicClass: Class[BASIC],
  wrapperSupplier: Supplier[Wrapper[BASIC]],
  parentEntityPropertyDesc: EntityPropertyDesc[PARENT, BASIC],
  holderDesc: HolderDesc[BASIC, HOLDER],
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
  parentEntityPropertyDesc,
  holderDesc,
  name,
  columnName,
  namingType,
  quoteRequired,
  idGenerator) {

 override def createProperty: entity.DefaultProperty[_, ENTITY, BASIC] = DefaultPropertyDesc.createPropertySupplier[ENTITY, BASIC, HOLDER](field, entityPropertyClass, wrapperSupplier, holderDesc)()

}

object GeneratedIdPropertyDesc {
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
  ) : GeneratedIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER] =
    new GeneratedIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER](
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
    holderDesc: AbstractHolderDesc[BASIC, HOLDER],
    name: String,
    columnName: String,
    namingType: NamingType,
    quoteRequired: Boolean,
    idGenerator: IdGenerator
  ): GeneratedIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER] = {
    new GeneratedIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER](
      entityClass,
      entityPropertyClass,
      holderDesc.getBasicClass.asInstanceOf[Class[BASIC]],
      holderDesc.wrapper,
      null,
      holderDesc,
      name,
      columnName,
      namingType,
      quoteRequired,
      idGenerator
    )
  }

  def ofAnyVal[ENTITY, BASIC <: Number, HOLDER](
    entityClass: Class[ENTITY],
    entityPropertyClass: Class[_],
    holderDesc: AbstractAnyValHolderDesc[BASIC, HOLDER],
    name: String,
    columnName: String,
    namingType: NamingType,
    quoteRequired: Boolean,
    idGenerator: IdGenerator
  ): GeneratedIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER] = {
    new GeneratedIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER](
      entityClass,
      entityPropertyClass,
      holderDesc.getBasicClass,
      holderDesc.wrapperSupplier,
      null,
      holderDesc,
      name,
      columnName,
      namingType,
      quoteRequired,
      idGenerator
    )
  }
}
