#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o pipefail
set -o nounset

# shellcheck disable=SC2164
cd edc
./gradlew publishToMavenLocal -x test

cd ../edc-controlplane-memory
./gradlew clean build

cd ../edc-dataplane
./gradlew clean build

cd ../publisher-bds
./gradlew clean build


cd ../receiver-bds
./gradlew clean build

cd ../frontend
npm install
npm run build