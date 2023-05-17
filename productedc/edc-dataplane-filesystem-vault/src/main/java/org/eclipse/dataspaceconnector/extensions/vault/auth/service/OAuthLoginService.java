package org.eclipse.dataspaceconnector.extensions.vault.auth.service;

import java.io.IOException;

import org.eclipse.dataspaceconnector.extensions.vault.auth.model.TokenResponse;
import org.eclipse.edc.spi.monitor.Monitor;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OAuthLoginService {

    private final Monitor monitor;
    private final OkHttpClient httpClient;

    public OAuthLoginService(Monitor monitor, OkHttpClient httpClient){
        this.monitor = monitor;
        this.httpClient = httpClient;
    }

    public String getToken(String idpUrl, String clientId, String clientSecret, String audience) throws IOException {
        TokenResponse tokenResponse = new ObjectMapper()
                                        .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
                                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                        .readValue(
                                            sendPOSTRequest(idpUrl, clientId, clientSecret, audience), 
                                            TokenResponse.class);
        return tokenResponse.access_token;
    }

    public String sendPOSTRequest(String url, String clientId, String clientSecret, String audience) throws IOException {
       
        RequestBody formBody = new FormBody.Builder()
                .addEncoded("grant_type", "client_credentials")
                .addEncoded("client_id", clientId)
                .addEncoded("client_secret", clientSecret)
                .addEncoded("audience", audience)
                .build();

        var request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build();
                
        return sendRequest(request);
    }

    private String sendRequest(Request request) throws IOException {
        var response = httpClient.newCall(request).execute();
        var body = response.body();

        if (!response.isSuccessful() || body == null) {
            monitor.warning(String.format("IDP responded with error: %s %s", response.code(), body != null ? body.string() : ""));
            return null;
        }

        var bodyString = body.string();
        return bodyString;
    }

}
