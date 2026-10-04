package com.dosebloom.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateManagerTest {

    @Test
    fun newerMajorVersionDetected() {
        assertTrue(UpdateManager.isNewerVersion("2.0.0", "1.5.0"))
        assertTrue(UpdateManager.isNewerVersion("2.0", "1.5.0"))
    }

    @Test
    fun newerMinorVersionDetected() {
        assertTrue(UpdateManager.isNewerVersion("1.6.0", "1.5.0"))
        assertTrue(UpdateManager.isNewerVersion("1.5.1", "1.5.0"))
    }

    @Test
    fun sameVersionNotNewer() {
        assertFalse(UpdateManager.isNewerVersion("1.5.0", "1.5.0"))
        assertFalse(UpdateManager.isNewerVersion("2.0.0", "2.0.0"))
    }

    @Test
    fun olderVersionNotNewer() {
        assertFalse(UpdateManager.isNewerVersion("1.4.9", "1.5.0"))
        assertFalse(UpdateManager.isNewerVersion("1.0.0", "2.0.0"))
    }

    @Test
    fun handlesVariableLengthSegments() {
        assertTrue(UpdateManager.isNewerVersion("1.5.0.1", "1.5.0"))
        assertFalse(UpdateManager.isNewerVersion("1.5", "1.5.0"))
    }

    @Test
    fun v201IsNewerThanV200() {
        assertTrue(UpdateManager.isNewerVersion("2.0.1", "2.0.0"))
        assertFalse(UpdateManager.isNewerVersion("2.0.0", "2.0.1"))
    }
}
