package coursier.m1tests

object LaunchTests extends coursier.clitests.LaunchTests {
  def launcher = Launcher.launcher
  def acceptsDOptions = false
  def acceptsJOptions = false
}
