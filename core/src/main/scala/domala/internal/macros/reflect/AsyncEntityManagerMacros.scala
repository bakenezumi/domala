package domala.internal.macros.reflect

import domala.async.AsyncAction
import domala.async.jdbc._

import scala.reflect.macros.blackbox


// TODO: Driver level non-blocking
object AsyncEntityManagerMacros {

  def insert[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entity: c.Expr[ENTITY])(config: c.Expr[AsyncConfig]): c.Expr[AsyncResult[ENTITY]] = {
    import c.universe._
    reify {
      AsyncAction {
        EntityManagerMacros.insert[ENTITY](c)(entity)(config).splice
      }(config.splice)
    }
  }
  def update[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entity: c.Expr[ENTITY])(config: c.Expr[AsyncConfig]): c.Expr[AsyncResult[ENTITY]] = {
    import c.universe._
    reify {
      AsyncAction {
        EntityManagerMacros.update[ENTITY](c)(entity)(config).splice
      }(config.splice)
    }
  }
  def delete[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entity: c.Expr[ENTITY])(config: c.Expr[AsyncConfig]): c.Expr[AsyncResult[ENTITY]] = {
    import c.universe._
    reify {
      AsyncAction {
        EntityManagerMacros.delete[ENTITY](c)(entity)(config).splice
      }(config.splice)
    }
  }

  def batchInsert[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entities: c.Expr[Iterable[ENTITY]])(config: c.Expr[AsyncConfig]): c.Expr[AsyncBatchResult[ENTITY]] = {
    import c.universe._
    reify {
      AsyncAction {
        EntityManagerMacros.batchInsert[ENTITY](c)(entities)(config).splice
      }(config.splice)
    }
  }
  def batchUpdate[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entities: c.Expr[Iterable[ENTITY]])(config: c.Expr[AsyncConfig]): c.Expr[AsyncBatchResult[ENTITY]] = {
    import c.universe._
    reify {
      AsyncAction {
        EntityManagerMacros.batchUpdate[ENTITY](c)(entities)(config).splice
      }(config.splice)
    }
  }
  def batchDelete[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entities: c.Expr[Iterable[ENTITY]])(config: c.Expr[AsyncConfig]): c.Expr[AsyncBatchResult[ENTITY]] = {
    import c.universe._
    reify {
      AsyncAction {
        EntityManagerMacros.batchDelete[ENTITY](c)(entities)(config).splice
      }(config.splice)
    }
  }

}
