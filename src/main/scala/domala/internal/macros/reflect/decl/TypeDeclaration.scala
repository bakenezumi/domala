package domala.internal.macros.reflect.decl

import domala.internal.macros.reflect.util.{ElementUtil, MacroTypeConverter, AnyValHolderDescGenerator}
import domala.jdbc.`type`.Types

import scala.reflect.macros.blackbox
import org.seasar.doma.internal.util.AssertionUtil.{assertNotNull, assertTrue}

import scala.collection.mutable

//noinspection NameBooleanParameters
class TypeDeclaration[C <: blackbox.Context](c: C)(
    val tpe: C#Type
) {
  import c.universe._

  private[this] val converter = MacroTypeConverter.of(c)
  val convertedType: Types = converter.toType(tpe)

  val NUMBER_PRIORITY_MAP: Map[String, Int] = Map(
    c.typeOf[java.math.BigDecimal].toString -> 80,
    c.typeOf[BigDecimal].toString -> 80,
    c.typeOf[java.math.BigInteger].toString -> 70,
    c.typeOf[BigInt].toString -> 70,
    c.typeOf[Double].toString -> 60,
    c.typeOf[Float].toString -> 50,
    c.typeOf[Long].toString -> 40,
    c.typeOf[Int].toString -> 30,
    c.typeOf[Integer].toString -> 30,
    c.typeOf[Short].toString -> 20,
    c.typeOf[Byte].toString -> 10
  )

  val numberPriority: Option[Int] = {
    if (convertedType.isFunction && tpe.typeArgs.nonEmpty) {
      NUMBER_PRIORITY_MAP.get(tpe.typeArgs.last.toString)
    } else NUMBER_PRIORITY_MAP.get(tpe.toString)
  }
  val typeParameterDeclarationsMap
    : mutable.Map[String, Seq[TypeParameterDeclaration[C]]] =
    TypeDeclaration.gatherTypeParameterDeclarations(c)(tpe)

  def getType: C#Type = tpe
  def getBinaryName: String = tpe.toString
  def isBooleanType: Boolean = tpe <:< c.typeOf[Boolean]
  def isNullType: Boolean = tpe <:< c.typeOf[Null]
  def isNumberType: Boolean = numberPriority.nonEmpty
  def isSameType(other: TypeDeclaration[C]): Boolean =
    tpe =:= other.tpe ||
      (this.isNumberType && other.isNumberType && this.numberPriority == other.numberPriority)
  def isTextType: Boolean =
    tpe <:< c.typeOf[String] || tpe <:< c.typeOf[Char] || tpe <:< c
      .typeOf[Character]
  def emulateConcatOperation(other: TypeDeclaration[C]): TypeDeclaration[C] = {
    assertNotNull(other, "")
    assertTrue(isTextType)
    assertTrue(other.isTextType)
    TypeDeclaration.newTypeDeclaration[C](c)(c.typeOf[String])
  }
  def emulateArithmeticOperation(
      other: TypeDeclaration[C]): TypeDeclaration[C] = {
    assertNotNull(other, "")
    assertTrue(isNumberType)
    assertTrue(other.isNumberType)
    val tpe: C#Type =
      if (this.numberPriority.get >= other.numberPriority.get) this.tpe
      else other.tpe
    TypeDeclaration.newTypeDeclaration[C](c)(tpe)
  }
  def getMethodDeclarations(name: String,
                            parameterTypeDeclarations: Seq[TypeDeclaration[C]])
    : Seq[MethodDeclaration[C]] =
    getMethodDeclarationsInternal(name, parameterTypeDeclarations, false)
  def getStaticMethodDeclarations(
      name: String,
      parameterTypeDeclarations: Seq[TypeDeclaration[C]])
    : Seq[MethodDeclaration[C]] =
    getMethodDeclarationsInternal(name, parameterTypeDeclarations, true)
  protected def getMethodDeclarationsInternal(
      name: String,
      parameterTypeDeclarations: Seq[TypeDeclaration[C]],
      statik: Boolean): Seq[MethodDeclaration[C]] = {
    val candidates =
      getCandidateMethodDeclarations(name, parameterTypeDeclarations, statik)
    candidates
  }
  protected def getCandidateMethodDeclarations(
      name: String,
      parameterTypeDeclarations: Seq[TypeDeclaration[C]],
      statik: Boolean): Seq[MethodDeclaration[C]] = {
    val results = mutable.Buffer[MethodDeclaration[C]]()
    for ((binaryName, typeParameterDeclarations) <- typeParameterDeclarationsMap) {
      val typeElement = ElementUtil.getTypeElement[C](c)(binaryName)
      typeElement.foreach(e =>
        for (method: MethodSymbol <- e.members.collect {
               case m: MethodSymbol
                   if m.isPublic && m.name.toString == name && !(m.returnType =:= c
                     .typeOf[Unit]) => m
             }) {
          val parameters = method.paramLists.flatten.map(_.typeSignature)
          if (parameters.size == parameterTypeDeclarations.size) {
            if (parameterTypeDeclarations.map(_.getType)
                  .zip(parameters.map(p => resolveTypeParameter(p, typeParameterDeclarations)))
                  .forall(t => t._1 <:< t._2 )) {
              val methodDeclaration =
                new MethodDeclaration(c)(method, typeParameterDeclarations)
              results += methodDeclaration
            }
          }
      })
    }
    results
  }
  def getTypeParameterDeclarations: Seq[TypeParameterDeclaration[C]] = {
    typeParameterDeclarationsMap.values.headOption.getOrElse(Nil)
  }
  def getFieldDeclaration(name: String): FieldDeclaration[C] =
    getFieldDeclarationInternal(name, false)
  def getStaticFieldDeclaration(name: String): FieldDeclaration[C] =
    getFieldDeclarationInternal(name, true)
  def getFieldDeclarationInternal(name: String,
                                  statik: Boolean): FieldDeclaration[C] = {
    val candidates = getCandidateFieldDeclaration(name, statik)
    if (candidates.isEmpty) return null
    if (candidates.size == 1) return candidates.head
    c.abort(c.enclosingPosition, name)
  }
  def getCandidateFieldDeclaration(
      name: String,
      statik: Boolean): Seq[FieldDeclaration[C]] = {
    val results = mutable.Buffer[FieldDeclaration[C]]()
    for ((typeQualifiedName, typeParameterDeclarations) <- typeParameterDeclarationsMap) {
      val typeElement = ElementUtil.getTypeElement[C](c)(typeQualifiedName)
      typeElement.foreach { e =>
        for (field: TermSymbol <- e.members.collect {
               case f: TermSymbol
                   if !(statik && !f.isStatic) && f.name.toString == name && !f.isMethod =>
                 f
             }) {
          val fieldDeclaration =
            new FieldDeclaration(c)(field, typeParameterDeclarations)
          results += fieldDeclaration
      }}
    }
    results
  }
  protected def resolveTypeParameter(
      formalType: C#Type,
      typeParameterDeclarations: Seq[TypeParameterDeclaration[C]]): C#Type = {
    typeParameterDeclarations.collectFirst {
      case typeParameterDecl if formalType.toString == typeParameterDecl.formalTypeName => typeParameterDecl.actualType
    }.getOrElse(formalType)
  }
}

