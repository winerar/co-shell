package app.coshell.templates.edit

import androidx.lifecycle.SavedStateHandle
import app.coshell.core.domain.templates.SaveTemplateUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import app.coshell.navigation.TemplateEditDestination
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeTemplateRepository
import app.coshell.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** «Платёж в другой валюте» in the template editor (#415, board `Rec415B`; poll 3 «Д» — never «Проводить само»). */
class TemplateForeignTest {
    @get:Rule val main = MainDispatcherRule()

    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val accounts = FakeAccountRepository()
    private val templates = FakeTemplateRepository()

    private suspend fun viewModel(templateId: String? = null): TemplateEditViewModel {
        accounts.saveAccount(Account("galicia", "Galicia", ars, sortOrder = 0))
        accounts.saveAccount(Account("cash", "Наличные", usd, sortOrder = 1))
        return TemplateEditViewModel(
            savedStateHandle = SavedStateHandle(mapOf(TemplateEditDestination.ARG_TEMPLATE_ID to templateId)),
            accounts = accounts,
            categories = FakeCategoryRepository(),
            templates = templates,
            saveTemplate = SaveTemplateUseCase(accounts, FakeCategoryRepository(), templates),
            ids = IdGenerator { "rent" },
            moneyFormat = MoneyFormat.RU,
            clock = object : Clock {
                override fun now() = Instant.parse("2026-11-15T12:00:00Z")
            },
            timeZone = TimeZone.UTC,
        )
    }

    @Test
    fun `ticking picks the first other currency and saves the amount in it`() = runTest {
        val vm = viewModel()
        assertEquals(listOf("USD"), vm.state.value.foreignChoices)
        vm.onNameChange("Аренда")
        vm.onAmountChange("650")
        vm.onForeignToggle(true)
        assertEquals("USD", vm.state.value.amountCurrencyShown)
        vm.save()

        val saved = templates.getTemplate("rent")!!
        assertEquals(Money(650_00, usd), saved.amount)
        assertEquals(ars, saved.currency)
        assertTrue(saved.isForeign)
    }

    @Test
    fun `a payment in another currency cannot post by itself`() = runTest {
        val vm = viewModel()
        vm.onNameChange("Аренда")
        vm.onAmountChange("650")
        vm.onRepeatChange(Recurrence.Monthly(5), PostMode.AUTO)
        assertTrue(vm.state.value.canPostAuto)

        vm.onForeignToggle(true)
        assertFalse(vm.state.value.canPostAuto)
        assertEquals(PostMode.REMIND, vm.state.value.postMode)
        vm.save()
        assertEquals(PostMode.REMIND, templates.getTemplate("rent")?.schedule?.mode)
    }

    @Test
    fun `the account's own currency, a bad code or no amount are refused`() = runTest {
        val vm = viewModel()
        vm.onNameChange("Аренда")
        vm.onForeignToggle(true)
        vm.save()
        assertTrue("the receipt amount is needed", vm.state.value.amountError)

        vm.onAmountChange("650")
        vm.onAmountCurrencyChange("ars")
        vm.save()
        assertTrue(vm.state.value.amountCurrencyError)

        vm.onAmountCurrencyChange("U1")
        vm.save()
        assertTrue(vm.state.value.amountCurrencyError)
        assertNull(templates.getTemplate("rent"))
    }

    @Test
    fun `unticking saves the amount in the account currency, a transfer ignores the box`() = runTest {
        templates.saveTemplate(TransactionTemplate("rent", "Аренда", TransactionType.EXPENSE, "galicia", ars, Money(650_00, usd)))
        val vm = viewModel("rent")
        assertTrue(vm.state.value.foreign)

        vm.onForeignToggle(false)
        assertEquals("ARS", vm.state.value.amountCurrencyShown)
        vm.onTypeChange(TransactionType.TRANSFER)
        vm.onForeignToggle(true)
        assertFalse(vm.state.value.isForeign)
        vm.onTypeChange(TransactionType.EXPENSE)
        vm.onForeignToggle(false)
        vm.save()
        assertEquals(Money(650_00, ars), templates.getTemplate("rent")?.amount)
    }
}
