package app.coshell.core.data.sync

import app.coshell.core.data.db.AccountEntity
import app.coshell.core.data.db.BudgetEntity
import app.coshell.core.data.db.CategoryEntity
import app.coshell.core.data.db.ManualRateEntity
import app.coshell.core.data.db.TemplateEntity
import app.coshell.core.data.db.TransactionEntity
import kotlinx.serialization.json.Json

/**
 * Room entity ⇄ [SyncRecord]. The payload is the entity's JSON: adding a column with a default
 * value is compatible both ways (unknown keys are ignored, missing keys take the default).
 * Renaming or removing a field is not — treat entity property names as a wire format.
 */
object SyncCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(e: AccountEntity) =
        SyncRecord(SyncKind.ACCOUNT, e.id, e.updatedAt, e.updatedBy, e.deleted, json.encodeToString(e))
    fun encode(e: CategoryEntity) =
        SyncRecord(SyncKind.CATEGORY, e.id, e.updatedAt, e.updatedBy, e.deleted, json.encodeToString(e))
    fun encode(e: TransactionEntity) =
        SyncRecord(SyncKind.TRANSACTION, e.id, e.updatedAt, e.updatedBy, e.deleted, json.encodeToString(e))
    fun encode(e: BudgetEntity) =
        SyncRecord(SyncKind.BUDGET, e.id, e.updatedAt, e.updatedBy, e.deleted, json.encodeToString(e))
    fun encode(e: ManualRateEntity) =
        SyncRecord(SyncKind.MANUAL_RATE, e.id, e.updatedAt, e.updatedBy, e.deleted, json.encodeToString(e))
    fun encode(e: TemplateEntity) =
        SyncRecord(SyncKind.TEMPLATE, e.id, e.updatedAt, e.updatedBy, e.deleted, json.encodeToString(e))

    // Decoded rows come from the server, so they are not pending.
    fun account(r: SyncRecord) = json.decodeFromString<AccountEntity>(r.json).copy(pendingSync = false)
    fun category(r: SyncRecord) = json.decodeFromString<CategoryEntity>(r.json).copy(pendingSync = false)
    fun transaction(r: SyncRecord) = json.decodeFromString<TransactionEntity>(r.json).copy(pendingSync = false)
    fun budget(r: SyncRecord) = json.decodeFromString<BudgetEntity>(r.json).copy(pendingSync = false)
    fun manualRate(r: SyncRecord) = json.decodeFromString<ManualRateEntity>(r.json).copy(pendingSync = false)
    fun template(r: SyncRecord) = json.decodeFromString<TemplateEntity>(r.json).copy(pendingSync = false)
}
