package domala.internal.macros.reflect

import domala.internal.reflect.util.ReflectionUtil
import domala.message.Message
import org.seasar.doma.DomaException

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object HolderReflectionMacros {

  private def handle[HOLDER: c.WeakTypeTag, R](c: blackbox.Context)(holderClass: c.Expr[Class[HOLDER]])(block: => R): R = try {
    block
  } catch {
    case e: ReflectAbortException =>
      import c.universe._
      c.abort(weakTypeOf[HOLDER].typeSymbol.pos, e.getLocalizedMessage)
  }

  def assertUnique(handler: () => Unit)(args: Any*) : Unit = {
    if(args.size != args.toSet.size) handler()
  }

  def getSubclasses[HOLDER: c.WeakTypeTag](c: blackbox.Context)(holderType: c.Type): Set[c.universe.Symbol] = {
    val subclasses: Set[c.universe.Symbol] = holderType.typeSymbol.asClass.knownDirectSubclasses
    if(subclasses.isEmpty) ReflectionUtil.abort(Message.DOMALA6007, holderType.typeSymbol.fullName)
    subclasses.find(!_.isModuleClass).foreach(sub => ReflectionUtil.abort(Message.DOMALA6008, sub.fullName))
    subclasses
  }

  def assertSubclassesImpl[HOLDER: c.WeakTypeTag](c: blackbox.Context)(
    holderClass: c.Expr[Class[HOLDER]]): c.Expr[Unit] = handle(c)(holderClass) {
    import c.universe._
    val holderType = weakTypeOf[HOLDER]
    val subclasses: Set[c.universe.Symbol] = getSubclasses(c)(holderType)

    // domala.internal.macros.reflect.HolderReflectionMacros.assertUnique(handler)(Child1.value, Child2.value, ...)
    val holderTypeName = c.Expr(Literal(Constant(holderType.typeSymbol.fullName)))
    val handler = reify {
      () => throw new DomaException(Message.DOMALA6016, holderTypeName.splice.toString)
    }
    val paramName = holderType.members.filter(m => m.isConstructor).head.asMethod.paramLists.head.head.name.toString

    val assertParams = subclasses.map { classSymbol: c.universe.Symbol =>
      val subclassName = classSymbol.name.toString
      Select(
        Ident(TermName(subclassName)),
        TermName(paramName)
      )
    }.toList

    val assertUnique =
      q"""
      domala.internal.macros.reflect.HolderReflectionMacros.assertUnique($handler)($assertParams : _*)
      """
    c.Expr(assertUnique).asInstanceOf[c.Expr[Unit]]
  }

  def assertSubclasses[HOLDER](holderClass: Class[HOLDER]): Unit = macro assertSubclassesImpl[HOLDER]

  def matchSubclassesImpl[BASIC: c.WeakTypeTag, HOLDER: c.WeakTypeTag](c: blackbox.Context)(basicClass: c.Expr[Class[BASIC]], holderClass: c.Expr[Class[HOLDER]]): c.Expr[BASIC => HOLDER] = {
    import c.universe._
    val basicType = weakTypeOf[BASIC]
    val holderType = weakTypeOf[HOLDER]
    val subclasses: Set[c.universe.Symbol] = getSubclasses(c)(holderType)

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
