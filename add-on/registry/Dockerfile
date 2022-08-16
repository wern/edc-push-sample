#
# Copyright (c) 2021-2022 T-Systems International GmbH (Catena-X Consortium)
#
# See the AUTHORS file(s) distributed with this work for additional
# information regarding authorship.
#
# See the LICENSE file(s) distributed with this work for
# additional information regarding license terms.
#

# Docker buildfile to containerize the semantics layer
FROM gcr.io/distroless/java11-debian11

WORKDIR /app

COPY ./target/registry*.jar app.jar

ENV JAVA_TOOL_OPTIONS "-Xms512m -Xmx2048m"
EXPOSE 4243

ENTRYPOINT [ "java","-Dspring.profiles.active=local","-Dserver.ssl.enabled=false","-jar","/app/app.jar" ]
