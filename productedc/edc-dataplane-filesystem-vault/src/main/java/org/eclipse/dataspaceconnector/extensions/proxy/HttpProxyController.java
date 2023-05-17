package org.eclipse.dataspaceconnector.extensions.proxy;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.dataspaceconnector.extensions.proxy.service.HttpProxyConfig;
import org.eclipse.dataspaceconnector.extensions.proxy.service.HttpProxyService;
import org.eclipse.edc.spi.monitor.Monitor;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Consumes()
@Produces({MediaType.APPLICATION_JSON})
@Path("/")
public class HttpProxyController {
    private final Monitor monitor;
    private final HttpProxyService httpProxyService;
    private final HttpProxyConfig config;

    public HttpProxyController(Monitor monitor,
                                HttpProxyService httpProxyService,
                                HttpProxyConfig config) {
        this.monitor = monitor;
        this.httpProxyService = httpProxyService;
        this.config= config;
    }

    @GET
    @Path("/{subUrl:.+}")
    public String proxyGet(@PathParam("subUrl") String subUrl, @Context UriInfo uriInfo, @Context HttpHeaders headers) throws IOException {
         monitor.debug("GET call for sub URL " + subUrl 
         + "\n with parameters " + uriInfo.getQueryParameters() 
         + "\n with headers "+ headers.getRequestHeaders());

        monitor.debug("Calling endpoint: "+ config.getTargetEndpointUrl());
        
        return httpProxyService.sendGETRequest(config, subUrl, headers.getRequestHeaders(), uriInfo.getQueryParameters() );
    }

    @POST
    @Path("/{subUrl:.+}")
    public String proxyPost(@PathParam("subUrl") String subUrl, String body, @Context UriInfo uriInfo, @Context HttpHeaders headers) throws IOException {
        monitor.debug("POST call for sub URL " + subUrl 
        + "\n with parameters " + uriInfo.getQueryParameters() 
        + "\n with headers "+ headers.getRequestHeaders()
        + "\n with body "+ body);

        monitor.debug("Calling endpoint: "+ config.getTargetEndpointUrl());
        
        return httpProxyService.sendPOSTRequest(config, subUrl, headers.getRequestHeaders(), uriInfo.getQueryParameters(), fixBody(body), okhttp3.MediaType.parse(MediaType.APPLICATION_JSON));
    }

    @PUT
    @Path("/{subUrl:.+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String proxyPut(@PathParam("subUrl") String subUrl, String body, @Context UriInfo uriInfo, @Context HttpHeaders headers) throws IOException {
         monitor.debug("POST call for sub URL " + subUrl
         + "\n with parameters " + uriInfo.getQueryParameters() 
         + "\n with headers "+ headers.getRequestHeaders()
        + "\n with body "+ body);

        monitor.debug("Calling endpoint: "+ config.getTargetEndpointUrl()
        + "with body: " + fixBody(body));

        return httpProxyService.sendPUTRequest(config, subUrl, headers.getRequestHeaders(), uriInfo.getQueryParameters(), fixBody(body), okhttp3.MediaType.parse(MediaType.APPLICATION_JSON));
    }

    private String fixBody(String body){
        if(body.contains("biogenicEmissions")){
            return fixTimeStamps(body);
        } else {
            return fixBiogenicEmissionsNotOptional(fixTimeStamps(body));
        }
    }

    private String fixTimeStamps(String body){
        return body.replaceAll("(?<=\\:\\d\\d)Z\"", "\\.000Z\"");
    }

    private String fixBiogenicEmissionsNotOptional(String body){
        return body.replaceAll("(?=\"primaryDataShare\"\\:)", "\"biogenicEmissions\": {\"otherEmissions\": \"0\",\"landUseEmissions\": \"0\"},");
    }

}
