package coursier.m1tests

object BootstrapTests extends coursier.clitests.BootstrapTests {
  def launcher = Launcher.launcher
  def assembly = Launcher.launcher

  override def acceptsDOptions = false
  override def acceptsJOptions = false

  override def enableNailgunTest = false
}
