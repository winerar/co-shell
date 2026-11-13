"""python3 -m unittest scripts/ci/test_rules_changed.py — CI tests and deploys rules only when they change."""
import os
import subprocess
import tempfile
import unittest

SCRIPT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "rules-changed.sh")


class RulesChangedTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.repo = self.tmp.name
        self.env = dict(os.environ, GIT_AUTHOR_NAME="t", GIT_AUTHOR_EMAIL="t@t",
                        GIT_COMMITTER_NAME="t", GIT_COMMITTER_EMAIL="t@t")
        self.git("init", "-q", "-b", "dev")
        self.commit("README.md")

    def tearDown(self):
        self.tmp.cleanup()

    def git(self, *args):
        subprocess.run(["git", *args], cwd=self.repo, env=self.env, check=True, capture_output=True)

    def commit(self, path):
        full = os.path.join(self.repo, path)
        os.makedirs(os.path.dirname(full), exist_ok=True)
        with open(full, "a") as f:
            f.write("x")
        self.git("add", path)
        self.git("commit", "-q", "-m", path)

    def changed(self, base="HEAD^1"):
        return subprocess.run([SCRIPT, base], cwd=self.repo, env=self.env, capture_output=True).returncode == 0

    def test_rules_files_count(self):
        for path in ["firebase/firestore.rules", "firebase/database.rules.json", "firebase/firebase.json",
                     "firebase/rules-test/rules.test.js", "scripts/ci/deploy-rules.sh"]:
            with self.subTest(path=path):
                self.commit(path)
                self.assertTrue(self.changed())

    def test_other_files_do_not(self):
        for path in ["app/Main.kt", "docs/setup/sync.md", "firebase/README.md", "scripts/ci/other.sh"]:
            with self.subTest(path=path):
                self.commit(path)
                self.assertFalse(self.changed())

    def test_pr_range_sees_every_commit_of_the_branch(self):
        self.git("checkout", "-q", "-b", "pr")
        self.commit("firebase/firestore.rules")
        self.commit("app/Main.kt")
        self.assertTrue(self.changed("dev...HEAD"))
        self.assertFalse(self.changed("HEAD^1"))


if __name__ == "__main__":
    unittest.main()
