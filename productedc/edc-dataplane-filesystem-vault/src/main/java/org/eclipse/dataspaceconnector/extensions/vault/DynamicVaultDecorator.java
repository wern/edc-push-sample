package org.eclipse.dataspaceconnector.extensions.vault;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.dataspaceconnector.extensions.vault.auth.OAuthTokenFactory;
import org.eclipse.dataspaceconnector.extensions.vault.auth.model.StringWithTimeout;
import org.eclipse.edc.spi.result.Result; 
import org.eclipse.edc.spi.security.Vault;

public class DynamicVaultDecorator implements Vault {
    private final static String DYNAMIC_SECRET_NAME = "DynamicOAuthToken";

    private final AtomicReference<StringWithTimeout> cachedToken = new AtomicReference<>();
    private final Vault delegate;
    private final OAuthTokenFactory tokenFactory;
    private final int timeoutInMins;

    public DynamicVaultDecorator(Vault delegate, OAuthTokenFactory tokenFactory, int tokenRefreshIntervalInMins) {
        if(delegate == null) {
            throw new NullPointerException("Delegate Vault is null!");
        }
        this.delegate = delegate;
        this.tokenFactory=tokenFactory;
        this.timeoutInMins = tokenRefreshIntervalInMins;
    }
   
    // ToDo make secretName configurable
    public String resolveSecret(String key){ 
        if(DYNAMIC_SECRET_NAME.equals(key)) {
            StringWithTimeout token = cachedToken.get();
            if(token == null || token.toString() == null){
                //System.out.println("##DVD## Creating new Token!");
                StringWithTimeout newToken = new StringWithTimeout(tokenFactory.createOAuthToken(), timeoutInMins);
                cachedToken.set(newToken);
                return newToken.toString();
            } else {
                ///System.out.println("##DVD## Returning cached Token!");
                return token.toString();  
            }
        } else {
            return delegate.resolveSecret(key);
        }
    } 

    public Result<Void> storeSecret(String key, String value) {
        return delegate.storeSecret(key, value);
    }

    public Result<Void> deleteSecret(String key){ 
        return delegate.deleteSecret(key);
    }
}