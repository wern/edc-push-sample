#!/bin/bash

cd product-edc
./mvnw clean install -DskipTests

cd ../edc-controlplane-memory
./mvnw clean install 

cd ../edc-dataplane-filesystem-vault
./mvnw clean install 

cd ../api-wrapper
./gradlew clean build

cd ..