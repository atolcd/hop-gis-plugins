#! /bin/sh

readonly ROOT_DIR=$(realpath "$(dirname "$0")/../..")
cd $ROOT_DIR

# Build artefacts and runnable image
docker build . -f docker/hop-gis/Dockerfile \
  --network=host --force-rm \
  -t atolcd/hop-gis \
  \
  || { echo 'Image build failed' ; exit 1; }
