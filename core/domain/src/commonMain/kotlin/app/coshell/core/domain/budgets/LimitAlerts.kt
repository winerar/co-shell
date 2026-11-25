package app.coshell.core.domain.budgets

import app.coshell.core.domain.hints.LimitWatch

/** Why a limit is worth a notification (#186): 80 % of it is spent, or spending runs ahead of the month. */
enum class LimitAlertKind { NEAR, PACE }

/** One notification about the limit of [progress]; [key] — the same limit, month and kind are told once. */
data class LimitAlert(val progress: BudgetProgress, val kind: LimitAlertKind) {
    val key: String get() = "${progress.budget.categoryId}:${progress.period.month}:${kind.name}"

    /** «Это 20 % лимита»: what is still available, of the month's limit, rounded down. */
    val leftPercent: Int get() = (progress.left.amountMinor * 100 / progress.available.amountMinor).toInt()

    /** «на 12 дней»: the days still to go, today included. */
    val daysToGo: Int get() = progress.daysLeft + 1
}

/**
 * Which limit the daily notification (#186) is about: at most one a day. First the limits 80–99 % spent ([LimitWatch], the same
 * as the hint `limit_80`), nearest to the end first; then those whose spending runs ahead of the month ([BudgetState.FAST]) and
 * whose forecast would use the limit up before the month ends, the fastest first. Over the limit — no notification: without
 * red and «превышен». [told] — keys already sent; each limit hears of each kind once a month.
 */
object LimitAlerts {
    fun pick(progress: List<BudgetProgress>, told: Set<String>): LimitAlert? {
        val current = progress.filter { it.paceToday != null && it.available.isPositive && it.left.isPositive }
        val near = LimitWatch.nearest(current.filter { LimitAlert(it, LimitAlertKind.NEAR).key !in told })
        if (near != null) return LimitAlert(near, LimitAlertKind.NEAR)
        return current
            .filter { it.state == BudgetState.FAST && !isNear(it) }
            .mapNotNull { p -> p.forecast?.takeIf { it > p.available }?.let { f -> LimitAlert(p, LimitAlertKind.PACE) to f.amountMinor * 1_000 / p.available.amountMinor } }
            .filter { it.first.key !in told }
            .maxByOrNull { it.second }
            ?.first
    }

    private fun isNear(p: BudgetProgress) = p.spent.amountMinor * 100 >= p.available.amountMinor * LimitWatch.FROM_PERCENT
}
