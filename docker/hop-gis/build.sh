#! /bin/sh

readonly ROOT_DIR=$(realpath "$(dirname "$0")/../..")
cd $ROOT_DIR

HOP_VERSION=$1

# Build artefacts and runnable image
docker build . -f docker/hop-gis/Dockerfile \
  --build-arg="HOP_VERSION=$HOP_VERSION" \
  --network=host --force-rm \
  -t atolcd/hop-gis \
  \
  || { echo 'Image build failed' ; exit 1; }
