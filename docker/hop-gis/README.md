# Apache Hop GIS Plugins - Docker

Execute the script with the right HOP version, from the root dir:
```sh
# Read Hop version in pom.xml
HOP_VERSION=$(grep -Po '<hop.version>\K[^<]+' pom.xml)
# HOP_VERSION="2.14.0"

docker/hop-gis/build.sh ${HOP_VERSION}
```

At the end, your image should be ready with the `latest` tag :
```sh
docker images --filter=reference='atolcd/hop*'
```