object TypeDeclaration {
  def newBooleanTypeDeclaration[C <: blackbox.Context](
      c: C): TypeDeclaration[C] = {
    new TypeDeclaration[C](c)(c.typeOf[Boolean])
  }
  def newTypeDeclaration[C <: blackbox.Context](c: C)(
      tpe: C#Type): TypeDeclaration[C] = {
    new TypeDeclaration[C](c)(tpe)
  }
  def newUnknownTypeDeclaration[C <: blackbox.Context](
      c: C): TypeDeclaration[C] = {
    new TypeDeclaration[C](c)(c.typeOf[Nothing])
  }
  protected def gatherTypeParameterDeclarations[C <: blackbox.Context](c: C)(
      tpe: C#Type): mutable.Map[String, Seq[TypeParameterDeclaration[C]]] = {
    val typeParameterDeclarationsMap =
      mutable.Map[String, Seq[TypeParameterDeclaration[C]]]()
    val typeSymbol = tpe.typeSymbol.asType
    if (typeSymbol == null) return typeParameterDeclarationsMap
    typeParameterDeclarationsMap.put(typeSymbol.fullName,
                                     createTypeParameterDeclarations(c)(tpe))
    typeParameterDeclarationsMap
  }
  def createTypeParameterDeclarations[C <: blackbox.Context](c: C)(
      tpe: C#Type): Seq[TypeParameterDeclaration[C]] = {
    assertNotNull(tpe, c)
    tpe.typeSymbol.typeSignature.typeParams.zip(tpe.typeArgs).map { t =>
      new TypeParameterDeclaration[C](c)(t._1.asInstanceOf[C#Symbol],
                                         t._2.asInstanceOf[C#Type])
    }
  }
}
