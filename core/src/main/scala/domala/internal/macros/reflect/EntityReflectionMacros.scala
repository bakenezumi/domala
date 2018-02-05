package domala.internal.macros.reflect

import domala.internal.macros.reflect.util.{MacroTypeConverter, MacroUtil, PropertyDescUtil}
import domala.internal.reflect.util.ReflectionUtil
import domala.internal.reflect.util.ReflectionUtil.extractionClassString
import domala.jdbc.`type`.Types
import domala.jdbc.entity.{EntityDesc, EntityPropertyDesc}
import domala.message.Message
import domala.{Column, Table}
import org.seasar.doma.jdbc.entity._
import org.seasar.doma.jdbc.id.{IdGenerator, SequenceIdGenerator, TableIdGenerator}

import scala.language.experimental.macros
import scala.reflect.ClassTag
import scala.reflect.macros.blackbox

object EntityReflectionMacros {

  private def handle[E: c.WeakTypeTag, R](c: blackbox.Context)(entityClass: c.Expr[Class[E]])(block: => R): R = try {
    block
  } catch {
    case e: ReflectAbortException =>
      import c.universe._
      c.abort(weakTypeOf[E].typeSymbol.pos, e.getLocalizedMessage)
  }

  def generatePropertyDescImpl[
  T: c.WeakTypeTag,
  E: c.WeakTypeTag,
  N: c.WeakTypeTag](c: blackbox.Context)(
    entityClass: c.Expr[Class[E]],
    paramName: c.Expr[String],
    namingType: c.Expr[NamingType],
    isId: c.Expr[Boolean],
    isIdGenerate: c.Expr[Boolean],
    idGenerator: c.Expr[IdGenerator],
    isVersion: c.Expr[Boolean],
    isTenantId: c.Expr[Boolean],
    column: c.Expr[Column]
  )(
    propertyClassTag: c.Expr[ClassTag[T]],
    nakedClassTag: c.Expr[ClassTag[N]]
  ): c.Expr[Map[String, EntityPropertyDesc[E, _]]] = handle(c)(entityClass) {
    PropertyDescUtil.generatePropertyDescImpl[T, E, N](c)(
      entityClass,
      paramName,
      namingType,
      isId,
      isIdGenerate,
      idGenerator,
      isVersion,
      isTenantId,
      column
    )(propertyClassTag, nakedClassTag)
  }

  def generatePropertyDesc[T, E, N](
    entityClass: Class[E],
    paramName: String,
    namingType: NamingType,
    isId: Boolean,
    isIdGenerate: Boolean,
    idGenerator: IdGenerator,
    isVersion: Boolean,
    isTenantId: Boolean,
    column: Column
  )(
    implicit propertyClassTag: ClassTag[T],
    nakedClassTag: ClassTag[N]
  ): Map[String, EntityPropertyDesc[E, _]] = macro generatePropertyDescImpl[T, E, N]

  def readPropertyImpl[T: c.WeakTypeTag, E: c.WeakTypeTag](c: blackbox.Context)(
    entityClass: c.Expr[Class[E]],
    args: c.Expr[java.util.Map[String, Property[E, _]]],
    propertyName: c.Expr[String])(
    propertyClassTag: c.Expr[ClassTag[T]]): c.Expr[T] = handle(c)(entityClass) {
    import c.universe._
    val wtt = weakTypeOf[T]
    MacroTypeConverter.of(c).toType(wtt) match {
      case Types.MacroEntityType =>
        c.Expr[T] {
          q"""
            import scala.collection.JavaConverters._
            domala.internal.macros.reflect.EmbeddableReflectionMacros.generateEmbeddableDesc[$wtt](classOf[$wtt]).newEmbeddable($propertyName, $args.asScala.toMap, $entityClass)
          """
        }
      case _ =>
        reify {
          (if (args.splice.get(propertyName.splice) != null)
            args.splice.get(propertyName.splice).get
          else null).asInstanceOf[T]
        }
    }
  }

  def readProperty[T, E](
    entityClass: Class[E],
    args: java.util.Map[String, Property[E, _]],
    propertyName: String)(implicit propertyClassTag: ClassTag[T]): T = macro readPropertyImpl[T, E]


