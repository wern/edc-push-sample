package org.eclipse.dataspaceconnector.apiwrapper;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.dataspaceconnector.apiwrapper.config.ApiWrapperConfig;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.model.ContractNegotiationDto;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.model.ContractOfferDescription;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.model.NegotiationInitiateRequestDto;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.service.ContractNegotiationService;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.service.ContractOfferService;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.service.HttpProxyService;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.service.TransferProcessService;
import org.eclipse.dataspaceconnector.apiwrapper.store.InMemoryContractAgreementStore;
import org.eclipse.dataspaceconnector.apiwrapper.store.InMemoryEndpointDataReferenceStore;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.AtomicConstraint;
import org.eclipse.dataspaceconnector.policy.model.LiteralExpression;
import org.eclipse.dataspaceconnector.policy.model.Operator;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Consumes()
@Produces({MediaType.APPLICATION_JSON})
@Path("/service")
public class ApiWrapperController {

    // Connection configurations
    private static final String IDS_PATH = "/api/v1/ids/data";
    private final String consumerConnectorUrl;
    private final String httpReceiverEndpoint;

    private final Monitor monitor;
    private final ContractOfferService contractOfferService;
    private final ContractNegotiationService contractNegotiationService;
    private final TransferProcessService transferProcessService;
    private final HttpProxyService httpProxyService;

    // In-memory stores
    private final InMemoryEndpointDataReferenceStore endpointDataReferenceStore;
    private final InMemoryContractAgreementStore contractAgreementStore;

    private Map<String, String> header;

    public ApiWrapperController(Monitor monitor,
                                ContractOfferService contractOfferService,
                                ContractNegotiationService contractNegotiationService,
                                TransferProcessService transferProcessService,
                                HttpProxyService httpProxyService,
                                InMemoryEndpointDataReferenceStore endpointDataReferenceStore,
                                InMemoryContractAgreementStore contractAgreementStore,
                                ApiWrapperConfig config) {
        this.monitor = monitor;
        this.contractOfferService = contractOfferService;
        this.contractNegotiationService = contractNegotiationService;
        this.transferProcessService = transferProcessService;
        this.httpProxyService = httpProxyService;
        this.endpointDataReferenceStore = endpointDataReferenceStore;
        this.contractAgreementStore = contractAgreementStore;

        this.consumerConnectorUrl = config.getConsumerEDCUrl();
        this.httpReceiverEndpoint = config.getReceiverHttpEndpoint();

        if (config.getConsumerEdcApiKeyValue() != null) {
            this.header = Collections.singletonMap(config.getConsumerEdcApiKeyName(), config.getConsumerEdcApiKeyValue());
        }
    }

    @GET
    @Path("/{assetId}/{subUrl:.+}")
    public String getWrapper(@QueryParam("provider-connector-url") String providerConnectorUrl, @PathParam("assetId") String assetId, @PathParam("subUrl") String subUrl, @Context UriInfo uriInfo) throws InterruptedException {
        monitor.debug("GET call on asset " +assetId + " with service call "+ subUrl);
        
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

        // Initialize and negotiate everything
        var dataReference = negotiateContractAndRetrieveEndpointDataReference(providerConnectorUrl, assetId);

        // Get data through data plane
        String data = "";
        try {
            data = httpProxyService.sendGETRequest(dataReference, subUrl, queryParams);
        } catch (IOException e) {
            monitor.severe("Call against consumer control plane failed!", e);
        }
        return data;
    }

    @POST
    @Path("/{assetId}/{subUrl:.+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String postWrapper(@QueryParam("provider-connector-url") String providerConnectorUrl, @PathParam("assetId") String assetId, @PathParam("subUrl") String subUrl, String body, @Context UriInfo uriInfo) throws InterruptedException {
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

        // Initialize and negotiate everything
        var dataReference = negotiateContractAndRetrieveEndpointDataReference(providerConnectorUrl, assetId);

        // Get data through data plane
        String data = "";
        try {
            data = httpProxyService.sendPOSTRequest(
                    dataReference,
                    subUrl,
                    queryParams,
                    body,
                    Objects.requireNonNull(okhttp3.MediaType.parse("application/json"))
            );
        } catch (IOException e) {
            monitor.severe("Call against consumer control plane failed!", e);
        }
        return data;
    }

