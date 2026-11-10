import unittest

from release_notes import compose, fixed_issues, merged_prs, top_section

WHATS_NEW = """# Что нового

Пояснение.

## 2026-11-10 · новое
• Пункт один.
• Пункт два.

## 2026-11-09 · старое
• Выпущено.
"""


class TopSectionTest(unittest.TestCase):
    def test_takes_only_the_first_section(self):
        heading, body = top_section(WHATS_NEW)
        self.assertEqual("2026-11-10 · новое", heading)
        self.assertEqual("• Пункт один.\n• Пункт два.", body)

    def test_no_sections(self):
        self.assertEqual((None, ""), top_section("# Что нового\n"))


class MergedPrsTest(unittest.TestCase):
    def test_reads_merge_subjects_oldest_first(self):
        subjects = [
            "Merge pull request #91 from winerar/ccr-77df22f7-ck3efo",
            "Merge remote-tracking branch 'origin/dev' into x",
            "Merge pull request #88 from winerar/claude/lucid",
            "Merge pull request #88 from winerar/claude/lucid",
        ]
        self.assertEqual([88, 91], merged_prs(subjects))


class FixedIssuesTest(unittest.TestCase):
    def test_collects_closing_keywords_once(self):
        bodies = ["Fixes #12, fixes #13", "Closes #12\nresolved #20", None, "see #99"]
        self.assertEqual([12, 13, 20], fixed_issues(bodies))


class ComposeTest(unittest.TestCase):
    def test_full_body(self):
        title, body = compose("2026-11-10 · новое", "• Пункт.", [(88, "Fix rates")], [12])
        self.assertEqual("Release: 2026-11-10 · новое", title)
        self.assertIn("• Пункт.", body)
        self.assertIn("- #88 Fix rates", body)
        self.assertTrue(body.endswith("Fixes #12"))

    def test_without_whats_new(self):
        title, body = compose(None, "", [], [])
        self.assertEqual("Release", title)
        self.assertIn("не обновлён", body)


if __name__ == "__main__":
    unittest.main()
