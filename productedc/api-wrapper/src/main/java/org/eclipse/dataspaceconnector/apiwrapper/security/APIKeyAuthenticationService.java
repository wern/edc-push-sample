package org.eclipse.dataspaceconnector.apiwrapper.security;

import org.eclipse.dataspaceconnector.api.auth.AuthenticationService;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        var authHeader = map.entrySet()
                            .stream()
                            .filter(e -> e.getKey().equalsIgnoreCase(headerName))
                            .map(e -> e.getValue())
                            .findFirst();

        if (authHeader.orElse(Collections.EMPTY_LIST).isEmpty()) {
            monitor.debug("No authentication header specified");
            return false;
        }

        if (!authHeader.get().iterator().next().equals(apiKey)) {
            monitor.debug("Wrong API key provided");
            monitor.debug(authHeader.get().iterator().next() + "!=" + apiKey);
            return false;
        }
        
        return true;
    }
}
