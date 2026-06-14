package com.vinscanner.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VinValidatorTest {

    @Test fun `valid VIN with 17 chars`() {
        assertTrue(VinValidator.isValid("LSGPC54U3KD123456"))
    }

    @Test fun `invalid - shorter than 17`() {
        assertFalse(VinValidator.isValid("LSGPC54U3KD12345"))
    }

    @Test fun `invalid - contains forbidden letter I`() {
        assertFalse(VinValidator.isValid("LSGPC54U3KDI23456"))
    }

    @Test fun `invalid - contains forbidden letter O`() {
        assertFalse(VinValidator.isValid("LSGPC54U3KDO23456"))
    }

    @Test fun `invalid - contains forbidden letter Q`() {
        assertFalse(VinValidator.isValid("LSGPC54U3KDQ23456"))
    }

    @Test fun `normalizes lowercase to uppercase`() {
        assertEquals("LSGPC54U3KD123456", VinValidator.normalize("lsgpc54u3kd123456"))
    }

    @Test fun `normalizes trims whitespace`() {
        assertEquals("LSGPC54U3KD123456", VinValidator.normalize("  LSGPC54U3KD123456  "))
    }

    @Test fun `null input returns empty normalized`() {
        assertEquals("", VinValidator.normalize(null))
        assertFalse(VinValidator.isValid(null))
    }

    @Test fun `normalizes filters out forbidden letters`() {
        assertEquals("LSGPC54U3KD23456", VinValidator.normalize("LSGPC54U3KDI23456"))
    }
}
