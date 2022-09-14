# edc-push-sample

## Preperation
You need to update the edc submodule before the build is started.

```shell
git submodule update --init
```

## Build

To build all components in one command, execute the following script.

```shell
./build.sh
```

## Configure DAPS

Add your DAPS configuration (local or central) to the controlplane config files.

```shell
./config/Consumer/docker/controlplane.configuration.properties
./config/Provider/docker/controlplane.configuration.properties
```

Add your own certificates (registered with DAPS) to the keystores and vaults.

```shell
./config/Consumer/docker/cert.pfx
./config/Consumer/docker/vault.properties

./config/Provider/docker/cert.pfx
./config/Provider/docker/vault.properties
```

## Run it without using DAPS (local testing only!)
Want to use mock-IAM only and git rid of all the DAPS configuration stuff? Just checkout the corresponding version using:
```shell
git checkout df8395ee
```

## Run self-build multiple docker container setup 

```shell
docker-compose -f docker-compose-buildit.yml up --build
```