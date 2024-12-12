import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.4.0`
import $ivy.`io.github.alexarchambault.mill::mill-native-image::0.1.29`
import $ivy.`io.github.alexarchambault.mill::mill-native-image-upload:0.1.24`

import de.tobiasroeser.mill.vcs.version._
import io.github.alexarchambault.millnativeimage.NativeImage
import io.github.alexarchambault.millnativeimage.upload.Upload
import mill._
import mill.scalalib._

import scala.util.Properties

def scalaDefaultVersion = "2.13.15"
def coursierVersion     = "2.1.21"
def graalVmVersion      = "22.1.0"
def utestVersion        = "0.8.4"

object `cs-m1` extends JavaModule with NativeImage {
  def ivyDeps = super.ivyDeps() ++ Seq(
    ivy"io.get-coursier:coursier-cli_2.13:$coursierVersion"
  )

  def nativeImageGraalVmJvmId = T {
    sys.env.getOrElse("GRAALVM_ID", s"graalvm-java17:$graalVmVersion")
  }

  def nativeImageClassPath = runClasspath()
  def nativeImageMainClass = "coursier.cli.Coursier"
  def nativeImagePersist   = System.getenv("CI") != null

  def nativeImageOptions = T {
    if (Properties.isLinux)
      Seq(
        // required on the Linux / ARM64 CI in particular (not sure why)
        "-Djdk.lang.Process.launchMechanism=vfork", // https://mbien.dev/blog/entry/custom-java-runtimes-with-jlink
        "-H:PageSize=65536" // Make sure binary runs on kernels with page size set to 4k, 16 and 64k
      )
    else
      Nil
  }

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
  def scalaVersion = scalaDefaultVersion
  def ivyDeps = super.ivyDeps() ++ Seq(
    ivy"io.get-coursier:cli-tests_2.13:$coursierVersion"
  )
  object test extends Tests {
    def ivyDeps = super.ivyDeps() ++ Seq(
      ivy"com.lihaoyi::utest::$utestVersion"
    )
    def testFramework = "utest.runner.Framework"
    def forkEnv = super.forkEnv() ++ Seq(
      "COURSIER_M1_LAUNCHER" -> `cs-m1`.nativeImage().path.toString
    )
  }
}

object ci extends Module {
  def upload(directory: String = "artifacts/") = T.command {
    val version = tag()

    val path = os.Path(directory, os.pwd)
    val launchers = os.list(path).filter(os.isFile(_)).map { path =>
      path -> path.last
    }
    val ghToken = Option(System.getenv("UPLOAD_GH_TOKEN")).getOrElse {
      sys.error("UPLOAD_GH_TOKEN not set")
    }
    val (tag0, overwrite) =
      if (version.endsWith("-SNAPSHOT")) ("nightly", true)
      else ("v" + version, false)

    Upload.upload(
      "VirtusLab",
      "coursier-m1",
      ghToken,
      tag0,
      dryRun = false,
      overwrite = overwrite
    )(launchers: _*)
  }

  private def computePublishVersion(state: VcsState): String =
    if (state.commitsSinceLastTag > 0) {
      val versionOrEmpty = state.lastTag
        .filter(_ != "latest")
        .filter(_ != "nightly")
        .map(_.stripPrefix("v"))
        .flatMap { tag =>
          val baseVersion = tag.takeWhile(c => c == '.' || c.isDigit)
          if (
            baseVersion == tag || tag
              .stripPrefix(baseVersion)
              .forall(c => c == '-' || c.isDigit)
          ) {
            val idx = baseVersion.lastIndexOf(".")
            if (idx >= 0)
              Some(
                baseVersion.take(idx + 1) + (baseVersion
                  .drop(idx + 1)
                  .toInt + 1).toString + "-SNAPSHOT"
              )
            else
              None
          }
          else
            Some(baseVersion + "-SNAPSHOT")
        }
        .getOrElse("0.0.1-SNAPSHOT")
      Some(versionOrEmpty)
        .filter(_.nonEmpty)
        .getOrElse(state.format())
    }
    else
      state.lastTag
        .getOrElse(state.format())
        .stripPrefix("v")

  def tag = T {
    val state = VcsVersion.vcsState()
    computePublishVersion(state)
  }
}
def copyTo(task: mill.main.Tasks[PathRef], dest: os.Path) = T.command {
  if (task.value.length > 1)
    sys.error("Expected a single task")
  val ref = task.value.head()
  os.makeDir.all(dest / os.up)
  os.copy.over(ref.path, dest)
}
