export EDC_VAULT=../../../../config/Consumer/local/vault.properties
export EDC_KEYSTORE=../../../../certs/cert.pfx
export EDC_KEYSTORE_PASSWORD=123456

$JAVA_HOME/bin/java -Dedc.fs.config=../../../../config/Consumer/local/dataplane.configuration.properties -Djava.util.logging.config.file=../../../../config/Consumer/local/logging.properties -Djava.security.edg=file:/dev/urandom -jar ../../../../edc-dataplane/build/libs/edc.jar