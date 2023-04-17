package org.eclipse.dataspaceconnector.extensions.proxy.service;

import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MultivaluedMap;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import org.eclipse.dataspaceconnector.spi.exception.AuthenticationFailedException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

public class HttpProxyService {
    public static class DataFixes {
        private final Monitor monitor;
        private final List<String> fixNotNullFields;

        public DataFixes(List<String> fixNotNullFields, Monitor monitor){
            this.fixNotNullFields = fixNotNullFields;
            this.monitor = monitor;
        }
       
        String fixData(String data) {
            String retValue = data;
            if(fixNotNullFields != null & !fixNotNullFields.isEmpty()) {
                monitor.debug("Data to fix:\n" + retValue);
                for(String fieldName : fixNotNullFields) {
                    monitor.debug("Fixing '"+ fieldName + "'..."); 
                    // This is really ugly and should be removed once versioning in test datamodel is fixed :( 
                    if(fieldName.equals("specVersion")){
                        retValue = retValue.replace("\""+ fieldName +"\":null", "\""+ fieldName +"\":\"1.0.0\"");
                    } else {
                        retValue = retValue.replace("\""+ fieldName +"\":null", "\""+ fieldName +"\":\"\"");
                    }
                }
                monitor.debug("Data after fix:\n" + retValue);
            } else {
                monitor.debug("Nothing to fix here ;)");
            }
            return retValue;
        }
    }

    private final Monitor monitor;
    private final OkHttpClient httpClient;
    private final DataFixes dataFixes;

    public HttpProxyService(Monitor monitor, OkHttpClient httpClient, DataFixes dataFixes) {
        this.monitor = monitor;
        this.httpClient = httpClient;
        this.dataFixes = dataFixes;
    }

    public HttpProxyService(Monitor monitor, OkHttpClient httpClient) {
        this(monitor, httpClient, null);
    }

    public String sendGETRequest(HttpProxyConfig proxyConfig, String subUrl, MultivaluedMap<String, String> headers, MultivaluedMap<String, String> parameters) throws IOException {
        var url = getUrl(proxyConfig.getTargetEndpointUrl(), subUrl, parameters);
        var builder = new Request.Builder().url(url);

        // need to remove original host header
        headers.remove("host");
        headers.forEach((h,vl) -> vl.forEach(v -> builder.addHeader(h, v)));

        return sendRequest(builder.build());
    }

    public String sendPOSTRequest(HttpProxyConfig proxyConfig, String subUrl, MultivaluedMap<String, String> headers, MultivaluedMap<String, String> parameters, String data, MediaType mediaType) throws IOException {
        var url = getUrl(proxyConfig.getTargetEndpointUrl(), subUrl, parameters);
        var builder = new Request.Builder().url(url);

        // need to remove original host header
        headers.remove("host");
        headers.forEach((h,vl) -> vl.forEach(v -> builder.addHeader(h, v)));

        return sendRequest(builder.post(RequestBody.create(data, mediaType)).build());
    }

    public String sendPUTRequest(HttpProxyConfig proxyConfig, String subUrl, MultivaluedMap<String, String> headers, MultivaluedMap<String, String> parameters, String data, MediaType mediaType) throws IOException {
        var url = getUrl(proxyConfig.getTargetEndpointUrl(), subUrl, parameters);
        var builder = new Request.Builder().url(url);

        // need to remove original host header
        headers.remove("host");
        headers.forEach((h,vl) -> vl.forEach(v -> builder.addHeader(h, v)));

        return sendRequest(builder.put(RequestBody.create(data, mediaType)).build());
    }

    private HttpUrl getUrl(String baseUrl, String subUrl, MultivaluedMap<String, String> parameters) {
        var url = baseUrl;

        if (subUrl != null && !subUrl.isEmpty()) {
            url = url + "/" + subUrl;
        }

        var httpBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();

        parameters.forEach((p,vl) -> vl.forEach(v -> httpBuilder.addQueryParameter(p, v)));

        return httpBuilder.build();
    }

    private String sendRequest(Request request) throws IOException {
        monitor.debug("ProxyService calling: " + request.url());
        var response = httpClient.newCall(request).execute();
        var body = response.body();

        if (!response.isSuccessful() || body == null) {
            monitor.warning(format("Endpoint responded with error: %s %s", response.code(), body != null ? body.string() : ""));
            switch(response.code()){
                case 401: 
                    throw new AuthenticationFailedException(body != null ? body.string() : "");
                case 403: 
                    throw new NotAuthorizedException(body != null ? body.string() : "");
                case 404: 
                    throw new NotFoundException(body != null ? body.string() : "");
                case 405: 
                    throw new NotAllowedException(body != null ? body.string() : "");
                default: 
                    throw new IllegalArgumentException(body != null ? body.string() : "");
            }
        }

        var bodyString = body.string();
        monitor.info("Endpoint responded correctly: " + bodyString);
        return bodyString;
    }
}
