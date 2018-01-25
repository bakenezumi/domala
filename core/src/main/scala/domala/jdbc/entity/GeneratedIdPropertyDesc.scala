package domala.jdbc.entity

import domala.jdbc.entity
import domala.jdbc.holder.HolderDesc
import org.seasar.doma.jdbc.id.IdGenerator

class GeneratedIdPropertyDesc[PARENT, ENTITY <: PARENT, BASIC <: Number, HOLDER] private (
  descParam: EntityPropertyDescParam[ENTITY, BASIC, HOLDER],
  _idGenerator: IdGenerator
) extends org.seasar.doma.jdbc.entity.GeneratedIdPropertyType[PARENT, ENTITY, BASIC, HOLDER] (
  descParam.entityClass,
  descParam.entityPropertyClass,
  descParam.typeDesc.getBasicClass,
  descParam.typeDesc.wrapperProvider,
  null,
  HolderDesc.of(descParam.typeDesc),
  descParam.name,
  descParam.column.name,
  descParam.namingType,
  descParam.column.quote,
  _idGenerator) {

  override def createProperty: entity.DefaultProperty[_, ENTITY, BASIC] = DefaultPropertyDesc.createPropertySupplier[ENTITY, BASIC, HOLDER](field, entityPropertyClass, wrapperSupplier, HolderDesc.of(descParam.typeDesc))()

}

object GeneratedIdPropertyDesc {
  def apply[ENTITY, BASIC <: Number, HOLDER](idGenerator: IdGenerator)(
    descParam: EntityPropertyDescParam[ENTITY, BASIC, HOLDER],
  ) = new GeneratedIdPropertyDesc[ENTITY, ENTITY, BASIC, HOLDER](descParam, idGenerator)
}