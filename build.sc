import $ivy.`io.github.alexarchambault.mill::mill-native-image::0.1.23`
import $ivy.`io.github.alexarchambault.mill::mill-native-image-upload:0.1.21`

import io.github.alexarchambault.millnativeimage.NativeImage
import io.github.alexarchambault.millnativeimage.upload.Upload
import mill._
import mill.scalalib._

def coursierVersion = "2.1.0-RC4"

object `cs-m1` extends JavaModule with NativeImage {
  def ivyDeps = super.ivyDeps() ++ Seq(
    ivy"io.get-coursier:coursier-cli_2.12:$coursierVersion"
  )

  def nativeImageGraalVmJvmId = T {
    sys.env.getOrElse("GRAALVM_ID", "graalvm-java17:22.1.0")
  }

  def nativeImageClassPath = runClasspath()
  def nativeImageMainClass = "coursier.cli.Coursier"
  def nativeImagePersist = System.getenv("CI") != null

  def copyToArtifacts(directory: String = "artifacts/") = T.command {
    val _ = Upload.copyLauncher(
      nativeImage().path,
      directory,
      "cs",
      compress = true,
      suffix = ""
    )
  }
}

object `cs-m1-tests` extends ScalaModule {
  def scalaVersion = "2.13.10"
  def ivyDeps = super.ivyDeps() ++ Seq(
    ivy"io.get-coursier:cli-tests_2.12:$coursierVersion"
  )
  object test extends Tests {
    def ivyDeps = super.ivyDeps() ++ Seq(
      ivy"com.lihaoyi::utest::0.8.1"
    )
    def testFramework = "utest.runner.Framework"
    def forkEnv = super.forkEnv() ++ Seq(
      "COURSIER_M1_LAUNCHER" -> `cs-m1`.nativeImage().path.toString
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

    Upload.upload("VirtusLab", "coursier-m1", ghToken, tag, dryRun = false, overwrite = true)(launchers: _*)
  }
}
