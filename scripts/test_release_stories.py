import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import release_stories as rs  # noqa: E402

OLD = "# Что нового\n\n## 2026-11-25\n• А\n"
NEW = ("# Что нового\n\n## 2026-12-01\n• Долги <!-- story: debts -->\n• Цели и «Что если» <!-- story: goals, debts -->\n"
       "• Быстрее списки <!-- story: none -->\n• Исправлено: подпись\n\n## 2026-11-25\n• А\n")
ART = "X = 1\nSTORIES = {\n    'debts': [\n    ],\n    'goals': [\n    ],\n}\n"
STRINGS = ('<string name="release_debts_title">Долги</string><string name="release_debts_text">Ещё → Долги.</string>'
           '<string name="release_goals_title">Цели</string><string name="release_goals_text">Ещё → Цели.</string>')


def files(whats_new=NEW, strings=STRINGS, art=ART, kotlin=None, drawables=True):
    heading = rs.top_section(whats_new)[0]
    data = {rs.WHATS_NEW: whats_new, rs.STRINGS: strings, rs.ART: art,
            rs.STORIES: kotlin if kotlin is not None else "object X {\n%s\n}\n" % rs.render(["debts", "goals"], rs.key_for(heading))}
    if drawables:
        data.update({rs.DRAWABLE % (i, layer): "x" for i in ("debts", "goals") for layer in rs.LAYERS})
    return data.get


class ReleaseStoriesTest(unittest.TestCase):
    def test_released_section_needs_nothing(self):
        self.assertEqual([], rs.problems(OLD, files(whats_new=OLD, kotlin="")))

    def test_ready_set_passes(self):
        self.assertEqual([], rs.problems(OLD, files()))

    def test_ids_in_order_without_repeats_and_none_or_fix_give_no_story(self):
        self.assertEqual(["debts", "goals"], rs.story_ids(rs.top_section(NEW)[1]))
        self.assertEqual([], rs.bullet_ids("Исправлено: подпись"))
        self.assertEqual([], rs.bullet_ids("Быстрее <!-- story: none -->"))
        self.assertIsNone(rs.bullet_ids("Новое без метки"))

    def test_missing_marker_fails(self):
        found = rs.problems(OLD, files(whats_new=NEW.replace("Долги <!-- story: debts -->", "Долги")))
        self.assertTrue(any("no story marker" in p for p in found))

    def test_missing_strings_scheme_and_drawables_fail(self):
        found = rs.problems(OLD, files(strings="", art="STORIES = {\n}\n", drawables=False))
        self.assertTrue(any("strings" in p for p in found))
        self.assertTrue(any("no scheme" in p for p in found))

    def test_long_words_fail(self):
        long = STRINGS.replace("Ещё → Долги.", "д" * (rs.TEXT_MAX + 1))
        self.assertTrue(any("text is" in p for p in rs.problems(OLD, files(strings=long))))

    def test_stale_kotlin_fails(self):
        found = rs.problems(OLD, files(kotlin="object X {\n%s\n}\n" % rs.render(["debts"], "release.old")))
        self.assertTrue(any("write" in p for p in found))

    def test_key_is_stable_per_section_and_new_for_the_next(self):
        self.assertEqual(rs.key_for("2026-12-01"), rs.key_for("2026-12-01"))
        self.assertNotEqual(rs.key_for("2026-12-01"), rs.key_for("2026-12-01, вечер"))
        self.assertTrue(rs.key_for("2026-12-01, вечер").startswith("release.2026-12-01-"))

    def test_markers_are_stripped_for_people(self):
        self.assertEqual("• Долги\n• Б", rs.strip_markers("• Долги <!-- story: debts -->\n• Б"))

    def test_art_names(self):
        self.assertEqual(["debts", "goals"], rs.art_names(ART))


if __name__ == "__main__":
    unittest.main()
