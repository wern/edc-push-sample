package org.eclipse.dataspaceconnector.extensions.proxy.service;

public class HttpProxyConfig {

    private final String targetEndpointUrl;


    public HttpProxyConfig(String targetEndpointUrl){
        this.targetEndpointUrl = targetEndpointUrl;
    }

    public String getTargetEndpointUrl(){
        return targetEndpointUrl;
    }
    
}
