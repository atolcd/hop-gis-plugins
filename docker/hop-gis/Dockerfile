# --------------------
# Build artefacts - hop-gis-plugins
# --------------------
FROM maven:3-jdk-11 as builder
WORKDIR /app
USER root
COPY . /app
RUN mvn clean package

# --------------------
# Build Hop with gis plugins image
# --------------------
FROM apache/incubator-hop:0.99

WORKDIR /app
# Builder
COPY --from=builder /app/assemblies/target/gis-plugin-assemblies-*.zip /tmp/
RUN unzip /tmp/gis-plugin-assemblies-*.zip -d ${DEPLOYMENT_PATH}/hop/plugins/ &&\
  rm /tmp/gis-plugin-assemblies-*.zip
