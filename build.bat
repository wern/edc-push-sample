cd edc
call ./gradlew publishToMavenLocal -x test

cd ../edc-controlplane-memory
call ./gradlew clean build

cd ../edc-dataplane
call ./gradlew clean build

cd ../api-wrapper
call ./gradlew clean build

cd ../clients/pcf-demo-provider
call ./gradlew clean build

cd ../..‚‚‚