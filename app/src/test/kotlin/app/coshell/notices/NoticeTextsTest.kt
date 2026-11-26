package app.coshell.notices

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import app.coshell.core.domain.notices.LimitFact
import app.coshell.core.domain.notices.NoticeFacts
import app.coshell.core.domain.notices.NoticeKind
import app.coshell.core.domain.notices.PlannedNotice
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Words, targets and channels of the optional notifications (#745, boards `Not745TextA1/A2`). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru", application = Application::class)
class NoticeTextsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val today = LocalDate(2026, 12, 2)
    private val facts = NoticeFacts(today, true, false, LocalDate(2026, 11, 28), LocalDate(2026, 11, 1), LocalDate(2026, 11, 11), readyDrafts = 4)
    private val limit = LimitFact("food:2026-11", "Продукты", "18 000 ARS", percentLeft = 20, daysLeft = 12)

    private fun content(kind: NoticeKind) = NoticeTexts.of(context, PlannedNotice(kind, "k", limit.takeIf { kind == NoticeKind.LIMIT }), facts)

    @Test fun `evening and comeback open a new entry`() {
        assertEquals(NoticeTarget.NEW_ENTRY, content(NoticeKind.EVENING).target)
        assertEquals(NoticeTarget.NEW_ENTRY, content(NoticeKind.COMEBACK).target)
        assertEquals("Как прошёл день?", content(NoticeKind.EVENING).title)
    }

    @Test fun `limit says what is left and for how long`() {
        val c = content(NoticeKind.LIMIT)
        assertEquals("Продукты: ещё можно 18 000 ARS", c.title)
        assertEquals("Это 20 % лимита на 12 дней.", c.text)
        assertEquals(NoticeTarget.BUDGETS, c.target)
    }

    @Test fun `limit days agree with the number`() {
        fun text(days: Int) = NoticeTexts.of(context, PlannedNotice(NoticeKind.LIMIT, "k", limit.copy(daysLeft = days)), facts).text
        assertTrue(text(1).endsWith("на 1 день."))
        assertTrue(text(3).endsWith("на 3 дня."))
        assertTrue(text(5).endsWith("на 5 дней."))
    }

    @Test fun `recap names the month that ended`() {
        val c = content(NoticeKind.RECAP)
        assertEquals("Итоги ноября готовы", c.title)
        assertEquals(NoticeTarget.RECAP, c.target)
    }

    @Test fun `backup tells how long ago, or that there is no copy yet`() {
        assertEquals("Последняя — 21 день назад.", content(NoticeKind.BACKUP).text)
        val never = NoticeTexts.of(context, PlannedNotice(NoticeKind.BACKUP, "k"), facts.copy(lastBackupDay = null))
        assertTrue(never.text.contains("Копии пока нет"))
        assertEquals(NoticeTarget.BACKUP, never.target)
    }

    @Test fun `drafts say how many are ready`() {
        assertEquals("Готово к записи: 4. Проверьте и запишите.", content(NoticeKind.DRAFTS).text)
        assertEquals(NoticeTarget.DRAFTS, content(NoticeKind.DRAFTS).target)
    }

    @Test fun `no text blames or shouts`() = NoticeKind.entries.forEach {
        val all = content(it).title + content(it).text
        assertFalse(it.key, all.contains("!") || all.contains("превыш") || all.contains("забыли", ignoreCase = true))
    }

    @Test fun `a post is one notification per kind with its own channel`() {
        shadowOf(context as Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        val posts = NoticePosts(context)
        assertTrue(posts.show(NoticeKind.EVENING, content(NoticeKind.EVENING)))
        assertTrue(posts.show(NoticeKind.LIMIT, content(NoticeKind.LIMIT)))
        val manager = context.getSystemService(NotificationManager::class.java)
        assertEquals(2, shadowOf(manager).allNotifications.size)
        assertEquals(setOf("notice_evening", "notice_limit"), shadowOf(manager).allNotifications.map { it.channelId }.toSet())
    }

    @Test fun `a tap carries the target and it is taken only once`() {
        val links = NoticeLinks()
        val intent = Intent().putExtra(NoticeLinks.EXTRA, NoticeTarget.DRAFTS.name)
        assertTrue(links.offer(intent))
        assertEquals(NoticeTarget.DRAFTS, links.target.value)
        assertFalse(links.offer(intent))
        links.consumed()
        assertNull(links.target.value)
    }
}
