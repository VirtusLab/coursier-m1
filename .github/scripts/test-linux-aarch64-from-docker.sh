#!/usr/bin/env bash
set -e

apt-get update -q -y
apt-get install -q -y build-essential libz-dev zlib1g-dev git python3-pip curl

export PATH="$(pwd)/utils:$PATH"

eval "$(cs java --env --jvm temurin:21 --jvm-index https://github.com/coursier/jvm-index/raw/master/index.json)"

git config --global --add safe.directory "$(pwd)"

./mill -i cs-m1-tests.test
