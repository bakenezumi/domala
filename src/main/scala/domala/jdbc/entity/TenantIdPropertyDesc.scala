package domala.jdbc.entity

import java.util.function.Supplier

import domala.Column
import domala.jdbc.entity
import domala.jdbc.holder.HolderDesc
import org.seasar.doma.wrapper.Wrapper

class TenantIdPropertyDesc[PARENT, ENTITY <: PARENT, BASIC, HOLDER] private (
  entityClass: Class[ENTITY],
  entityPropertyClass: Class[_],
  basicClass: Class[BASIC],
  wrapperSupplier: Supplier[Wrapper[BASIC]],
  parentEntityPropertyDesc: EntityPropertyDesc[PARENT, BASIC],
  holderDesc: HolderDesc[BASIC, HOLDER],
  name: String,
  column: Column,
  namingType: NamingType
) extends org.seasar.doma.jdbc.entity.TenantIdPropertyType[PARENT, ENTITY, BASIC, HOLDER](
  entityClass,
  entityPropertyClass,
  basicClass,
  wrapperSupplier,
  parentEntityPropertyDesc,
  holderDesc,
  name,
  column.name,
  namingType,
  column.quote
) {

  override def createProperty: entity.DefaultProperty[_, ENTITY, BASIC] = DefaultPropertyDesc.createPropertySupplier[ENTITY, BASIC, HOLDER](field, entityPropertyClass, wrapperSupplier, holderDesc)()

}

object TenantIdPropertyDesc {
  def ofBasic[ENTITY, BASIC, HOLDER](
    entityClass: Class[ENTITY],
    entityPropertyClass: Class[_],
    basicClass: Class[BASIC],
    wrapperSupplier: Supplier[Wrapper[BASIC]],
    name: String,
    column: Column,
    namingType: NamingType
  ): TenantIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER] = {
    new TenantIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER](
      entityClass,
      entityPropertyClass,
      basicClass,
      wrapperSupplier,
      null,
      null,
      name,
      column,
      namingType
    )
  }

  def ofHolder[ENTITY, BASIC, HOLDER](
    entityClass: Class[ENTITY],
    entityPropertyClass: Class[_],
    holderDesc: HolderDesc[BASIC, HOLDER],
    name: String,
    column: Column,
    namingType: NamingType
  ): TenantIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER] = {
    new TenantIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER](
      entityClass,
      entityPropertyClass,
      holderDesc.getBasicClass,
      holderDesc.wrapperProvider,
      null,
      holderDesc,
      name,
      column,
      namingType
    )
  }

}
