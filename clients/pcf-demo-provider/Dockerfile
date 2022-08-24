FROM gcr.io/distroless/java11-debian11

ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8090
ARG JAR

WORKDIR /app

COPY build/libs/pcf-demo-provider-0.0.1-SNAPSHOT.jar app.jar

CMD ["-Djava.util.logging.config.file=/app/logging.properties", \
     "-Djava.security.edg=file:/dev/.urandom", \
     "-Dapi.wrapper.url=http://provider-apiwrapper:3131/api/service/194f0160-cef1-4cb3-a6f1-1d917b4e39c0-fec89cf0-9382-4d68-8cea-dae797b7b8ae/productIds/$productId?BPN=$BPN&requestId=$requestId&provider-connector-url=http://consumer-controlplane:9193", \
     "-jar", \
     "app.jar"]