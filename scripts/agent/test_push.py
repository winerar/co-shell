"""python3 -m unittest scripts/agent/test_push.py — push.sh pushes to the branch the local one tracks (#460)."""
import os
import subprocess
import tempfile
import unittest

SCRIPT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "push.sh")


class PushTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        root = self.tmp.name
        self.origin = os.path.join(root, "origin.git")
        self.repo = os.path.join(root, "repo")
        self.env = dict(os.environ, GIT_AUTHOR_NAME="t", GIT_AUTHOR_EMAIL="t@t",
                        GIT_COMMITTER_NAME="t", GIT_COMMITTER_EMAIL="t@t", PUSH_REPO=self.repo)
        subprocess.run(["git", "init", "-q", "--bare", "-b", "dev", self.origin], check=True)
        subprocess.run(["git", "clone", "-q", self.origin, self.repo], check=True, capture_output=True)
        self.git("checkout", "-q", "-b", "dev")
        self.commit("a")
        self.git("push", "-q", "origin", "dev")

    def tearDown(self):
        self.tmp.cleanup()

    def git(self, *args):
        return subprocess.run(["git", *args], cwd=self.repo, env=self.env, check=True, capture_output=True, text=True).stdout.strip()

    def commit(self, name):
        with open(os.path.join(self.repo, name), "a") as f:
            f.write("x")
        self.git("add", name)
        self.git("commit", "-q", "-m", name)

    def push(self):
        return subprocess.run(["bash", SCRIPT], cwd=self.repo, env=self.env, capture_output=True, text=True)

    def remote_branches(self):
        return set(self.git("ls-remote", "--heads", "origin").replace("\t", " ").split("\n")) and {
            line.split("refs/heads/")[1] for line in self.git("ls-remote", "--heads", "origin").split("\n")}

    def test_pushes_to_the_tracked_branch_under_another_local_name(self):
        self.git("checkout", "-q", "-b", "ccr-pr")
        self.commit("b")
        self.git("push", "-q", "-u", "origin", "ccr-pr")
        self.git("checkout", "-q", "-b", "p450")
        self.git("branch", "-q", "-u", "origin/ccr-pr")
        self.commit("c")
        result = self.push()
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertEqual({"dev", "ccr-pr"}, self.remote_branches())
        self.assertEqual(self.git("rev-parse", "HEAD"), self.git("rev-parse", "origin/ccr-pr"))

    def test_without_upstream_it_uses_the_local_name(self):
        self.git("checkout", "-q", "-b", "ccr-new")
        self.commit("b")
        result = self.push()
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertEqual({"dev", "ccr-new"}, self.remote_branches())

    def test_a_branch_made_from_dev_is_pushed_under_its_own_name(self):
        self.git("fetch", "-q", "origin")
        self.git("checkout", "-q", "-B", "ccr-start", "origin/dev")  # what `start` does: it tracks origin/dev
        self.commit("b")
        result = self.push()
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertEqual({"dev", "ccr-start"}, self.remote_branches())

    def test_refuses_dev(self):
        self.commit("b")
        result = self.push()
        self.assertEqual(1, result.returncode)
        self.assertIn("refusing", result.stdout)


if __name__ == "__main__":
    unittest.main()
