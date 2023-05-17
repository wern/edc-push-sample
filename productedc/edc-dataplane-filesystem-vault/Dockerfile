#FROM openjdk:11-jre-slim-buster
FROM gcr.io/distroless/java11-debian11

#ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8090
ARG JAR
ARG LIB

WORKDIR /app

COPY ${JAR} edc.jar
COPY ${LIB} ./lib/

CMD ["-Dedc.fs.config=/app/configuration.properties", \
     "-Djava.util.logging.config.file=/app/logging.properties", \
     "-Djava.security.edg=file:/dev/.urandom", \
     "-jar", \
     "edc.jar"]
