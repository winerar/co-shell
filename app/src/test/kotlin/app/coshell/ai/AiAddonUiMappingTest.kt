package app.coshell.ai

import org.junit.Assert.assertEquals
import org.junit.Test

/** What the «ИИ на телефоне» screen is told (#587): the phase as the stage, and a weak phone as «не подойдёт» (poll 2026-11-21, «А»). */
class AiAddonUiMappingTest {
    @Test
    fun `phases map to stages and a measured bar`() {
        assertEquals(AiAddonStage.OFF, AiAddonState().toUi(false).stage)
        assertEquals(AiAddonStage.STEP_ADDON, AiAddonState(AiAddonPhase.InstallingAddon(null)).toUi(false).stage)
        val model = AiAddonState(AiAddonPhase.DownloadingModel(1_060_000_000, 2_500_000_000)).toUi(false)
        assertEquals(AiAddonStage.STEP_MODEL, model.stage)
        assertEquals(1_060L, model.doneMb)
        assertEquals(2_500L, model.totalMb)
        assertEquals(0.424f, model.progress!!, 0.001f)
        assertEquals(AiAddonStage.ON, AiAddonState(AiAddonPhase.On).toUi(false).stage)
    }

    @Test
    fun `the add-on step counts megabytes too, and what is left`() {
        val addon = AiAddonState(AiAddonPhase.InstallingAddon(0.4f, 20_000_000)).toUi(false)
        assertEquals(8L, addon.doneMb)
        assertEquals(20L, addon.totalMb)
        assertEquals(12L, addon.leftMb)
        assertEquals(1_440L, AiAddonState(AiAddonPhase.DownloadingModel(1_060_000_000, 2_500_000_000)).toUi(false).leftMb)
        assertEquals(0L, AiAddonState(AiAddonPhase.InstallingAddon(null, 20_000_000)).toUi(false).totalMb)
    }

    @Test
    fun `a size not known yet has no bar`() {
        assertEquals(null, AiAddonState(AiAddonPhase.DownloadingModel(0, 0)).toUi(false).progress)
    }

    @Test
    fun `under 6 GB the off screen says the phone does not fit, and never asks about the download`() {
        val weak = AiAddonState().toUi(askData = true).forMemory(4_000)
        assertEquals(AiAddonStage.UNSUPPORTED, weak.stage)
        assertEquals(4, weak.memoryGb)
        assertEquals(false, weak.askData)
    }

    @Test
    fun `a 6 GB phone reports about 5,6 GB and is fine, and a model already on a weak phone stays on`() {
        assertEquals(AiAddonStage.OFF, AiAddonState().toUi(false).forMemory(5_700).stage)
        assertEquals(AiAddonStage.ON, AiAddonState(AiAddonPhase.On).toUi(false).forMemory(3_000).stage)
    }
}
