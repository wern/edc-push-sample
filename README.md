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

There is no API Wrapper incl. yet.

```shell
docker-compose up --build
```