package org.eclipse.dataspaceconnector.apiwrapper.config;

import org.eclipse.dataspaceconnector.spi.EdcSetting;

public final class ApiWrapperConfigKeys {

    @EdcSetting(required = true)
    public static final String CONSUMER_EDC_URL = "wrapper.consumer.edc.url";

    @EdcSetting
    public static final String CONSUMER_EDC_APIKEY_NAME = "wrapper.consumer.edc.apikey.name";

    @EdcSetting
    public static final String CONSUMER_EDC_APIKEY_VALUE = "wrapper.consumer.edc.apikey.value";

    @EdcSetting
    public static final String AUTH_APIKEY_NAME = "wrapper.auth.apikey.name";
    
    @EdcSetting
    public static final String AUTH_APIKEY_VALUE = "wrapper.auth.apikey.value";

    @EdcSetting
    public static final String BASIC_AUTH = "wrapper.auth.basic";

    @EdcSetting
    public static final String DATA_FIX_NOTNULL_FIELDS="wrapper.data.fix.notnullable.fields";

    private ApiWrapperConfigKeys() {}
}
