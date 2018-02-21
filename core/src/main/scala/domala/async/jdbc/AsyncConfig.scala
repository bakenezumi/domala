package domala.async.jdbc

import domala.async.AsyncContext
import domala.jdbc.Config

/** A runtime configuration for asynchronously DAOs.
  *
  * The implementation must be thread safe.
  *
  */
trait AsyncConfig extends Config with AsyncContext
