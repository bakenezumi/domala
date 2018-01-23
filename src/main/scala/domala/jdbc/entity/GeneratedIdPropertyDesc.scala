package domala.jdbc.entity

import java.util.function.Supplier

import domala.Column
import domala.jdbc.entity
import domala.jdbc.holder.HolderDesc
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
  column: Column,
  namingType: NamingType,
  idGenerator: IdGenerator
) extends org.seasar.doma.jdbc.entity.GeneratedIdPropertyType[PARENT, ENTITY, BASIC, HOLDER] (
  entityClass,
  entityPropertyClass,
  basicClass,
  wrapperSupplier,
  parentEntityPropertyDesc,
  holderDesc,
  name,
  column.name,
  namingType,
  column.quote,
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
    column: Column,
    namingType: NamingType,
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
    column,
    namingType,
    idGenerator)

  def ofHolder[ENTITY, BASIC <: Number, HOLDER](
    entityClass: Class[ENTITY],
    entityPropertyClass: Class[_],
    holderDesc: HolderDesc[BASIC, HOLDER],
    name: String,
    column: Column,
    namingType: NamingType,
    idGenerator: IdGenerator
  ): GeneratedIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER] = {
    new GeneratedIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER](
      entityClass,
      entityPropertyClass,
      holderDesc.getBasicClass,
      holderDesc.wrapperProvider,
      null,
      holderDesc,
      name,
      column,
      namingType,
      idGenerator
    )
  }

}
