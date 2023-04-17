package org.eclipse.dataspaceconnector.apiwrapper;

import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.core.Response.Status;
import okhttp3.OkHttpClient;
import org.eclipse.dataspaceconnector.api.auth.AuthenticationRequestFilter;
import org.eclipse.dataspaceconnector.spi.exception.*;
import org.eclipse.dataspaceconnector.apiwrapper.config.ApiWrapperConfig;
import org.eclipse.dataspaceconnector.apiwrapper.config.ApiWrapperConfigKeys;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.service.ContractNegotiationService;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.service.ContractOfferService;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.service.HttpProxyService;
import org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.service.TransferProcessService;
import org.eclipse.dataspaceconnector.apiwrapper.security.APIKeyAuthenticationService;
import org.eclipse.dataspaceconnector.apiwrapper.security.BasicAuthenticationService;
import org.eclipse.dataspaceconnector.apiwrapper.store.InMemoryContractAgreementStore;
import org.eclipse.dataspaceconnector.apiwrapper.store.InMemoryEndpointDataReferenceStore;
import org.eclipse.dataspaceconnector.spi.ApiErrorDetail;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.WebService;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.system.configuration.Config;

import java.util.Map;
import java.util.stream.Collectors;

public class ApiWrapperExtension implements ServiceExtension {

    public class EdcApiExceptionMapper implements ExceptionMapper<EdcApiException> {
        private final Map<Class<? extends Throwable>, Response.Status> exceptionMap;
    
        public EdcApiExceptionMapper(Map<Class<? extends Throwable>, Response.Status> exceptionMap) {
            this. exceptionMap = exceptionMap;
        }
    
        @Override
        public Response toResponse(EdcApiException exception) {
            var status = exceptionMap.getOrDefault(exception.getClass(), Status.INTERNAL_SERVER_ERROR);
    
            var errorDetails = exception.getMessages().stream()
                    .map(message -> ApiErrorDetail.Builder.newInstance()
                            .message(message)
                            .type(exception.getType())
                            .build()
                    )
                    .collect(Collectors.toList());
    
            return Response.status(status)
                    .entity(errorDetails)
                    .build();
        }
    }

    private static final String DEFAULT_CONTEXT_ALIAS = "default";
    private static final String CALLBACK_CONTEXT_ALIAS = "callback";

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

    @Inject
    private WebService webService;

    @Inject
    private OkHttpClient httpClient;

    @Override
    public String name() {
        return "PCF-API-Wrapper";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var config = createApiWrapperConfig(context.getConfig());
        var monitor = context.getMonitor();
        var typeManager = context.getTypeManager();

        // Map exceptions to proper status codes
        webService.registerResource(new EdcApiExceptionMapper(exceptionMapping));
        webService.registerResource(CALLBACK_CONTEXT_ALIAS, new EdcApiExceptionMapper(exceptionMapping));

        // Register API Key filter if configured
        if (config.getAuthApiKeyValue() != null && !config.getAuthApiKeyValue().isEmpty()) {
            var authService = new APIKeyAuthenticationService(context.getMonitor(), config.getAuthEdcApiKeyName(), config.getAuthApiKeyValue());
            webService.registerResource(new AuthenticationRequestFilter(authService));
        }

        // Register basic authentication filter if configured
        if (config.getBasicAuthUsers() != null && !config.getBasicAuthUsers().isEmpty()) {
            var authService = new BasicAuthenticationService(context.getMonitor(), config.getBasicAuthUsers());
            webService.registerResource(new AuthenticationRequestFilter(authService));
        }

        // In-memory stores
        var endpointDataReferenceStore = new InMemoryEndpointDataReferenceStore(context.getMonitor(), config.getEndpointDataReferenceCacheTimeInMinutes());
        var contractAgreementStore = new InMemoryContractAgreementStore();

        // Setup controller
        var contractOfferService = new ContractOfferService(monitor, typeManager, httpClient);
        var contractOfferRequestService = new ContractNegotiationService(monitor, typeManager, httpClient);
        var transferProcessService = new TransferProcessService(monitor, typeManager, httpClient);
        var httpProxyService = new HttpProxyService(monitor, httpClient, new HttpProxyService.DataFixes(config.fixNotNullFields(), monitor));

        webService.registerResource(DEFAULT_CONTEXT_ALIAS, new ApiWrapperController(
                monitor,
                contractOfferService,
                contractOfferRequestService,
                transferProcessService,
                httpProxyService,
                endpointDataReferenceStore,
                contractAgreementStore,
                config
        ));
        webService.registerResource(CALLBACK_CONTEXT_ALIAS, new EdcCallbackController(monitor, endpointDataReferenceStore));
    }

    private ApiWrapperConfig createApiWrapperConfig(Config config) {
        ApiWrapperConfig.Builder builder = ApiWrapperConfig.Builder.newInstance();

        builder.consumerEdcUrl(config.getString(ApiWrapperConfigKeys.CONSUMER_EDC_URL));

        var consumerEdcApiKeyName = config.getString(ApiWrapperConfigKeys.CONSUMER_EDC_APIKEY_NAME, null);
        if (consumerEdcApiKeyName != null) {
            builder.consumerEdcApiKeyName(consumerEdcApiKeyName);
        }

        var consumerEdcApiKeyValue = config.getString(ApiWrapperConfigKeys.CONSUMER_EDC_APIKEY_VALUE, null);
        if (consumerEdcApiKeyValue != null) {
            builder.consumerEdcApiKeyValue(consumerEdcApiKeyValue);
        }

        var authApiKeyName = config.getString(ApiWrapperConfigKeys.AUTH_APIKEY_NAME, null);
        if (authApiKeyName != null) {
            builder.authApiKeyName(authApiKeyName);
        }

        var authApiKeyValue = config.getString(ApiWrapperConfigKeys.AUTH_APIKEY_VALUE, null);
        if (authApiKeyValue != null) {
            builder.authApiKeyValue(authApiKeyValue);
        }

        var basicAuthUsers = config.getConfig(ApiWrapperConfigKeys.BASIC_AUTH).getRelativeEntries();
        if (!basicAuthUsers.isEmpty()) {
            if(authApiKeyValue != null){
                throw new EdcException("API-Key and Basic Auth User cannot be configured at the same time!");
            }
            builder.basicAuthUsers(basicAuthUsers);
        }

        //Configure caching time of EndpointDataReference
        builder.endpointDataReferenceCacheTimeInMinutes(config.getInteger(ApiWrapperConfigKeys.ENDPOINT_REF_CACHETIME_IN_MINS, 2));

        //Enable some temporary data fixes, if needed
        builder.fixNotNullFields(config.getString(ApiWrapperConfigKeys.DATA_FIX_NOTNULL_FIELDS,"").split(","));

        return builder.build();
    }
}
