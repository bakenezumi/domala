package domala.tests.function

import domala.jdbc.Config
import domala.tests.H2TestConfigTemplate

object AsyncFunctionTestConfigs {
  private[this] val configs = (0 to 5).map {i =>
    //noinspection SpellCheckingInspection
    new H2TestConfigTemplate("asyncfnctest" + i){}
    //new H2TestConfigTemplate("asyncfnctest" + i + ";TRACE_LEVEL_SYSTEM_OUT=4"){}
  }

  def get(i: Int): Config = configs(i)
}
