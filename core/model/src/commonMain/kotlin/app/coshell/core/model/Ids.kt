package app.coshell.core.model

import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Typed identifiers: an AccountId cannot be passed where a CategoryId is expected.
// Values are random UUID strings, generated on the device that creates the entity,
// so two offline phones never produce the same id.

@JvmInline
value class AccountId(val value: String) {
    override fun toString(): String = value
    companion object {
        fun random(): AccountId = AccountId(randomId())
    }
}

@JvmInline
value class CategoryId(val value: String) {
    override fun toString(): String = value
    companion object {
        fun random(): CategoryId = CategoryId(randomId())
    }
}

@JvmInline
value class TransactionId(val value: String) {
    override fun toString(): String = value
    companion object {
        fun random(): TransactionId = TransactionId(randomId())
    }
}

/** Family member; the value is the auth uid once sign-in exists (stage 2). */
@JvmInline
value class MemberId(val value: String) {
    override fun toString(): String = value
}

@OptIn(ExperimentalUuidApi::class)
internal fun randomId(): String = Uuid.random().toString()
