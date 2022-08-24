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

## Run self-build multiple docker container setup 

```shell
docker-compose -f docker-compose-buildit.yml up --build
```