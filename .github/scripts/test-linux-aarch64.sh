#!/usr/bin/env bash
set -euv

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)"

cp "$DIR/test-linux-aarch64-from-docker.sh" utils/

docker run $(if test -t 1; then echo "-it"; fi) --rm \
  --volume "$(pwd):/data" \
  -w /data \
  -e "CI=$CI" \
  ubuntu:20.04 \
    /data/utils/test-linux-aarch64-from-docker.sh
