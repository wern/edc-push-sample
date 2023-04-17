package org.eclipse.dataspaceconnector.apiwrapper.store;

import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.model.EndpointDataReferenceWithTimeout;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class InMemoryEndpointDataReferenceStore {
    private final Map<String, EndpointDataReferenceWithTimeout> store = new HashMap<>();
    private final int timeoutInMinutes;
    private final Monitor monitor;

    public InMemoryEndpointDataReferenceStore(Monitor monitor, int timeoutInMins){
        this.monitor = monitor;
        this.timeoutInMinutes = timeoutInMins;
    }

    public void put(String agreementId, EndpointDataReference endpointDataReference) {
        monitor.debug("Storing reference for "+ timeoutInMinutes + " minutes.");
        store.put(agreementId, new EndpointDataReferenceWithTimeout(endpointDataReference, timeoutInMinutes, ChronoUnit.MINUTES));
    }

    public EndpointDataReference get(String agreementId) {
        return store.get(agreementId)!=null?store.get(agreementId).toEndpointDataReference():null;
    }
}
