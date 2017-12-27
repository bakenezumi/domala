package domala.jdbc.entity

import java.util.function.Supplier

import domala.jdbc.entity
import domala.jdbc.holder.{AbstractAnyValHolderDesc, AbstractHolderDesc, HolderDesc}
import org.seasar.doma.wrapper.Wrapper

class VersionPropertyDesc[PARENT, ENTITY <: PARENT, BASIC <: Number, HOLDER] private (
  entityClass: Class[ENTITY],
  entityPropertyClass: Class[_],
  basicClass: Class[BASIC],
  wrapperSupplier: Supplier[Wrapper[BASIC]],
  parentEntityPropertyDesc: EntityPropertyDesc[PARENT, BASIC],
  holderDesc: HolderDesc[BASIC, HOLDER],
  name: String,
  columnName: String,
  namingType: NamingType,
  quoteRequired: Boolean
) extends org.seasar.doma.jdbc.entity.VersionPropertyType[PARENT, ENTITY, BASIC, HOLDER](
  entityClass,
  entityPropertyClass,
  basicClass,
  wrapperSupplier,
  parentEntityPropertyDesc,
  holderDesc,
  name,
  columnName,
  namingType,
  quoteRequired
) {

  override def createProperty: entity.DefaultProperty[_, ENTITY, BASIC] = DefaultPropertyDesc.createPropertySupplier[ENTITY, BASIC, HOLDER](field, entityPropertyClass, wrapperSupplier, holderDesc)()

}

object VersionPropertyDesc {
  def ofBasic[ENTITY, BASIC <: Number, HOLDER](
    entityClass: Class[ENTITY],
    entityPropertyClass: Class[_],
    basicClass: Class[BASIC],
    wrapperSupplier: Supplier[Wrapper[BASIC]],
    name: String,
    columnName: String,
    namingType: NamingType,
    quoteRequired: Boolean
  ): VersionPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER] = {
    new VersionPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER](
      entityClass,
      entityPropertyClass,
      basicClass,
      wrapperSupplier,
      null,
      null,
      name,
      columnName,
      namingType,
      quoteRequired
    )
  }

  def ofHolder[ENTITY, BASIC <: Number, HOLDER](
    entityClass: Class[ENTITY],
    entityPropertyClass: Class[_],
    holderDesc: AbstractHolderDesc[BASIC, HOLDER],
    name: String,
    columnName: String,
    namingType: NamingType,
    quoteRequired: Boolean
  ): VersionPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER] = {
    new VersionPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER](
      entityClass,
      entityPropertyClass,
      holderDesc.getBasicClass.asInstanceOf[Class[BASIC]],
      holderDesc.wrapper,
      null,
      holderDesc,
      name,
      columnName,
      namingType,
      quoteRequired
    )
  }

  def ofAnyVal[ENTITY, BASIC <: Number, HOLDER](
    entityClass: Class[ENTITY],
    entityPropertyClass: Class[_],
    holderDesc: AbstractAnyValHolderDesc[BASIC, HOLDER],
    name: String,
    columnName: String,
    namingType: NamingType,
    quoteRequired: Boolean
  ): VersionPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER] = {
    new VersionPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER](
      entityClass,
      entityPropertyClass,
      holderDesc.getBasicClass,
      holderDesc.wrapperSupplier,
      null,
      holderDesc,
      name,
      columnName,
      namingType,
      quoteRequired
    )
  }
}
