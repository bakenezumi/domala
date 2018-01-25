package domala.jdbc.entity

import domala.jdbc.entity
import domala.jdbc.holder.HolderDesc

class AssignedIdPropertyDesc[PARENT, ENTITY <: PARENT, BASIC, HOLDER] private (
  descParam: EntityPropertyDescParam[ENTITY, BASIC, HOLDER]
) extends org.seasar.doma.jdbc.entity.AssignedIdPropertyType[PARENT, ENTITY, BASIC, HOLDER] (
  descParam.entityClass,
  descParam.entityPropertyClass,
  descParam.typeDesc.getBasicClass,
  descParam.typeDesc.wrapperProvider,
  null,
  HolderDesc.of(descParam.typeDesc),
  descParam.name,
  descParam.column.name,
  descParam.namingType,
  descParam.column.quote) {

 override def createProperty: entity.DefaultProperty[_, ENTITY, BASIC] = DefaultPropertyDesc.createPropertySupplier[ENTITY, BASIC, HOLDER](field, entityPropertyClass, wrapperSupplier, HolderDesc.of(descParam.typeDesc))()

}

object AssignedIdPropertyDesc {

  def apply[ENTITY, BASIC, HOLDER](
    descParam: EntityPropertyDescParam[ENTITY, BASIC, HOLDER]
  ): AssignedIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER] = {
    new AssignedIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER](
      descParam: EntityPropertyDescParam[ENTITY, BASIC, HOLDER])
  }

}
