package domala.jdbc.builder


case class Param(paramClass: Class[_], param: Any, index: ParamIndex, literal: Boolean) {
  val name: String = "p" + index.getValue
}
