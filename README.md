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

## Run self-build multiple docker container setup 

```shell
docker-compose -f docker-compose-buildit.yml up --build
```