package domala.jdbc.entity

import domala.jdbc.entity
import domala.jdbc.holder.HolderDesc

class VersionPropertyDesc[PARENT, ENTITY <: PARENT, BASIC <: Number, HOLDER] private (
  descParam: EntityPropertyDescParam[ENTITY, BASIC, HOLDER]
) extends org.seasar.doma.jdbc.entity.VersionPropertyType[PARENT, ENTITY, BASIC, HOLDER](
  descParam.entityClass,
  descParam.entityPropertyClass,
  descParam.typeDesc.getBasicClass,
  descParam.typeDesc.wrapperProvider,
  null,
  HolderDesc.of(descParam.typeDesc),
  descParam.name,
  descParam.column.name,
  descParam.namingType,
  descParam.column.quote
) {

  override def createProperty: entity.DefaultProperty[_, ENTITY, BASIC] = DefaultPropertyDesc.createPropertySupplier[ENTITY, BASIC, HOLDER](field, entityPropertyClass, wrapperSupplier, HolderDesc.of(descParam.typeDesc))()

}

object VersionPropertyDesc {

  def apply[ENTITY, BASIC <: Number, HOLDER](
    descParam: EntityPropertyDescParam[ENTITY, BASIC, HOLDER],
  ): VersionPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER] = {
    new VersionPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER](
      descParam
    )
  }

}
