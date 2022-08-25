package org.eclipse.dataspaceconnector.extensions.autoconfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.dataspaceconnector.dataloading.AssetLoader;
import org.eclipse.dataspaceconnector.extensions.autoconfig.service.OAuthLoginService;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.asset.AssetSelectorExpression;
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.store.PolicyStore;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.types.domain.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractDefinition;

import okhttp3.OkHttpClient;

public class AutoConfigExtension implements ServiceExtension {

    private static final String USE_POLICY = "use-eu";
    private static final String POLICY_ID = "8f4dfcfb-a4fb-4582-96b9-7984309e44a8";
    private static final String ASSET_ID = "pcf.asset.id";
    private static final String PCF_ENDPOINT = "pcf.asset.endpoint";
    private static final String AUTH_KEY = "pcf.asset.auth.key";
    private static final String AUTH_CODE = "pcf.asset.auth.code";
    private static final String IDP_URL = "pcf.asset.auth.oidc.idp.url";
    private static final String CLIENT_ID = "pcf.asset.auth.oidc.client.id";
    private static final String CLIENT_SECRET = "pcf.asset.auth.oidc.client.secret";


    @Inject
    private ContractDefinitionStore contractStore;
    @Inject
    private AssetLoader loader;
    @Inject
    private PolicyStore policyStore;
    @Inject
    private OkHttpClient httpClient;

    private Monitor monitor;

    private ServiceExtensionContext context;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private ScheduledFuture<?> schedulerHandle;

    private OAuthLoginService loginService;

    public void initialize(ServiceExtensionContext context) {
        this.monitor = context.getMonitor();
        this.context = context;
        loginService = new OAuthLoginService(monitor, httpClient);
        var authCode = context.getSetting(AUTH_CODE, "");
        var policy = createPolicy(context);
        policyStore.save(policy);

        registerDataEntries(context, authCode, false);
        registerContractDefinition(policy.getUid());

        monitor.info("AutoRegisterAssetExtension initialized, Assets registered.");
    }

    public void start() {
        monitor.info("AutoRegisterAssetExtension starting... ");
        final Runnable assetUpdater = new Runnable() {
            public void run() { 
                monitor.debug("Trying to refresh asset...");
                var idpUrl = context.getSetting(IDP_URL, "");
                var clientId = context.getSetting(CLIENT_ID, "");
                var clientSecret = context.getSetting(CLIENT_SECRET, "");

                if( idpUrl.isBlank() || clientId.isBlank() || clientSecret.isBlank()){
                    monitor.info("No IDP information configured. Token refresh disabled!");
                    return;
                }

                try{
                    registerDataEntries(context, "Bearer " + loginService.getToken(idpUrl, clientId, clientSecret), true); 
                }catch(Throwable t){
                    monitor.severe("Something went wrong while recreationg the asset", t);
                }
                monitor.debug("Done!");
            }
         };
         schedulerHandle =
            scheduler.scheduleAtFixedRate(assetUpdater, 0, 60, TimeUnit.MINUTES);
    }
    
    public void shutdown() {
        monitor.info("AutoRegisterAssetExtension shutting down... ");
        schedulerHandle.cancel(true);
    }

    private Policy createPolicy(ServiceExtensionContext context) {
        var assetID = context.getSetting(ASSET_ID, "42424242424242");

        context.getMonitor().debug("Using assetID '"+ assetID +"'");

        var usePermission = Permission.Builder.newInstance()
                .action(Action.Builder.newInstance().type("USE").build())
                .target(assetID)
                .build();

        return Policy.Builder.newInstance()
                .id(POLICY_ID)
                .permission(usePermission)
                .target(assetID)
                .build();
    }

    private void registerDataEntries(ServiceExtensionContext context, String authCode, boolean removeOldAsset) {

        var monitor = context.getMonitor();

        var endpointURL = context.getSetting(PCF_ENDPOINT, "not set!");
        var assetID = context.getSetting(ASSET_ID, "42424242424242");
        var authKey = context.getSetting(AUTH_KEY, "");

        monitor.debug("Using endpoint '"+ endpointURL +"'");
        monitor.debug("Using authKey '"+ authKey +"'");
        monitor.debug("Using authCode '"+ authCode +"'");

        var dataAddressBuilder = DataAddress.Builder.newInstance()
                .property("type", "HttpData")
                .property("endpoint", endpointURL)
                .property("proxyMethod", "true")
                .property("proxyBody", "true")
                .property("proxyPath", "true")
                .property("proxyQueryParams", "true");

        if(authKey!=null && authCode!=null){
            dataAddressBuilder.property("authKey", authKey)
                              .property("authCode", authCode);
        } else {
            monitor.warning("No authorization info configured for asset " + assetID);
        }

        var dataAddress = dataAddressBuilder.build();


        var asset = Asset.Builder.newInstance()
                .id(assetID)
                .name("PCF Exchange Asset")
                .contentType("application/json")
                .property("asset:prop:policy-id", USE_POLICY)
                .property("asset:prop:type", "pcf-push")
                .build();

        if(removeOldAsset){
            monitor.debug("Removing old asset...");
            loader.deleteById(asset.getId());
        }

        monitor.debug("Loading new asset...");
        loader.accept(asset, dataAddress);
    }

    private void registerContractDefinition(String uid) {
        var contractDefinition = ContractDefinition.Builder.newInstance()
                .id("1")
                .accessPolicyId(uid)
                .contractPolicyId(uid)
                .selectorExpression(AssetSelectorExpression.Builder.newInstance().build())
                .build();

        contractStore.save(contractDefinition);
    }
}
