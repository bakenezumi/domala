package domala.internal.jdbc.holder

import java.util.function.Supplier

import domala.internal.reflect.util.ClassTypeConverter
import domala.jdbc.`type`.Types
import domala.jdbc.holder.{AbstractAnyValHolderDesc, HolderDesc}
import domala.wrapper._
import org.seasar.doma.jdbc.ClassHelper

object AnyValHolderDescRepository {

  private[this] val cacheByClass = scala.collection.concurrent.TrieMap[String, HolderDesc[_,_]]()
  private[this] val cacheByType = scala.collection.concurrent.TrieMap[String, HolderDesc[_,_]]()

  def getByClass(holderClass: Class[_], classHelper: ClassHelper): HolderDesc[Any, Any] =
    cacheByClass.getOrElseUpdate(holderClass.getName + holderClass.hashCode(), {
      val constructors = holderClass.getConstructors
      if (constructors.length != 1) return null
      val constructor = constructors.head
      val parameterTypes = constructor.getParameterTypes
      if (parameterTypes.length != 1) return null
      val elementType = parameterTypes.head
      val fieldName = constructor.getParameters.head.getName
      Option[Supplier[Wrapper[Any]]] {
        ClassTypeConverter.toType(elementType) match {
          case t: Types.Basic[_] => t.wrapperSupplier.asInstanceOf[Supplier[Wrapper[Any]]]
          case _ => null
        }
      }.map { wrapperSupplier =>
        new AbstractAnyValHolderDesc[Any, Any](wrapperSupplier) {
          override def newHolder(value: Any): Any = {
            constructor.newInstance((if (value == null && elementType.isPrimitive) 0 else value).asInstanceOf[Object])
          }

          override def getBasicValue(holder: Any): Any =
            if (holder == null) null
            else if (holder.getClass != holderClass) holder
            else holderClass.getMethod(fieldName).invoke(holder)
        }
      }.orNull
    }).asInstanceOf[HolderDesc[Any, Any]]

  def getByType[BASIC, HOLDER](holderClass: Class[HOLDER], op: => HolderDesc[BASIC, HOLDER]): HolderDesc[BASIC, HOLDER] = {
    cacheByType.getOrElseUpdate(holderClass.getName + holderClass.hashCode(), op).asInstanceOf[HolderDesc[BASIC, HOLDER]]
  }

  def clearCache(): Unit = {
    cacheByClass.clear()
    cacheByType.clear()
  }

}