  def validateListenerImpl[E: c.WeakTypeTag, T: c.WeakTypeTag](c: blackbox.Context)(
    entityClass: c.Expr[Class[E]],
    listenerClass: c.Expr[Class[T]]): c.Expr[Unit] = handle(c)(entityClass) {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (tpe.typeSymbol.isAbstract)
      ReflectionUtil.abort(
        Message.DOMALA4166,
        extractionClassString(tpe.toString))
    val ctor = tpe.typeSymbol.asClass.primaryConstructor
    if (!ctor.isPublic || ctor.asMethod.paramLists.flatten.nonEmpty)
      ReflectionUtil.abort(
        Message.DOMALA4167,
        extractionClassString(tpe.toString))
    val entityType = tpe.baseType(typeOf[EntityListener[_]].typeSymbol.asClass).typeArgs.head
    // TODO: 互換性がある場合は通す
    if (!(weakTypeOf[E] =:= entityType))
      ReflectionUtil.abort(
        Message.DOMALA4229,
        typeOf[EntityListener[_]].typeSymbol.typeSignature.typeParams.head.name,
        entityType.toString,
        weakTypeOf[E].toString
      )
    reify(())
  }

  def validateListener[E, T <: EntityListener[_]](entityClass: Class[E], listenerClass: Class[T]): Unit = macro validateListenerImpl[E, T]

  def validateTableIdGeneratorImpl[E: c.WeakTypeTag, G: c.WeakTypeTag](c: blackbox.Context)(
    entityClass: c.Expr[Class[E]],
    generatorClass: c.Expr[Class[G]]): c.Expr[Unit] = handle(c)(entityClass) {
    import c.universe._
    val tpe = weakTypeOf[G]
    if (tpe.typeSymbol.isAbstract)
      ReflectionUtil.abort(
        Message.DOMALA4168,
        extractionClassString(tpe.toString))
    val ctor = tpe.typeSymbol.asClass.primaryConstructor
    if (!ctor.isPublic || ctor.asMethod.paramLists.flatten.nonEmpty)
      ReflectionUtil.abort(
        Message.DOMALA4169,
        extractionClassString(tpe.toString))
    reify(())
  }

  def validateTableIdGenerator[E, G <: TableIdGenerator](entityClass: Class[E], generatorClass: Class[G]): Unit = macro validateTableIdGeneratorImpl[E, G]

  def validateSequenceIdGeneratorImpl[E: c.WeakTypeTag, G: c.WeakTypeTag](c: blackbox.Context)(
    entityClass: c.Expr[Class[E]],
    generatorClass: c.Expr[Class[G]]): c.Expr[Unit] = handle(c)(entityClass) {
    import c.universe._
    val tpe = weakTypeOf[G]
    if (tpe.typeSymbol.isAbstract)
      ReflectionUtil.abort(
        Message.DOMALA4170,
        extractionClassString(tpe.toString))
    val ctor = tpe.typeSymbol.asClass.primaryConstructor
    if (!ctor.isPublic || ctor.asMethod.paramLists.flatten.nonEmpty)
      ReflectionUtil.abort(
        Message.DOMALA4171,
        extractionClassString(tpe.toString))
    reify(())
  }

  def validateSequenceIdGenerator[E, G <: SequenceIdGenerator](entityClass: Class[E], generatorClass: Class[G]): Unit = macro validateSequenceIdGeneratorImpl[E, G]

