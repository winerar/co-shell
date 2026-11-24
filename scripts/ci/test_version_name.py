"""python3 -m unittest scripts/ci/test_version_name.py — release version = YY.M.N (releases of the month)."""
import os
import subprocess
import sys
import tempfile
import unittest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import version_name  # noqa: E402

SCRIPT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "version_name.py")


class VersionNameTest(unittest.TestCase):
    def test_formats_two_digit_year_and_plain_month(self):
        self.assertEqual(version_name.version_name(2026, 12, 1), "26.12.1")
        self.assertEqual(version_name.version_name(2027, 1, 3), "27.1.3")

    def test_number_is_at_least_one(self):
        self.assertEqual(version_name.version_name(2026, 12, 0), "26.12.1")

    def test_counts_merges_of_the_head_month_only(self):
        with tempfile.TemporaryDirectory() as repo:
            def git(when, *args):
                env = dict(os.environ, GIT_AUTHOR_NAME="t", GIT_AUTHOR_EMAIL="t@t", GIT_COMMITTER_NAME="t",
                           GIT_COMMITTER_EMAIL="t@t", GIT_AUTHOR_DATE=when, GIT_COMMITTER_DATE=when)
                subprocess.run(["git", *args], cwd=repo, env=env, check=True, capture_output=True)

            def version():
                return subprocess.run([sys.executable, SCRIPT, repo], check=True, capture_output=True,
                                      text=True).stdout.strip()

            def release(n, when):
                git(when, "checkout", "-q", "-b", f"dev{n}")
                git(when, "commit", "-q", "--allow-empty", "-m", f"work {n}")
                git(when, "checkout", "-q", "main")
                git(when, "merge", "-q", "--no-ff", f"dev{n}", "-m", f"release {n}")

            git("2026-11-02T10:00:00Z", "init", "-q", "-b", "main")
            git("2026-11-02T10:00:00Z", "commit", "-q", "--allow-empty", "-m", "base")
            release(1, "2026-11-30T10:00:00Z")
            self.assertEqual(version(), "26.11.1")
            release(2, "2026-12-07T10:00:00Z")
            self.assertEqual(version(), "26.12.1")
            release(3, "2026-12-14T10:00:00Z")  # a weekly release or a hotfix: the next number
            self.assertEqual(version(), "26.12.2")
            release(4, "2027-01-04T10:00:00Z")
            self.assertEqual(version(), "27.1.1")


if __name__ == "__main__":
    unittest.main()
