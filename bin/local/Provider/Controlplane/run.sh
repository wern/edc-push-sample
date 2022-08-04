export EDC_VAULT=../../../../config/Provider/local/vault.properties
export EDC_KEYSTORE=../../../../certs/cert.pfx
export EDC_KEYSTORE_PASSWORD=123456

$JAVA_HOME/bin/java -Dedc.fs.config=../../../../config/Provider/local/controlplane.configuration.properties -Djava.util.logging.config.file=../../../../config/Provider/local/logging.properties -Djava.security.edg=file:/dev/urandom -jar ../../../../edc-controlplane-memory/build/libs/edc.jar