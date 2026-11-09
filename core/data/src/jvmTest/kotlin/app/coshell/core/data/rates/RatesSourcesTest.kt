package app.coshell.core.data.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class RatesSourcesTest {
    private val usd = CurrencyCode("USD")

    private class FakeHttp(val body: String) : HttpGet {
        val urls = mutableListOf<String>()
        override suspend fun get(url: String): String {
            urls += url
            return body
        }
    }

    @Test
    fun jsonNumbersAreReadExactly() {
        assertEquals(Rate.parse("1049.69"), rateFromJsonLiteral("1049.69"))
        assertEquals(Rate.parse("0.000012345"), rateFromJsonLiteral("1.2345e-05"))
        assertEquals(Rate.parse("5000"), rateFromJsonLiteral("5E+3"))
        assertEquals(Rate.parse("386"), rateFromJsonLiteral("386"))
        // more than 15 significant digits are rounded half to even, not cut through Double
        assertEquals(Rate.parse("0.123456789012346"), rateFromJsonLiteral("0.12345678901234567"))
        assertNull(rateFromJsonLiteral("0"))
        assertNull(rateFromJsonLiteral("-1.5"))
        assertNull(rateFromJsonLiteral("1e-30")) // below Rate's range
        assertNull(rateFromJsonLiteral("abc"))
    }

    @Test
    fun currencyApiUrls() {
        val http = FakeHttp("")
        val day = LocalDate(2026, 11, 8)
        assertEquals(
            "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@2026-11-08/v1/currencies/usd.json",
            CurrencyApiSource(http, CurrencyApiSource.Mirror.JSDELIVR).url(day),
        )
        assertEquals(
            "https://latest.currency-api.pages.dev/v1/currencies/usd.json",
            CurrencyApiSource(http, CurrencyApiSource.Mirror.CLOUDFLARE).url(null),
        )
    }

    @Test
    fun currencyApiParsesTableAndSkipsJunk() = runTest {
        val body = """
            {"date": "2026-11-07", "usd": {
              "ars": 1049.69, "amd": 386.12, "usdt": 0.99981, "btc": 9.87e-06,
              "usd": 1, "1inch": 3.5, "bad-code": 2, "zero": 0, "text": "12", "nested": {"a": 1}
            }}
        """.trimIndent()
        val snapshot = CurrencyApiSource(FakeHttp(body), CurrencyApiSource.Mirror.JSDELIVR).fetch(null)
        assertEquals(LocalDate(2026, 11, 7), snapshot.date)
        assertEquals("currency-api/jsdelivr", snapshot.source)
        val byCode = snapshot.rates.associate { it.to.value to it.rate.toPlainString() }
        assertEquals(
            mapOf("ARS" to "1049.69", "AMD" to "386.12", "USDT" to "0.99981", "BTC" to "0.00000987", "1INCH" to "3.5"),
            byCode,
        )
        snapshot.rates.forEach {
            assertEquals(usd, it.from)
            assertEquals(RateOrigin.DOWNLOADED, it.origin)
            assertEquals(snapshot.date, it.date)
        }
    }

    @Test
    fun currencyApiRejectsBrokenAnswer() = runTest {
        val source = CurrencyApiSource(FakeHttp("""{"date": "2026-11-07"}"""), CurrencyApiSource.Mirror.CLOUDFLARE)
        assertFailsWith<IllegalStateException> { source.fetch(null) }
        val notJson = CurrencyApiSource(FakeHttp("<html>"), CurrencyApiSource.Mirror.CLOUDFLARE)
        assertFailsWith<Exception> { notJson.fetch(null) }
    }

    @Test
    fun openErApiParsesLatest() = runTest {
        val http = FakeHttp(
            """{"result": "success", "time_last_update_unix": 1762560151, "base_code": "USD",
               "rates": {"USD": 1, "ARS": 1449.5, "AMD": 383.7}}""",
        )
        val snapshot = OpenErApiSource(http).fetch(null)!!
        assertEquals(listOf("https://open.er-api.com/v6/latest/USD"), http.urls)
        assertEquals(LocalDate(2025, 11, 8), snapshot.date) // 1762560151 = 2025-11-08T00:02:31Z
        assertEquals(mapOf("ARS" to "1449.5", "AMD" to "383.7"), snapshot.rates.associate { it.to.value to it.rate.toPlainString() })
    }

    @Test
    fun openErApiHasNoHistoryAndChecksResult() = runTest {
        val http = FakeHttp("""{"result": "error", "error-type": "unsupported-code"}""")
        assertNull(OpenErApiSource(http).fetch(LocalDate(2026, 1, 1)))
        assertEquals(emptyList(), http.urls)
        assertFailsWith<IllegalStateException> { OpenErApiSource(http).fetch(null) }
    }
}
