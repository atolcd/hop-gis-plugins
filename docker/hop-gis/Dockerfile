ARG HOP_VERSION=2.14.0
# --------------------
# Build artefacts - hop-gis-plugins
# --------------------
FROM maven:3-eclipse-temurin-17 AS builder
WORKDIR /app
USER root
COPY . /app
RUN mvn clean package

# --------------------
# Build Hop with gis plugins image
# --------------------
FROM apache/hop:${HOP_VERSION}

WORKDIR /app
# Builder
COPY --from=builder /app/assemblies/target/gis-plugin-assemblies-*.zip /tmp/
RUN unzip /tmp/gis-plugin-assemblies-*.zip -d ${DEPLOYMENT_PATH}/plugins/ &&\
  rm /tmp/gis-plugin-assemblies-*.zip
