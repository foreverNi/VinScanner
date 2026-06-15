package com.vinscanner.app.util

import com.vinscanner.app.data.VinRecord
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailSenderTest {

    @Test fun `buildBody includes all VINs with timestamps`() {
        val records = listOf(
            VinRecord("LSGPC54U3KD123456", 1_700_000_000_000L, "scan"),
            VinRecord("LSGPC54U3KD123457", 1_700_000_000_000L, "manual")
        )
        val body = EmailSender.buildBody(records)
        assertTrue(body.contains("LSGPC54U3KD123456"))
        assertTrue(body.contains("LSGPC54U3KD123457"))
        assertTrue(body.contains("共计 2 条。"))
        assertTrue(body.contains("VinScanner"))
    }

    @Test fun `buildBody with empty list still shows header and footer`() {
        val body = EmailSender.buildBody(emptyList())
        assertTrue(body.contains("VIN"))
        assertTrue(body.contains("共计 0 条。"))
    }
}
