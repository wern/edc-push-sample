package org.eclipse.dataspaceconnector.apiwrapper.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ApiWrapperConfig {

    private final String consumerEDCUrl;
    private final String consumerEdcApiKeyName;
    private final String consumerEdcApiKeyValue;
    private final String authApiKeyName;
    private final String authApiKeyValue;
    private final Map<String, String> basicAuthUsers;
    private final List<String> fixNotNullFields;
    private final int endpointDataReferenceCacheTimeInMinutes; 

    public ApiWrapperConfig(String consumerEDCUrl, String consumerEdcApiKeyName, String consumerEdcApiKeyValue, String authApiKeyName, String authApiKeyValue, Map<String, String> basicAuthUsers, List<String> fixNotNullFields, int endpointDataReferenceCacheTimeInMinutes) {
        this.consumerEDCUrl = consumerEDCUrl;
        this.consumerEdcApiKeyName = consumerEdcApiKeyName;
        this.consumerEdcApiKeyValue = consumerEdcApiKeyValue;
        this.authApiKeyName = authApiKeyName;
        this.authApiKeyValue = authApiKeyValue;
        this.basicAuthUsers = basicAuthUsers;
        this.fixNotNullFields = fixNotNullFields;
        this.endpointDataReferenceCacheTimeInMinutes = endpointDataReferenceCacheTimeInMinutes;
    }

    public String getConsumerEDCUrl() {
        return consumerEDCUrl;
    }

    public String getConsumerEdcApiKeyName() {
        return consumerEdcApiKeyName;
    }

    public String getConsumerEdcApiKeyValue() {
        return consumerEdcApiKeyValue;
    }

    public String getAuthEdcApiKeyName() {
        return authApiKeyName;
    }

    public String getAuthApiKeyValue() {
        return authApiKeyValue;
    }

    public Map<String, String> getBasicAuthUsers() {
        return basicAuthUsers;
    }

    public List<String> fixNotNullFields() {
        return fixNotNullFields;
    }

    public int getEndpointDataReferenceCacheTimeInMinutes() {
        return endpointDataReferenceCacheTimeInMinutes;
    }

    public static final class Builder {
        private String consumerEdcUrl = null;
        private String consumerEdcApiKeyName = "X-Api-Key";
        private String consumerEdcApiKeyValue = "";
        private String authApiKeyName = "X-Api-Key";
        private String authApiKeyValue = null;
        private Map<String, String> basicAuthUsers = Collections.emptyMap();
        private List<String> fixNotNullFields = Collections.emptyList();
        private int endpointDataReferenceCacheTimeInMinutes=2; 

        private Builder() {
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder consumerEdcUrl(String consumerEdcUrl) {
            this.consumerEdcUrl = consumerEdcUrl;
            return this;
        }

        public Builder consumerEdcApiKeyName(String consumerEdcApiKeyName) {
            this.consumerEdcApiKeyName = consumerEdcApiKeyName;
            return this;
        }

        public Builder consumerEdcApiKeyValue(String consumerEdcApiKeyValue) {
            this.consumerEdcApiKeyValue = consumerEdcApiKeyValue;
            return this;
        }

        public Builder authApiKeyName(String authApiKeyName) {
            this.authApiKeyName = authApiKeyName;
            return this;
        }

        public Builder authApiKeyValue(String authApiKeyValue) {
            this.authApiKeyValue = authApiKeyValue;
            return this;
        }

        public Builder basicAuthUsers(Map<String, String> basicAuthUsers) {
            this.basicAuthUsers = basicAuthUsers;
            return this;
        }

        public Builder fixNotNullFields(String... fields) {
            this.fixNotNullFields  = Arrays.asList(fields);
            return this;
        }

        public Builder endpointDataReferenceCacheTimeInMinutes(int timeout) {
            this.endpointDataReferenceCacheTimeInMinutes  = timeout;
            return this;
        }

        public ApiWrapperConfig build() {
            return new ApiWrapperConfig(consumerEdcUrl, consumerEdcApiKeyName, consumerEdcApiKeyValue, authApiKeyName, authApiKeyValue, basicAuthUsers, fixNotNullFields, endpointDataReferenceCacheTimeInMinutes);
        }
    }
}
