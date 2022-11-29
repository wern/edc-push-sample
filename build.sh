#!/bin/bash

cd ./GradlePlugins
./gradlew clean publishToMavenLocal -x test -x javadoc -Pskip.signing=true

cd ../edc
./gradlew clean publishToMavenLocal -x test -x javadoc -Pskip.signing=true

cd ../edc-controlplane-memory
./gradlew clean build

cd ../edc-dataplane
./gradlew clean build

cd ../api-wrapper
./gradlew clean build

cd ../clients/pcf-demo-provider
./gradlew clean build