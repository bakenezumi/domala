package domala.jdbc.entity

import java.util.function.Supplier

import domala.jdbc.entity
import domala.jdbc.holder.AbstractHolderDesc
import org.seasar.doma.jdbc.domain.DomainType
import org.seasar.doma.jdbc.entity.EntityPropertyType
import org.seasar.doma.jdbc.entity.NamingType
import org.seasar.doma.wrapper.Wrapper

class VersionPropertyType[PARENT, ENTITY <: PARENT, BASIC <: Number, DOMAIN](
  entityClass: Class[ENTITY],
  entityPropertyClass: Class[_],
  basicClass: Class[BASIC],
  wrapperSupplier: Supplier[Wrapper[BASIC]],
  parentEntityPropertyType: EntityPropertyType[PARENT, BASIC],
  domainType: DomainType[BASIC, DOMAIN],
  name: String,
  columnName: String,
  namingType: NamingType,
  quoteRequired: Boolean
) extends org.seasar.doma.jdbc.entity.VersionPropertyType[PARENT, ENTITY, BASIC, DOMAIN](
  entityClass,
  entityPropertyClass,
  basicClass,
  wrapperSupplier,
  parentEntityPropertyType,
  domainType,
  name,
  columnName,
  namingType,
  quoteRequired
) {

  override def createProperty: entity.DefaultProperty[_, ENTITY, BASIC] = DefaultPropertyType.createPropertySupplier[ENTITY, BASIC, DOMAIN](field, entityPropertyClass, wrapperSupplier, domainType)()

}

object VersionPropertyType {
  def ofDomain[ENTITY, BASIC <: Number, DOMAIN](
    entityClass: Class[ENTITY],
    entityPropertyClass: Class[_],
    domainType: AbstractHolderDesc[BASIC, DOMAIN],
    name: String,
    columnName: String,
    namingType: NamingType,
    quoteRequired: Boolean
  ): VersionPropertyType[ENTITY, ENTITY, BASIC, DOMAIN] = {
    new VersionPropertyType[ENTITY, ENTITY, BASIC, DOMAIN](
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
