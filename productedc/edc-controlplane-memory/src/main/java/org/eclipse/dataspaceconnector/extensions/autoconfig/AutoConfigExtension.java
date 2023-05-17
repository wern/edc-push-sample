package org.eclipse.dataspaceconnector.extensions.autoconfig;

import org.eclipse.edc.spi.asset.AssetIndex;
import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.connector.policy.spi.PolicyDefinition;
import org.eclipse.edc.spi.asset.AssetSelectorExpression;
import org.eclipse.edc.connector.contract.spi.offer.store.ContractDefinitionStore;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.connector.policy.spi.store.PolicyDefinitionStore;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.asset.Asset;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractDefinition;

import okhttp3.OkHttpClient;

public class AutoConfigExtension implements ServiceExtension {

    private static final String USE_POLICY = "use-eu";
    private static final String POLICY_ID = "8f4dfcfb-a4fb-4582-96b9-7984309e44a8";
    private static final String ASSET_ID = "pcf.asset.id";
    private static final String PCF_ENDPOINT = "pcf.asset.endpoint";
    private static final String AUTH_KEY = "pcf.asset.auth.key";
    private static final String SECRET_NAME = "pcf.asset.auth.secret.name";
    private static final String VALIDITY_SECS = "pcf.asset.validity.in.seconds";

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
              //  .constraint(
              //    AtomicConstraint.Builder.newInstance()
              //    .leftExpression(new LiteralExpression("idsc:PURPOSE"))
              //    .operator(Operator.EQ)
              //    .rightExpression(new LiteralExpression("ID 3.0 PCF"))
              //      .build()
              //  )
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
        var validityInSecs = context.getSetting(VALIDITY_SECS, 31536000L);

        var contractDefinition = ContractDefinition.Builder.newInstance()
                .id("1")
                .accessPolicyId(uid)
                .contractPolicyId(uid)
                .validity(validityInSecs)
                .selectorExpression(AssetSelectorExpression.Builder.newInstance().build())
                .build();

        contractStore.save(contractDefinition);
    }
}
