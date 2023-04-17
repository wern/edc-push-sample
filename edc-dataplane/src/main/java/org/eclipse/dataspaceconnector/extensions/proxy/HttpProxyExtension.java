package org.eclipse.dataspaceconnector.extensions.proxy;

import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.core.Response.Status;

import java.util.Map;

import org.eclipse.dataspaceconnector.extensions.proxy.service.HttpProxyConfig;
import org.eclipse.dataspaceconnector.extensions.proxy.service.HttpProxyService;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.BaseExtension;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Inject;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Requires;
import org.eclipse.dataspaceconnector.spi.WebService;
import org.eclipse.dataspaceconnector.spi.exception.AuthenticationFailedException;
import org.eclipse.dataspaceconnector.spi.exception.ObjectExistsException;
import org.eclipse.dataspaceconnector.spi.exception.ObjectNotFoundException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import jakarta.ws.rs.NotAuthorizedException;
import okhttp3.OkHttpClient;

public class HttpProxyExtension implements ServiceExtension {

    private static final String PROXY_TARGET_URL = "pcf.asset.proxy.target.url";
    private final static String PROXY_CONTEXT_ALIAS = "proxy";

    private final Map<Class<? extends Throwable>, Response.Status> exceptionMapping = Map.of(
            IllegalArgumentException.class, Status.BAD_REQUEST,
            NullPointerException.class, Status.BAD_REQUEST,
            AuthenticationFailedException.class, Status.UNAUTHORIZED,
            NotAuthorizedException.class, Status.FORBIDDEN,
            ObjectNotFoundException.class, Status.NOT_FOUND,
            NotFoundException.class, Status.NOT_FOUND,
            NotAllowedException.class, Status.METHOD_NOT_ALLOWED,
            ObjectExistsException.class, Status.CONFLICT,
           // ObjectNotModifiableException.class, 423,
            UnsupportedOperationException.class, Status.NOT_IMPLEMENTED
    );

    public class HttpProxyExceptionMapper implements ExceptionMapper<RuntimeException> {
        private final Map<Class<? extends Throwable>, Response.Status> exceptionMap;
    
        public HttpProxyExceptionMapper(Map<Class<? extends Throwable>, Response.Status> exceptionMap) {
            this. exceptionMap = exceptionMap;
        }
    
        @Override
        public Response toResponse(RuntimeException exception) {
            var status = exceptionMap.getOrDefault(exception.getClass(), Status.INTERNAL_SERVER_ERROR);
    
            var errorDetails = exception.getMessage();
    
            return Response.status(status)
                    .entity(errorDetails)
                    .build();
        }
    }

    @Inject
    private WebService webService;
    @Inject
    private OkHttpClient httpClient;

    @Override
    public String name() {
        return "Endpoint Proxy Ext.";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        Monitor monitor = context.getMonitor();
        String targetEndpointURL = context.getSetting(PROXY_TARGET_URL, null);
        
        // Map exceptions to proper status codes
        webService.registerResource(new HttpProxyExceptionMapper(exceptionMapping));
        webService.registerResource(PROXY_CONTEXT_ALIAS, new HttpProxyExceptionMapper(exceptionMapping));

        // Register ProxyController
        webService.registerResource(PROXY_CONTEXT_ALIAS, new HttpProxyController(monitor, new HttpProxyService(monitor, httpClient), new HttpProxyConfig(targetEndpointURL)));
    }
    
}
