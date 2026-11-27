import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import release_stories as rs  # noqa: E402

OLD = "# Что нового\n\n## 2026-11-25\n• А\n"
NEW = "# Что нового\n\n## 2026-12-01\n• Б\n• В\n\n## 2026-11-25\n• А\n"


def kt(key):
    return 'object ReleaseStories {\n    const val KEY = "%s"\n}\n' % key


class ReleaseStoriesGateTest(unittest.TestCase):
    def test_no_new_section_needs_no_stories(self):
        ok, _ = rs.check(OLD, OLD, kt("release.2026-11-25"), kt("release.2026-11-25"))
        self.assertTrue(ok)

    def test_new_release_with_old_key_fails(self):
        ok, message = rs.check(OLD, NEW, kt("release.2026-11-25"), kt("release.2026-11-25"))
        self.assertFalse(ok)
        self.assertIn("2026-12-01", message)

    def test_new_release_with_new_key_passes(self):
        ok, _ = rs.check(OLD, NEW, kt("release.2026-11-25"), kt("release.2026-12-01"))
        self.assertTrue(ok)

    def test_items_and_art_names(self):
        self.assertEqual(["Б", "В"], rs.items("• Б\n• В\n\nтекст"))
        self.assertEqual(["debts", "fan"], rs.art_names("X = 1\nSTORIES = {\n    'debts': [\n    ],\n    'fan': [\n    ],\n}\n"))

    def test_key_is_read(self):
        self.assertEqual("release.2026-11-25", rs.story_key(kt("release.2026-11-25")))


if __name__ == "__main__":
    unittest.main()