  def generateEntityDescImpl[E: c.WeakTypeTag](c: blackbox.Context): c.Expr[EntityDesc[E]] = {
    import c.universe._
    val entityType = weakTypeOf[E]
    val entityClass = c.Expr[Class[E]](q"classOf[$entityType]")

    case class EntityPropertyMeta(
      propertyType: Type,
      nakedType: Type,
      name: String,
      isId: Boolean,
      isIdGenerate: Boolean,
      idGenerator: Tree,
      isVersion: Boolean,
      isTenantId: Boolean,
      column: c.Expr[Column]
    ) {
      Seq((isId, "@Id"), (isTenantId, "@TenantId"), (isVersion, "@Version")).collect {
        case (true, annotationName) => annotationName
      } match {
        case x :: y :: _ => ReflectionUtil.abort(Message.DOMALA4086, x, y, entityType.typeSymbol.fullName, name)
        case _ => ()
      }
    }

    handle(c)(entityClass) {
      MacroTypeConverter.of(c).toType(entityType) match {
        case Types.GeneratedEntityType =>
          c.Expr[EntityDesc[E]] {
            q"domala.internal.reflect.util.ReflectionUtil.getEntityDesc[$entityType]"
          }
        case t if t.isMacroEntity =>
          val table = entityType.typeSymbol.annotations.collectFirst {
            case a: Annotation if a.tree.tpe =:= typeOf[Table] =>
              c.Expr[Table](q"domala.Table(..${a.tree.children.tail})")
          }.getOrElse {
            c.Expr[Table](q" domala.Table()")
          }

          val entityPropertyTypeSymbols = entityType.typeSymbol.asClass.primaryConstructor.asMethod.paramLists.flatten
          def confirmAnnotation(annotations: Seq[Annotation], tpe: Type): Boolean = annotations.exists(_.tree.tpe =:= tpe)

          val entityPropertyMetaList: Seq[EntityPropertyMeta] = entityPropertyTypeSymbols.map((param: Symbol) => {
            val isId = confirmAnnotation(param.annotations, typeOf[domala.Id])
            val idGenerator = if (isId) {
              param.annotations.collectFirst {
                case a: Annotation if a.tree.tpe =:= typeOf[domala.GeneratedValue] =>
                  a.tree.children.tail.head match {
                    case Select(_, TermName("IDENTITY")) => q"new org.seasar.doma.jdbc.id.BuiltinIdentityIdGenerator()"
                    case x => ReflectionUtil.abort(Message.DOMALA6026, entityType.typeSymbol.fullName, param.name, x)
                  }
              }.getOrElse(q"null")
            } else {
              q"null"
            }
            val isIdGenerate = confirmAnnotation(param.annotations, typeOf[domala.GeneratedValue])
            val isVersion = confirmAnnotation(param.annotations, typeOf[domala.Version])
            val isTenantId = confirmAnnotation(param.annotations, typeOf[domala.TenantId])

            val propertyType = param.typeSignature
            val column = param.annotations.collectFirst {
              case a: Annotation if a.tree.tpe =:= typeOf[domala.Column] =>
                c.Expr[Column](q"domala.Column(..${a.tree.children.tail})")
            }.getOrElse(c.Expr[Column](q"domala.Column()"))
            val nakedTpe = MacroTypeConverter.of(c).toType(propertyType) match {
              case _: Types.Basic[_] => propertyType
              case _: Types.Option => propertyType.typeArgs.head
              case t if t.isHolder => propertyType
              case Types.MacroEntityType => propertyType
              case _ => ReflectionUtil.abort(Message.DOMALA4096, propertyType, entityType.typeSymbol.fullName, param.name)
            }
            EntityPropertyMeta(propertyType, nakedTpe, param.name.toString, isId, isIdGenerate, idGenerator, isVersion, isTenantId, column)
          })

          if(entityPropertyMetaList.count(_.isIdGenerate) > 1)
            ReflectionUtil.abort(Message.DOMALA4037, entityType.typeSymbol.fullName, entityPropertyMetaList.filter(_.isIdGenerate).tail.head.name)
          entityPropertyMetaList.find(_.isIdGenerate).foreach { p =>
            if(entityPropertyMetaList.count(_.isId) > 1)
              ReflectionUtil.abort(Message.DOMALA4036, entityType.typeSymbol.fullName)
            if(!p.isId)
              ReflectionUtil.abort(Message.DOMALA4033, entityType.typeSymbol.fullName, p.name)
          }
          if(entityPropertyMetaList.count(_.isVersion) > 1)
            ReflectionUtil.abort(Message.DOMALA4024, entityType.typeSymbol.fullName, entityPropertyMetaList.filter(_.isVersion).tail.head.name)
          if(entityPropertyMetaList.count(_.isTenantId) > 1)
            ReflectionUtil.abort(Message.DOMALA4442, entityType.typeSymbol.fullName, entityPropertyMetaList.filter(_.isTenantId).tail.head.name)

          val generatePropertyDescMap = {
            val propertyDescList = entityPropertyMetaList.map((propertyMeta: EntityPropertyMeta) => {
               c.Expr[Map[String, EntityPropertyDesc[E, _]]] {
                q"""
                  domala.internal.macros.reflect.EntityReflectionMacros.generatePropertyDesc[${propertyMeta.propertyType}, $entityType, ${propertyMeta.nakedType}](
                  $entityClass,
                  ${propertyMeta.name},
                  null,
                  ${propertyMeta.isId},
                  ${propertyMeta.isIdGenerate},
                  ${propertyMeta.idGenerator},
                  ${propertyMeta.isVersion},
                  ${propertyMeta.isTenantId},
                  ${propertyMeta.column})
                """
              }
            })

            c.Expr[Map[String, EntityPropertyDesc[E, _]]] {
              q"Seq(..$propertyDescList).flatten.toMap"
            }
          }

          val companion = entityType.companion.typeSymbol
          val apply = companion.typeSignature.member(TermName("apply")).asMethod
          val (applyParams, imports): (Seq[Tree], Seq[Tree]) = apply.paramLists.head.map { p =>
            val parameterType = p.typeSignature
            (
              MacroTypeConverter.of(c).toType(parameterType) match {
                case Types.MacroEntityType =>
                  q"""
                    val propertyName = ${p.name.toString}
                    val desc = domala.internal.macros.reflect.EmbeddableReflectionMacros.generateEmbeddableDesc(classOf[$parameterType])
                    desc.newEmbeddable[$entityType](propertyName, map.asScala.toMap, $entityClass)
                    """
                        case _ =>
                          q"""
                    val propertyName = ${p.name.toString}
                    if (map.containsKey(propertyName))
                      map.get(propertyName).asInstanceOf[domala.jdbc.entity.Property[$entityType, _]].get().asInstanceOf[$parameterType]
                    else
                      throw new org.seasar.doma.DomaException(domala.message.Message.DOMALA6024, $entityClass.getName(), ${p.name.toString}, ${parameterType.typeSymbol.fullName})
                    """
              }, {
              val pt = if (parameterType <:< typeOf[Option[_]]) parameterType.typeArgs.head
              else parameterType
              MacroUtil.generateImport(c)(pt).getOrElse(q"()")

            })
          }.unzip

          c.Expr[EntityDesc[E]] {
            q"""{
              domala.internal.jdbc.entity.MacroEntityDesc.of[$entityType]($entityClass, {
                import scala.collection.JavaConverters._
                ..$imports
                new domala.jdbc.entity.AbstractEntityDesc[$entityType] {
                  import domala.jdbc.entity._
                  override type ENTITY_LISTENER = NullEntityListener[$entityType]

                  override val listener = new NullEntityListener[$entityType]()

                  override val table: domala.Table = $table

                  override protected val propertyDescMap: Map[String, EntityPropertyDesc[$entityType, _]] = $generatePropertyDescMap

                  override protected val idPropertyDescList: List[EntityPropertyDesc[$entityType, _]] = propertyDescMap.values.collect {
                    case p: AssignedIdPropertyDesc[_, _, _, _] => p: EntityPropertyDesc[$entityType, _]
                    case p: GeneratedIdPropertyDesc[_, _, _, _] => p: EntityPropertyDesc[$entityType, _]
                  }.toList

                  override def getNamingType: NamingType = null

                  override def getTenantIdPropertyType: TenantIdPropertyDesc[_ >: $entityType, $entityType, _, _] = propertyDescMap.values.collectFirst {
                    case p: TenantIdPropertyDesc[_, _, _, _] => p
                  }.orNull.asInstanceOf[TenantIdPropertyDesc[_ >: $entityType, $entityType, _, _]]

                  override def getVersionPropertyType: VersionPropertyDesc[_ >: $entityType, $entityType, _ <: Number, _] = propertyDescMap.values.collectFirst {
                    case p: VersionPropertyDesc[_, _, _, _] => p
                  }.orNull.asInstanceOf[VersionPropertyDesc[_ >: $entityType, $entityType, _ <: Number, _]]

                  override def newEntity(map: java.util.Map[String, Property[$entityType, _]]): $entityType = ${apply.asTerm}(..$applyParams)

                  override def getGeneratedIdPropertyType: org.seasar.doma.jdbc.entity.GeneratedIdPropertyType[_ >: $entityType, $entityType, _ <: Number, _] = propertyDescMap.values.collectFirst {
                    case p: org.seasar.doma.jdbc.entity.GeneratedIdPropertyType[_, _, _, _] => p
                  }.orNull.asInstanceOf[GeneratedIdPropertyDesc[_ >: $entityType, $entityType, _ <: Number, _]]

                  override def getName = ${entityType.typeSymbol.name.toString}
                }
              })
            }"""
          }
        case _ =>
          ReflectionUtil.abort(
            Message.DOMALA6025,
            entityType.typeSymbol.fullName, MacroUtil.getPropertyErrorMessage(c)(entityType))
      }
    }
  }

}
