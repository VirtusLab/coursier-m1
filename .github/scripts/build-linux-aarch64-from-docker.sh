#!/usr/bin/env bash
set -e

apt-get update -q -y
apt-get install -q -y build-essential libz-dev zlib1g-dev git python3-pip curl zip

export PATH="$(pwd)/utils:$PATH"

eval "$(cs java --env --jvm temurin:17 --jvm-index https://github.com/coursier/jvm-index/raw/master/index.json)"

git config --global --add safe.directory "$(pwd)"

./mill -i "cs-m1.writeNativeImageScript" generate.sh ""
./generate.sh
./mill -i "cs-m1.copyToArtifacts" ./artifacts
.github/scripts/generate-os-packages.sh
