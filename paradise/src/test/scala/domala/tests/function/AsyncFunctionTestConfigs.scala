package domala.tests.function

import domala.jdbc.Config
import domala.tests.H2TestConfigTemplate

object AsyncFunctionTestConfigs {
  private[this] val configs = (0 to 5).map {i =>
    new H2TestConfigTemplate("async_function_test" + i){}
    //new H2TestConfigTemplate("async_function_test" + i + ";TRACE_LEVEL_SYSTEM_OUT=4"){}
  }

  def get(i: Int): Config = configs(i)
}
