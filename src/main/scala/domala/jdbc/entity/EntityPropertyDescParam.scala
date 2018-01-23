package domala.jdbc.entity

import domala.Column

case class EntityPropertyDescParam[ENTITY, BASIC, HOLDER] (
  entityClass: Class[ENTITY],
  entityPropertyClass: Class[_],
  typeDesc: SingleTypeDesc[BASIC, HOLDER],
  name: String,
  column: Column,
  namingType: NamingType,
)
