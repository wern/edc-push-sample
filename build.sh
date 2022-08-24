#!/bin/bash

cd edc
./gradlew publishToMavenLocal -x test

cd ../edc-controlplane-memory
./gradlew clean build

cd ../edc-dataplane
./gradlew clean build

cd ../api-wrapper
./gradlew clean build

cd ../clients/pcf-demo-provider
./gradlew clean build