package coursier.m1tests

object Launcher {

  lazy val launcher = Option(System.getenv("COURSIER_M1_LAUNCHER")).getOrElse {
    sys.error("COURSIER_M1_LAUNCHER not set")
  }

}
