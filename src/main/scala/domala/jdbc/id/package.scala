package domala.jdbc

import org.seasar.doma

package object id {
  // Alias of Doma type
  type BuiltinSequenceIdGenerator = doma.jdbc.id.BuiltinSequenceIdGenerator
  type BuiltinTableIdGenerator = doma.jdbc.id.BuiltinTableIdGenerator
  type SequenceIdGenerator = doma.jdbc.id.SequenceIdGenerator
  type TableIdGenerator = doma.jdbc.id.TableIdGenerator

}
