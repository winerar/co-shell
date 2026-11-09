package app.coshell.update

import app.coshell.core.designsystem.components.HeroRing
import app.coshell.core.domain.update.AppRelease
import app.coshell.update.components.UpdateHeroStep
import app.coshell.update.components.heroRing
import app.coshell.update.components.heroStep
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateHeroStepTest {
    private val release = AppRelease(2, "0.2.0", "https://x/a.apk", "0".repeat(64), 1_048_576, "")

    @Test
    fun `ring spins while the amount of work is unknown`() {
        assertEquals(HeroRing.Busy, UpdatePhase.Checking.heroRing)
        assertEquals(HeroRing.Busy, UpdatePhase.Installing(release).heroRing)
    }

    @Test
    fun `ring fills with the download and is full when up to date`() {
        assertEquals(HeroRing.Progress(0.4f), UpdatePhase.Downloading(release, 0.4f).heroRing)
        assertEquals(HeroRing.Progress(1f), UpdatePhase.UpToDate.heroRing)
        assertEquals(HeroRing.Idle, UpdatePhase.Available(release).heroRing)
        assertEquals(HeroRing.Idle, UpdatePhase.Failed(UpdateError.Check).heroRing)
    }

    @Test
    fun `progress updates keep the same step so the centre does not re-animate`() {
        assertEquals(
            UpdatePhase.Downloading(release, 0.1f).heroStep,
            UpdatePhase.Downloading(release, 0.9f).heroStep,
        )
        assertEquals(UpdateHeroStep.Failed, UpdatePhase.Failed(UpdateError.Download, release).heroStep)
    }
}
