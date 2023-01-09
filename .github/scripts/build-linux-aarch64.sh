#!/usr/bin/env bash
set -euv

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)"

mkdir -p artifacts
mkdir -p utils
cp "$(cs get https://github.com/coursier/coursier/releases/download/v2.0.16/cs-aarch64-pc-linux)" utils/cs
chmod +x utils/cs

cp "$DIR/build-linux-aarch64-from-docker.sh" utils/

docker run $(if test -t 1; then echo "-it"; fi) --rm \
  --volume "$(pwd):/data" \
  -w /data \
  -e "CI=$CI" \
  ubuntu:20.04 \
    /data/utils/build-linux-aarch64-from-docker.sh
