package domala.internal.macros.reflect

import domala.internal.macros.helper.MacrosHelper
import domala.message.Message

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object HolderReflectionMacros {

  def matchSubclassesImpl[BASIC: c.WeakTypeTag, HOLDER: c.WeakTypeTag](c: blackbox.Context)(basicClass: c.Expr[Class[BASIC]], holderClass: c.Expr[Class[HOLDER]]): c.Expr[BASIC => HOLDER] = {
    import c.universe._
    val basicType = weakTypeOf[BASIC]
    val holderType = weakTypeOf[HOLDER]
    val subclasses: Set[c.universe.Symbol] = holderType.typeSymbol.asClass.knownDirectSubclasses
    if(subclasses.isEmpty) MacrosHelper.abort(Message.DOMALA6007, holderType.typeSymbol.fullName)
    subclasses.find(!_.isModuleClass).foreach(sub => MacrosHelper.abort(Message.DOMALA6008, sub.fullName))
    // (value: BASIC) => value match {
    //   case Child1.value => Child1
    //   case Child2.value => Child2
    // }
    val paramName = holderType.members.filter(m => m.isConstructor).head.asMethod.paramLists.head.head.name.toString
    val cases = subclasses.map { classSymbol: c.universe.Symbol =>
      val subclassName = classSymbol.name.toString
      CaseDef(
        q"${TermName(subclassName)}.${TermName(paramName)}",
        EmptyTree,
        q"${TermName(subclassName)}"
      )
    }.toList
    val partial = Function(
      List(ValDef(Modifiers(Flag.PARAM), TermName(paramName), Ident(basicType.typeSymbol), EmptyTree)),
      Match(Ident(TermName(paramName)), cases)
    )
    c.Expr(partial)
  }

  def matchSubclasses[BASIC, HOLDER](basicClass: Class[BASIC], holderClass: Class[HOLDER]): BASIC => HOLDER = macro matchSubclassesImpl[BASIC, HOLDER]

}
