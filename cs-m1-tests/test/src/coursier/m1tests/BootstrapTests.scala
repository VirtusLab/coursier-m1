package coursier.m1tests

object BootstrapTests extends coursier.clitests.BootstrapTests {
  def launcher = Launcher.launcher

  override def enableNailgunTest = false
}
