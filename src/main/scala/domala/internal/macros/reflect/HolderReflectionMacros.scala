package domala.internal.macros.reflect

import domala.internal.macros.helper.MacrosHelper
import domala.message.Message

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object HolderReflectionMacros {

  def matchSubclassesImpl[BASIC: c.WeakTypeTag, HOLDER: c.WeakTypeTag](c: blackbox.Context)(basicClass: c.Expr[Class[BASIC]], holderClass: c.Expr[Class[BASIC]]): c.Expr[BASIC => HOLDER] = {
    import c.universe._
    val basicType = weakTypeOf[BASIC]
    val holderType = weakTypeOf[HOLDER]
    val subclasses: Set[c.universe.Symbol] = holderType.typeSymbol.asClass.knownDirectSubclasses
    if(subclasses.isEmpty) MacrosHelper.abort(Message.DOMALA6007, holderType.typeSymbol.fullName)
    subclasses.find(!_.isModuleClass).foreach(sub => MacrosHelper.abort(Message.DOMALA6008, sub.fullName))
    val cases = subclasses.map(classSymbol => CaseDef(
      q"${TermName(classSymbol.name.toString)}.value",
      EmptyTree,
      q"${TermName(classSymbol.name.toString)}"
    )).toList
    c.Expr(Function(
      List(ValDef(Modifiers(Flag.PARAM), TermName("value"), Ident(basicType.typeSymbol), EmptyTree)),
      Match(Ident(TermName("value")), cases)
    ))
  }

  def matchSubclasses[BASIC, HOLDER](basicClass: Class[BASIC], holderClass: Class[HOLDER]): BASIC => HOLDER = macro matchSubclassesImpl[BASIC, HOLDER]

}
