package app.coshell.core.domain.receipts

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class TeleshkaFormatTest {
    private val ars = CurrencyCode("ARS")

    /** `docs/samples/receipts/teleshka.txt`: the text layer of a real PDF, delivery address replaced. */
    private val sample =
        "Teleshka\tЧЕК\n" +
        "\t#4807\n" +
        "Maure 3815, C1427 Cdad. Autónoma de Buenos Aires\n" +
        "Статус\tДоставили\n" +
        "Дата\t14.11.2026 09:42\n" +
        "Адрес доставки\tCalle Falsa 123, Ciudad Autónoma de Buenos Aires\n" +
        "Товар\tКол-во\tСумма\n" +
        "Сырники замороженные ванильные 15шт У беларуса, 975г\tx1\t${"$"}24.000\n" +
        "1 шт\n" +
        "Блины с говядиной и сердцем У Беларуса, 8шт\tx1\t${"$"}15.000\n" +
        "1 шт\n" +
        "Пельмени Сибирские У беларуса, 1кг\tx1\t${"$"}25.000\n" +
        "1 шт\n" +
        "Котлеты куриные СМАЧНЫЕ У беларуса, 500г\tx1\t${"$"}16.000\n" +
        "1 шт\n" +
        "Джерки говяжьи Fiambres Mundial, 50g\tx1\t${"$"}6.000\n" +
        "1 шт\n" +
        "Товары\t${"$"}86.000\n" +
        "Доставка\t${"$"}5.000\n" +
        "Скидка\t-${"$"}8.600\n" +
        "Итого\t${"$"}82.400\n" +
        "Наличные получены\t${"$"}82.400\n" +
        "Спасибо за покупку в Teleshka!\n" +
        "Сформировано 20.11.2026 10:53"

    @Test
    fun `reads the total, the labelled date and the number`() {
        val receipt = TeleshkaFormat.read(sample)!!
        assertEquals(Money.ofMajor(82_400, currency = ars), receipt.total)
        assertEquals(LocalDate(2026, 11, 14), receipt.date)
        assertEquals("teleshka:4807", receipt.key)
        assertEquals("Teleshka", receipt.merchant)
    }

    @Test
    fun `reads the line items`() {
        val items = TeleshkaFormat.read(sample)!!.items
        assertEquals(5, items.size)
        assertEquals("Пельмени Сибирские У беларуса, 1кг", items[2].name)
        assertEquals("1", items[2].quantity)
        assertEquals(Money.ofMajor(25_000, currency = ars), items[2].amount)
    }

    @Test
    fun `columns split by runs of spaces read the same`() {
        val spaced = sample.replace("\t", "      ")
        assertEquals(TeleshkaFormat.read(sample), TeleshkaFormat.read(spaced))
    }

    @Test
    fun `the generated-at date alone is not the purchase date`() {
        assertNull(TeleshkaFormat.read(sample.lines().filterNot { it.startsWith("Дата") }.joinToString("\n")))
    }

    @Test
    fun `no total means no receipt`() {
        assertNull(TeleshkaFormat.read(sample.lines().filterNot { it.startsWith("Итого") }.joinToString("\n")))
    }

    @Test
    fun `other text is not read`() {
        assertNull(TeleshkaFormat.read("Итого ${"$"}82.400\nДата 14.11.2026\n#4807"))
    }
}
