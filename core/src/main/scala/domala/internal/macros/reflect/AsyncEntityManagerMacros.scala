package domala.internal.macros.reflect

import domala.async.jdbc.{AsyncConfig, FutureBatchResult, FutureResult}

import scala.reflect.macros.blackbox


// TODO: Driver level non-blocking
object AsyncEntityManagerMacros {

  def insert[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entity: c.Expr[ENTITY])(config: c.Expr[AsyncConfig]): c.Expr[FutureResult[ENTITY]] = {
    import c.universe._
    reify {
      config.splice.future {
        EntityManagerMacros.insert[ENTITY](c)(entity)(config).splice
      }
    }
  }
  def update[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entity: c.Expr[ENTITY])(config: c.Expr[AsyncConfig]): c.Expr[FutureResult[ENTITY]] = {
    import c.universe._
    reify {
      config.splice.future {
        EntityManagerMacros.update[ENTITY](c)(entity)(config).splice
      }
    }
  }
  def delete[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entity: c.Expr[ENTITY])(config: c.Expr[AsyncConfig]): c.Expr[FutureResult[ENTITY]] = {
    import c.universe._
    reify {
      config.splice.future {
        EntityManagerMacros.delete[ENTITY](c)(entity)(config).splice
      }
    }
  }

  def batchInsert[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entities: c.Expr[Iterable[ENTITY]])(config: c.Expr[AsyncConfig]): c.Expr[FutureBatchResult[ENTITY]] = {
    import c.universe._
    reify {
      config.splice.future {
        EntityManagerMacros.batchInsert[ENTITY](c)(entities)(config).splice
      }
    }
  }
  def batchUpdate[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entities: c.Expr[Iterable[ENTITY]])(config: c.Expr[AsyncConfig]): c.Expr[FutureBatchResult[ENTITY]] = {
    import c.universe._
    reify {
      config.splice.future {
        EntityManagerMacros.batchUpdate[ENTITY](c)(entities)(config).splice
      }
    }
  }
  def batchDelete[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entities: c.Expr[Iterable[ENTITY]])(config: c.Expr[AsyncConfig]): c.Expr[FutureBatchResult[ENTITY]] = {
    import c.universe._
    reify {
      config.splice.future {
        EntityManagerMacros.batchDelete[ENTITY](c)(entities)(config).splice
      }
    }
  }

}
