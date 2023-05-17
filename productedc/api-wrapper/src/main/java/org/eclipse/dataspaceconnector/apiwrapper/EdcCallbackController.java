package org.eclipse.dataspaceconnector.apiwrapper;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.dataspaceconnector.apiwrapper.store.InMemoryEndpointDataReferenceStore;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;

@Consumes({MediaType.APPLICATION_JSON})
@Path("/endpoint-data-reference")
public class EdcCallbackController {

    private final Monitor monitor;
    private final InMemoryEndpointDataReferenceStore endpointDataReferenceStore;

    public EdcCallbackController(Monitor monitor, InMemoryEndpointDataReferenceStore endpointDataReferenceStore) {
        this.monitor = monitor;
        this.endpointDataReferenceStore = endpointDataReferenceStore;
    }

    @POST
    public void receiveEdcCallback(EndpointDataReference dataReference) {
        String contractId = dataReference.getProperties().get("cid" /* DataPlaneTransferConstants.CONTRACT_ID */ );
        endpointDataReferenceStore.put(contractId, dataReference);
        monitor.debug("Endpoint Data Reference received and stored for agreement: " + contractId);
        monitor.debug("Using endpoint: " + dataReference.getEndpoint());
        monitor.debug("Using AuthKey:" + dataReference.getAuthKey());
        monitor.debug("Using AuthCode:" + dataReference.getAuthCode());
    }
}
