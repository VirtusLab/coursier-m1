import $ivy.`io.github.alexarchambault.mill::mill-native-image::0.1.21`
import $ivy.`io.github.alexarchambault.mill::mill-native-image-upload:0.1.21`

import io.github.alexarchambault.millnativeimage.NativeImage
import io.github.alexarchambault.millnativeimage.upload.Upload
import mill._
import mill.scalalib._

def coursierVersion = "2.1.0-M6-28-gbad85693f"

object `cs-m1` extends JavaModule with NativeImage {
  def ivyDeps = super.ivyDeps() ++ Seq(
    ivy"io.get-coursier:coursier-cli_2.12:$coursierVersion"
  )

  def nativeImageClassPath = runClasspath()
  def nativeImageMainClass = "coursier.cli.Coursier"
}

object `cs-m1-tests` extends ScalaModule {
  def scalaVersion = "2.12.16"
  def ivyDeps = super.ivyDeps() ++ Seq(
    ivy"io.get-coursier:cli-tests_2.12:2.1.0-M6-28-gbad85693f-12-c7620d19f-SNAPSHOT"
  )
  object test extends Tests {
    def ivyDeps = super.ivyDeps() ++ Seq(
      ivy"com.lihaoyi::utest::0.8.0"
    )
    def testFramework = "utest.runner.Framework"
    def forkEnv = super.forkEnv() ++ Seq(
      "COURSIER_M1_LAUNCHER" -> os.proc("cs", "get", "--archive", "https://github.com/coursier/coursier/releases/download/v2.1.0-M6-28-gbad85693f/cs-x86_64-apple-darwin.gz").call().out.trim()
    )
  }
}

object ci extends Module {
  def upload(directory: String = "artifacts/") = T.command {
    val version = coursierVersion

    val path = os.Path(directory, os.pwd)
    val launchers = os.list(path).filter(os.isFile(_)).map { path =>
      path -> path.last
    }
    val ghToken = Option(System.getenv("UPLOAD_GH_TOKEN")).getOrElse {
      sys.error("UPLOAD_GH_TOKEN not set")
    }
    val tag = "v" + version

    Upload.upload("scala-cli", "java-class-name", ghToken, tag, dryRun = false, overwrite = true)(launchers: _*)
  }
}
