package org.eclipse.dataspaceconnector.extensions.vault.auth.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class StringWithTimeout {
    private String theString;
    private LocalDateTime endOfValidity;
    
    /**
     * Create a <code>StringWithTimeout</code> with the passed timeout in minutes.
     * 
     * @param theString
     * @param timeoutInMins
     */
    public StringWithTimeout(String theString, int timeoutInMins) {
        this(theString, timeoutInMins, ChronoUnit.MINUTES);
    }

    public StringWithTimeout(String theString, int timeoutInMins, TemporalUnit temporalUnit) {
        this.theString = theString;
        endOfValidity = LocalDateTime.now().plus(timeoutInMins, temporalUnit);
    }

    public boolean isValid(){
        return endOfValidity.isAfter(LocalDateTime.now());
    }

    public String toString(){
        return isValid()?theString:null;
    }
}
