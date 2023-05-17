package org.eclipse.dataspaceconnector.apiwrapper.connector.sdk.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;

public class EndpointDataReferenceWithTimeout {
    private EndpointDataReference theReference;
    private LocalDateTime endOfValidity;
    
    /**
     * Create a <code>EndpointDataReferenceWithTimeout</code> with the passed timeout in minutes.
     * 
     * @param theReference
     * @param timeoutInMins
     */
    public EndpointDataReferenceWithTimeout(EndpointDataReference theReference, int timeoutInMins) {
        this(theReference, timeoutInMins, ChronoUnit.MINUTES);
    }

    public EndpointDataReferenceWithTimeout(EndpointDataReference theReference, int timeoutInMins, TemporalUnit temporalUnit) {
        this.theReference = theReference;
        endOfValidity = LocalDateTime.now().plus(timeoutInMins, temporalUnit);
    }

    public boolean isValid(){
        return endOfValidity.isAfter(LocalDateTime.now());
    }

    public EndpointDataReference toEndpointDataReference(){
        return isValid()?theReference:null;
    }
}
