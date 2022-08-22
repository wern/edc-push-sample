package org.eclipse.dataspaceconnector.apiwrapper.security;

import org.eclipse.dataspaceconnector.api.auth.AuthenticationService;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

import java.util.List;
import java.util.Map;

public class APIKeyAuthenticationService implements AuthenticationService {

    private final Monitor monitor;
    private final String headerName;
    private final String apiKey;

    public APIKeyAuthenticationService(Monitor monitor, String headerName, String apiKey) {
        this.monitor = monitor;
        this.headerName = headerName;
        this.apiKey= apiKey;
    }

    @Override
    public boolean isAuthenticated(Map<String, List<String>> map) {
        var authHeader = map.get(headerName);

        if (authHeader == null || authHeader.isEmpty()) {
            monitor.debug("No authentication header specified");
            return false;
        }

        if (!authHeader.iterator().next().equals(apiKey)) {
            monitor.debug("Wrong API key provided");
            return false;
        }
        
        return true;
    }
}
