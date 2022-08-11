package org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.InternalServerErrorException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.model.ContractNegotiationDto;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.model.NegotiationId;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.model.NegotiationInitiateRequestDto;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.model.NegotiationStatusResponse;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.Utility;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.TypeManager;

import java.util.Map;

import static java.lang.String.format;

import java.io.IOException;

public class ContractNegotiationService {
    private final Monitor monitor;
    private final TypeManager typeManager;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String NEGOTIATION_PATH = "/contractnegotiations";
    private static final String NEGOTIATION_STATE_PATH_PATTERN = NEGOTIATION_PATH + "/%s";

    public ContractNegotiationService(Monitor monitor, TypeManager typeManager, OkHttpClient httpClient) {
        this.monitor = monitor;
        this.typeManager = typeManager;
        this.objectMapper = typeManager.getMapper();
        this.httpClient = httpClient;
    }

    public String initiateNegotiation(NegotiationInitiateRequestDto contractOfferRequest, String connectorControlPlaneBaseUrl, Map<String, String> headers) {
        var url = connectorControlPlaneBaseUrl + NEGOTIATION_PATH;
        var requestBody = RequestBody.create(
                typeManager.writeValueAsString(contractOfferRequest),
                Utility.JSON
        );

        var request = new Request.Builder()
                .url(url)
                .post(requestBody);
        headers.forEach(request::addHeader);

        monitor.debug("initateNegotiation at " + url);
        try{
            final Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            monitor.debug("negotiating offer: " + buffer.readUtf8());
        }catch(IOException ioe){
            //ignored
        }
        try (var response = httpClient.newCall(request.build()).execute()) {
            var body = response.body();

            if (!response.isSuccessful() || body == null) {
                monitor.warning(format("Control plane responded with error: %s %s", response.code(), body != null ? body.string() : ""));
                return null;
            }

            var negotiationId = objectMapper.readValue(body.string(), NegotiationId.class);
            monitor.info("Started negotiation with ID: " + negotiationId.getId());

            return negotiationId.getId();
        } catch (Exception e) {
            monitor.severe(format("Error in calling the Control plane at %s", url), e);
        }

        return null;
    }

    public NegotiationStatusResponse getNegotiationStateBroken(String negotiationId, String connectorControlPlaneBaseUrl, Map<String, String> headers) {
        var negotiationStateUrlPattern = connectorControlPlaneBaseUrl + NEGOTIATION_STATE_PATH_PATTERN;
        var url = format(negotiationStateUrlPattern, negotiationId);
        var request = new Request.Builder()
                .url(url);
        headers.forEach(request::addHeader);

        monitor.debug("getNegotiationState at " + url);

        try (var response = httpClient.newCall(request.build()).execute()) {
            var body = response.body();

            if (!response.isSuccessful() || body == null) {
                monitor.warning(format("Control plane responded with error: %s %s", response.code(), body != null ? body.string() : ""));
                return null;
            }

            var negotiationStatus = objectMapper.readValue(body.string(), NegotiationStatusResponse.class);
            monitor.info(format("Negotiation %s is in state '%s' (agreementId: %s)", negotiationId, negotiationStatus.getStatus(), negotiationStatus.getContractAgreementId()));

            return negotiationStatus;
        } catch (Exception e) {
            monitor.severe(format("Error in calling the Control plane at %s", url), e);
        }

        return null;
    }

    public ContractNegotiationDto getNegotiationState(String negotiationId, String connectorEdcDataManagementUrl, Map<String, String> headers) throws IOException {
        var url = format("%s/%s", connectorEdcDataManagementUrl + NEGOTIATION_PATH, negotiationId);
        var request = new Request.Builder()
                .url(url);
        headers.forEach(request::addHeader);

        try (var response = httpClient.newCall(request.build()).execute()) {
            var body = response.body();

            if (!response.isSuccessful() || body == null) {
                throw new InternalServerErrorException(format("Control plane responded with: %s %s", response.code(), body != null ? body.string() : ""));
            }

            var negotiation = objectMapper.readValue(body.string(), ContractNegotiationDto.class);
            monitor.info(format("Negotiation %s is in state '%s' (agreementId: %s)", negotiationId, negotiation.getState(), negotiation.getContractAgreementId()));

            return negotiation;
        } catch (Exception e) {
            monitor.severe(format("Error in calling the Control plane at %s", url), e);
            throw e;
        }
    }
}
