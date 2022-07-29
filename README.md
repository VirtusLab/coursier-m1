*coursier-m1*

Apple M1 launchers of [coursier](https://github.com/coursier/coursier)

Find the launchers as assets of the [releases](https://github.com/VirtusLab/coursier-m1/releases).

This repository builds coursier launchers for macOS, both for
Intel processors (as a sanity check of the build here), and for
M1.

The generated launchers are tested just like those of the main coursier
repository, via the
[`cli-tests`](https://github.com/coursier/coursier/tree/bd1c50cf9957e5fc747c69d0c085181d3c9f7a37/modules/cli-tests/src)
module of the main coursier repository, that is published to Maven
Central, is pulled in this repo, and used to test the launchers here.
