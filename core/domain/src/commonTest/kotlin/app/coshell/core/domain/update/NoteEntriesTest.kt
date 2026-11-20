package app.coshell.core.domain.update

import kotlin.test.Test
import kotlin.test.assertEquals

class NoteEntriesTest {
    @Test
    fun pointsAreNewUnlessMarkedAsFixed() {
        val notes = "• Новая шторка ввода.\n• Исправлено: курс на тёмной теме.\n— Счета группами"
        assertEquals(
            listOf(
                NoteEntry(NoteKind.New, "Новая шторка ввода."),
                NoteEntry(NoteKind.Fixed, "Курс на тёмной теме."),
                NoteEntry(NoteKind.New, "Счета группами"),
            ),
            noteEntries(notes),
        )
    }

    @Test
    fun prefixIsCaseInsensitiveAndOtherLanguagesWork() {
        assertEquals(NoteKind.Fixed, noteEntries("• fixed: the rate").single().kind)
        assertEquals(NoteKind.Fixed, noteEntries("• Corregido: el tipo").single().kind)
    }

    @Test
    fun aBarePrefixKeepsTheTextAndOldNotesStayNew() {
        assertEquals(NoteEntry(NoteKind.Fixed, "Исправлено:"), noteEntries("• Исправлено:").single())
        assertEquals(emptyList(), noteEntries(""))
        assertEquals(NoteKind.New, noteEntries("Без маркера").single().kind)
    }

    @Test
    fun fixedTextStartsWithACapitalNextToTheLabel() {
        assertEquals("В светлой теме нет чёрточки.", noteEntries("• Исправлено: в светлой теме нет чёрточки.").single().text)
        assertEquals("The rate", noteEntries("• Fixed: the rate").single().text)
    }
}
