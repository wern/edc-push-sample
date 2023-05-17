package org.eclipse.dataspaceconnector.extensions.vault;

import org.eclipse.dataspaceconnector.extensions.vault.auth.OAuthTokenFactory;
import org.eclipse.dataspaceconnector.extensions.vault.auth.service.OAuthLoginService;

import org.eclipse.edc.runtime.metamodel.annotation.BaseExtension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Requires;

import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.security.Vault;

import okhttp3.OkHttpClient;

@BaseExtension
@Requires({ Vault.class })
public class DynamicVaultExtension implements ServiceExtension, OAuthTokenFactory {

    private static final String IDP_URL = "pcf.asset.auth.oidc.idp.url";
    private static final String CLIENT_ID = "pcf.asset.auth.oidc.client.id";
    private static final String CLIENT_SECRET = "pcf.asset.auth.oidc.client.secret";
    private static final String AUTH_AUDIENCE = "pcf.asset.auth.oidc.auth.audience";
    private static final String TOKEN_CACHE_MINS= "pcf.asset.auth.oidc.auth.cache.mins";

    private static final String DEFAULT_CACHE_TIME_MINS = "25";

    @Inject
    private OkHttpClient httpClient;

    private Monitor monitor;

    private ServiceExtensionContext context;

    private OAuthLoginService loginService;

    @Override
    public String name() {
        return "Dynamic Vault Ext.";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        this.context = context;
        this.monitor = context.getMonitor();
        this.loginService = new OAuthLoginService(monitor, httpClient);

        int cacheTimeoutInMinutes = Integer.parseInt(context.getSetting(TOKEN_CACHE_MINS, DEFAULT_CACHE_TIME_MINS));

        Vault delegate = context.getService(Vault.class);
        if(delegate != null) {
            monitor.info("Decorating registered Vault to enable dynamic Token creation.");
            monitor.info("Caching tokens for " + cacheTimeoutInMinutes + " mins.");
            context.registerService(Vault.class, new DynamicVaultDecorator(delegate, this, cacheTimeoutInMinutes));
        } else {
            monitor.warning("Unable to decorate, target Vault is null!");
        }
        monitor.info("DynamicVaultExtension initialized");
    }

    //ToDo: Extract in own class
    public String createOAuthToken() { 
        monitor.debug("Trying to refresh token...");
        var idpUrl = context.getSetting(IDP_URL, "");
        var clientId = context.getSetting(CLIENT_ID, "");
        var clientSecret = context.getSetting(CLIENT_SECRET, "");
        var audience = context.getSetting(AUTH_AUDIENCE, "");

        if( isIdpConfigurationMissing(idpUrl, clientId, clientSecret, audience)){
            monitor.info("No IDP information configured. Token refresh disabled!");
            return null;
        } else {
            return getTokenFromService(idpUrl, clientId, clientSecret, audience);
        }
        
    }

    private boolean isIdpConfigurationMissing(String idpUrl, String clientId, String clientSecret, String audience) {
        return idpUrl.isBlank() || clientId.isBlank() || clientSecret.isBlank() || audience.isBlank();
    }

    private String getTokenFromService(String idpUrl, String clientId, String clientSecret, String audience){
        try{
            var token = loginService.getToken(idpUrl, clientId, clientSecret, audience);
            monitor.info("Token created!");
            monitor.debug(token);
            return "Bearer " + token; 
        }catch(Throwable t){
            monitor.severe("Something went wrong while getting the token", t);
            return null;
        }
    }

}
