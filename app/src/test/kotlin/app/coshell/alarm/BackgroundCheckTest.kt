package app.coshell.alarm

import org.junit.Assert.assertEquals
import org.junit.Test

class BackgroundCheckTest {
    @Test fun `xiaomi phones always get the autostart advice`() {
        for (brand in listOf("Xiaomi", "Redmi", "POCO")) assertEquals(BackgroundCheck.Advice.XIAOMI, BackgroundCheck.advice(brand, true))
    }

    @Test fun `other phones get the battery advice only while limited`() {
        assertEquals(BackgroundCheck.Advice.BATTERY, BackgroundCheck.advice("Google", false))
        assertEquals(BackgroundCheck.Advice.NONE, BackgroundCheck.advice("Google", true))
    }

    @Test fun `asks on enabling while limited, on xiaomi only for the first reminder`() {
        assertEquals(true, BackgroundCheck.askOnEnable(BackgroundCheck.Advice.BATTERY, anythingOnBefore = true))
        assertEquals(true, BackgroundCheck.askOnEnable(BackgroundCheck.Advice.XIAOMI, anythingOnBefore = false))
        assertEquals(false, BackgroundCheck.askOnEnable(BackgroundCheck.Advice.XIAOMI, anythingOnBefore = true))
        assertEquals(false, BackgroundCheck.askOnEnable(BackgroundCheck.Advice.NONE, anythingOnBefore = false))
    }

    @Test fun `home asks only after an update with something on and never on a fresh install`() {
        assertEquals(true, BackgroundCheck.askOnHome(answered = false, wasUpdated = true, anythingOn = true))
        assertEquals(false, BackgroundCheck.askOnHome(answered = false, wasUpdated = false, anythingOn = true))
        assertEquals(false, BackgroundCheck.askOnHome(answered = true, wasUpdated = true, anythingOn = true))
        assertEquals(false, BackgroundCheck.askOnHome(answered = false, wasUpdated = true, anythingOn = false))
    }
}
