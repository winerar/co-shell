package app.coshell.update.components

import app.coshell.core.designsystem.components.HeroRing
import app.coshell.update.UpdatePhase

/** What the big ring of the update screen shows for each phase (pure, unit-tested). */
internal enum class UpdateHeroStep { Checking, UpToDate, NotConfigured, Available, Downloading, Installing, Failed }

internal val UpdatePhase.heroStep: UpdateHeroStep
    get() = when (this) {
        UpdatePhase.Checking -> UpdateHeroStep.Checking
        UpdatePhase.UpToDate -> UpdateHeroStep.UpToDate
        UpdatePhase.NotConfigured -> UpdateHeroStep.NotConfigured
        is UpdatePhase.Available -> UpdateHeroStep.Available
        is UpdatePhase.Downloading -> UpdateHeroStep.Downloading
        is UpdatePhase.Installing -> UpdateHeroStep.Installing
        is UpdatePhase.Failed -> UpdateHeroStep.Failed
    }

internal val UpdatePhase.heroRing: HeroRing
    get() = when (this) {
        UpdatePhase.Checking, is UpdatePhase.Installing -> HeroRing.Busy
        is UpdatePhase.Downloading -> HeroRing.Progress(progress)
        UpdatePhase.UpToDate -> HeroRing.Progress(1f)
        else -> HeroRing.Idle
    }

/** "4,2" for 4 400 000 bytes. */
internal fun megabytes(bytes: Long): String = "%.1f".format(bytes / 1_048_576.0)
