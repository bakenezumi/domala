package domala.jdbc.entity

import java.util.function.Supplier

import domala.jdbc.entity
import domala.jdbc.holder.AbstractHolderDesc
import org.seasar.doma.jdbc.domain.DomainType
import org.seasar.doma.jdbc.entity.EntityPropertyType
import org.seasar.doma.jdbc.entity.NamingType
import org.seasar.doma.wrapper.Wrapper

class AssignedIdPropertyType[PARENT, ENTITY <: PARENT, BASIC, HOLDER] (
  entityClass: Class[ENTITY],
  entityPropertyClass: Class[_],
  basicClass: Class[BASIC],
  wrapperSupplier: Supplier[Wrapper[BASIC]],
  parentEntityPropertyType: EntityPropertyType[PARENT, BASIC],
  holderType: DomainType[BASIC, HOLDER],
  name: String,
  columnName: String,
  namingType: NamingType,
  quoteRequired: Boolean
) extends org.seasar.doma.jdbc.entity.AssignedIdPropertyType[PARENT, ENTITY, BASIC, HOLDER] (
  entityClass,
  entityPropertyClass,
  basicClass,
  wrapperSupplier,
  parentEntityPropertyType,
  holderType,
  name,
  columnName,
  namingType,
  quoteRequired) {

 override def createProperty: entity.DefaultProperty[_, ENTITY, BASIC] = DefaultPropertyType.createPropertySupplier[ENTITY, BASIC, HOLDER](field, entityPropertyClass, wrapperSupplier, holderType)()

}

object AssignedIdPropertyType {
  def ofHolder[ENTITY, BASIC, HOLDER](
    entityClass: Class[ENTITY],
    entityPropertyClass: Class[_],
    domainType: AbstractHolderDesc[BASIC, HOLDER],
    name: String,
    columnName: String,
    namingType: NamingType,
    quoteRequired: Boolean
  ): AssignedIdPropertyType[ENTITY, ENTITY, BASIC, HOLDER] = {
    new AssignedIdPropertyType[ENTITY, ENTITY, BASIC, HOLDER](
      entityClass,
      entityPropertyClass,
      domainType.getBasicClass.asInstanceOf[Class[BASIC]],
      domainType.wrapper,
      null,
      domainType,
      name,
      columnName,
      namingType,
      quoteRequired
    )
  }
}