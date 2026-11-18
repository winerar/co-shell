"""python3 -m unittest scripts/ci/test_release_needed.py — when the weekly release is skipped."""
import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from release_needed import release_needed  # noqa: E402


class ReleaseNeededTest(unittest.TestCase):
    def test_nothing_changed(self):
        self.assertFalse(release_needed([]))
        self.assertFalse(release_needed(["", "\n"]))

    def test_docs_only(self):
        self.assertFalse(release_needed(["docs/STATUS.md", "README.md", "scripts/agent/agent.sh"]))

    def test_code_changed(self):
        self.assertTrue(release_needed(["docs/STATUS.md", "app/src/main/kotlin/A.kt"]))

    def test_ci_scripts_count_as_code(self):
        self.assertTrue(release_needed([".github/workflows/ci.yml"]))
        self.assertTrue(release_needed(["scripts/ci/automerge.sh"]))


if __name__ == "__main__":
    unittest.main()