    @PUT
    @Path("/{assetId}/{subUrl:.+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String putWrapper(@QueryParam("provider-connector-url") String providerConnectorUrl, @PathParam("assetId") String assetId, @PathParam("subUrl") String subUrl, String body, @Context UriInfo uriInfo) throws InterruptedException {
        monitor.debug("PUT call on asset " +assetId + " with service call "+ subUrl);

        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

        // Initialize and negotiate everything
        var dataReference = negotiateContractAndRetrieveEndpointDataReference(providerConnectorUrl, assetId);

        // Get data through data plane
        String data = "";
        try {
            data = httpProxyService.sendPUTRequest(
                    dataReference,
                    subUrl,
                    queryParams,
                    body,
                    Objects.requireNonNull(okhttp3.MediaType.parse("application/json"))
            );
        } catch (IOException e) {
            monitor.severe("Call against consumer control plane failed!", e);
        }
        return data;
    }

    private EndpointDataReference negotiateContractAndRetrieveEndpointDataReference(String providerConnectorUrl, String assetId) throws InterruptedException{
        // Initialize and negotiate everything
        var agreementId = initializeContractNegotiation(providerConnectorUrl, assetId);

        // Initiate transfer process
        transferProcessService.initiateHttpProxyTransferProcess(
                agreementId,
                assetId,
                consumerConnectorUrl,
                providerConnectorUrl + IDS_PATH,
                httpReceiverEndpoint,
                header
        );

        EndpointDataReference dataReference = null;
        for (int i=0; dataReference == null && i < 60; i++) {
            Thread.sleep(1000);
            dataReference = endpointDataReferenceStore.get(agreementId);
        }

        if(dataReference != null) {
            return dataReference;
        } else {
            // let's initiate a new negotiation (looks like agreement is outdated)
            contractAgreementStore.remove(assetId);
            return negotiateContractAndRetrieveEndpointDataReference(providerConnectorUrl, assetId);
        }
    }

    private String initializeContractNegotiation(String providerConnectorUrl, String assetId) throws InterruptedException {
        String agreementId = contractAgreementStore.get(assetId);

        if (agreementId != null) {
            monitor.debug("Found already existing contract agreement in cache");
            return agreementId;
        }

        monitor.info("Initialize contract negotiation");

        // Initiate negotiation
        var contractOffer = contractOfferService.findContractOffer4AssetId(
                assetId,
                consumerConnectorUrl,
                providerConnectorUrl + IDS_PATH,
                header
        );

        var validity = contractOffer.getContractEnd().toEpochSecond()-contractOffer.getContractStart().toEpochSecond();
        monitor.debug("### Offer Validity: "+ validity + "s");

        var patchedPolicy = Policy.Builder.newInstance()
                                //.id(/*contractOffer.getPolicy().getUid() */ null)
                                .permission(
                                    Permission.Builder.newInstance()
                                    .target(assetId)
                                    .action(Action.Builder.newInstance().type("USE").build())
                                //    .constraint(AtomicConstraint.Builder.newInstance()
                                //        .leftExpression(new LiteralExpression("idsc:PURPOSE"))
                                //        .operator(Operator.EQ)
                                //        .rightExpression(new LiteralExpression("ID 3.0 PCF"))
                                //        .build())
                                    .build()
                                ).build();

        var contractOfferDescription = new ContractOfferDescription(
            contractOffer.getId(),
            assetId,
            null,
            patchedPolicy
        );

        // Initiate negotiation
        var contractNegotiationRequest = NegotiationInitiateRequestDto.Builder.newInstance()
                .offerId(contractOfferDescription)
                .connectorId("provider")
                .connectorAddress(providerConnectorUrl + IDS_PATH)
                .protocol("ids-multipart")
                .build();
        var negotiationId = contractNegotiationService.initiateNegotiation(
                contractNegotiationRequest,
                consumerConnectorUrl,
                header,
                validity
        );

        // Check negotiation state
        ContractNegotiationDto negotiationResponse = null;

        while (negotiationResponse == null || !Objects.equals(negotiationResponse.getState(), "CONFIRMED")) {
            Thread.sleep(1000);

            try{
                negotiationResponse = contractNegotiationService.getNegotiationState(
                        negotiationId,
                        consumerConnectorUrl,
                        header
                );
            }catch (IOException ioe){
                // retry for now...
            }
        }

        agreementId = negotiationResponse.getContractAgreementId();
        contractAgreementStore.put(assetId, agreementId);

        return agreementId;
    }
}
