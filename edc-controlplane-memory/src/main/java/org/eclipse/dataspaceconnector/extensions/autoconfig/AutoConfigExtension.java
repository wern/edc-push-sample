package org.eclipse.dataspaceconnector.extensions.autoconfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.dataspaceconnector.spi.asset.AssetIndex;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.policy.PolicyDefinition;
import org.eclipse.dataspaceconnector.spi.asset.AssetSelectorExpression;
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.store.PolicyDefinitionStore;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Inject;
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
    private static final String SECRET_NAME = "pcf.asset.auth.secret.name";
    private static final String IDP_URL = "pcf.asset.auth.oidc.idp.url";
    private static final String CLIENT_ID = "pcf.asset.auth.oidc.client.id";
    private static final String CLIENT_SECRET = "pcf.asset.auth.oidc.client.secret";
    private static final String AUTH_AUDIENCE = "pcf.asset.auth.oidc.auth.audience";
    private static final String REFRESH_INTERVAL_MINS= "pcf.asset.auth.oidc.auth.refresh.interval.mins";


    @Inject
    private ContractDefinitionStore contractStore;
    @Inject
    private AssetIndex loader;
    @Inject
    private PolicyDefinitionStore policyStore;
    @Inject
    private OkHttpClient httpClient;

    private Monitor monitor;

    private ServiceExtensionContext context;


    @Override
    public String name() {
        return "Asset Auto Config Ext.";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        this.monitor = context.getMonitor();
        this.context = context;
        var policy = createPolicy(context);
        policyStore.save(policy);

        registerDataEntries(context, false);
        registerContractDefinition(policy.getUid());

        monitor.info("AutoRegisterAssetExtension initialized, Assets registered.");
    }

    private PolicyDefinition createPolicy(ServiceExtensionContext context) {
        var assetID = context.getSetting(ASSET_ID, "42424242424242");

        context.getMonitor().debug("Using assetID '"+ assetID +"'");

        var usePermission = Permission.Builder.newInstance()
                .action(Action.Builder.newInstance().type("USE").build())
                .target(assetID)
                .build();

        return PolicyDefinition.Builder.newInstance()
                .id(POLICY_ID)
                .policy(Policy.Builder.newInstance()
                        .permission(usePermission)
                        .build())
                //.target(assetID)
                .build();
    }

    private void registerDataEntries(ServiceExtensionContext context, boolean removeOldAsset) {

        var monitor = context.getMonitor();

        var endpointURL = context.getSetting(PCF_ENDPOINT, "not set!");
        var assetID = context.getSetting(ASSET_ID, "42424242424242");
        var authKey = context.getSetting(AUTH_KEY, "Authorization");
        var secretName = context.getSetting(SECRET_NAME, "DynamicOAuthToken");

        monitor.debug("Using endpoint '"+ endpointURL +"'");
        monitor.debug("Using authKey '"+ authKey +"'");
        monitor.debug("Using secretName '"+ secretName +"'");

        var dataAddressBuilder = DataAddress.Builder.newInstance()
                .property("type", "HttpData")
                .property("baseUrl", endpointURL)
                .property("proxyMethod", "true")
                .property("proxyBody", "true")
                .property("proxyPath", "true")
                .property("proxyQueryParams", "true");

        if(authKey!=null ){
            dataAddressBuilder.property("authKey", authKey)
                              .property("secretName", secretName);
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
