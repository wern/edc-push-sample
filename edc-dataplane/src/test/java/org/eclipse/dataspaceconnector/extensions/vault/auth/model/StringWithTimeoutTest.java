package org.eclipse.dataspaceconnector.extensions.vault.auth.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringWithTimeoutTest {
  
    @Test
    public void testStringIsValidWithinValidityPeriod(){
        StringWithTimeout swt = new StringWithTimeout("NotNull", 100);
        assertTrue(swt.isValid(), "String value should be valid");
    }

    @Test
    public void testStringIsNotNullWithinValidityPeriod(){
        StringWithTimeout swt = new StringWithTimeout("NotNull", 100);
        assertNotNull(swt.toString(), "String value should not be null");
        assertEquals("NotNull", swt.toString());
    }

    @Test
    public void testStringIsNullAfterValidityPeriod(){
        StringWithTimeout swt = new StringWithTimeout("NotNull", -100);
        assertNull(swt.toString(), "String value should be null");
    }
}
