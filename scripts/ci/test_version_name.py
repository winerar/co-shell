"""python3 -m unittest scripts/ci/test_version_name.py — release version = merges into main (#268)."""
import os
import subprocess
import sys
import tempfile
import unittest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import version_name  # noqa: E402

SCRIPT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "version_name.py")


class VersionNameTest(unittest.TestCase):
    def test_minor_is_merges_minus_offset(self):
        self.assertEqual(version_name.version_name(0, 0, 37), "0.37")
        self.assertEqual(version_name.version_name(1, 40, 42), "1.2")
        self.assertEqual(version_name.version_name(1, 40, 40), "1.0")

    def test_minor_never_negative(self):
        self.assertEqual(version_name.version_name(1, 40, 3), "1.0")

    def test_counts_first_parent_merges_of_head(self):
        with tempfile.TemporaryDirectory() as repo:
            env = dict(os.environ, GIT_AUTHOR_NAME="t", GIT_AUTHOR_EMAIL="t@t",
                       GIT_COMMITTER_NAME="t", GIT_COMMITTER_EMAIL="t@t")

            def git(*args):
                subprocess.run(["git", *args], cwd=repo, env=env, check=True, capture_output=True)

            with open(os.path.join(repo, "version.properties"), "w") as f:
                f.write("# comment\nmajor=0\nminorOffset=1\n")
            git("init", "-q", "-b", "main")
            git("add", ".")
            git("commit", "-q", "-m", "base")
            for i in range(3):  # three releases: a commit on dev merged into main
                git("checkout", "-q", "-b", f"dev{i}")
                git("commit", "-q", "--allow-empty", "-m", f"work {i}")
                git("checkout", "-q", "main")
                git("merge", "-q", "--no-ff", f"dev{i}", "-m", f"release {i}")
            out = subprocess.run([sys.executable, SCRIPT, repo], check=True, capture_output=True, text=True).stdout
            self.assertEqual(out.strip(), "0.2")


if __name__ == "__main__":
    unittest.main()
