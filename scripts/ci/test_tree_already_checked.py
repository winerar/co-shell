"""python3 -m unittest scripts/ci/test_tree_already_checked.py — dev skips Gradle only for a checked tree."""
import os
import subprocess
import tempfile
import unittest

SCRIPT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "tree-already-checked.sh")


class TreeAlreadyCheckedTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.repo = os.path.join(self.tmp.name, "repo")
        bin_dir = os.path.join(self.tmp.name, "bin")
        os.makedirs(self.repo)
        os.makedirs(bin_dir)
        # Fake gh: prints the run id from $GREEN_RUN (empty → no green run).
        with open(os.path.join(bin_dir, "gh"), "w") as f:
            f.write('#!/bin/sh\nprintf "%s" "$GREEN_RUN"\n')
        os.chmod(os.path.join(bin_dir, "gh"), 0o755)
        self.env = dict(os.environ, PATH=bin_dir + os.pathsep + os.environ["PATH"], GREEN_RUN="42",
                        GIT_AUTHOR_NAME="t", GIT_AUTHOR_EMAIL="t@t", GIT_COMMITTER_NAME="t", GIT_COMMITTER_EMAIL="t@t")
        self.git("init", "-q", "-b", "dev")
        self.commit("base.txt")

    def tearDown(self):
        self.tmp.cleanup()

    def git(self, *args):
        return subprocess.run(["git", *args], cwd=self.repo, env=self.env, check=True,
                              capture_output=True, text=True).stdout.strip()

    def commit(self, name):
        with open(os.path.join(self.repo, name), "w") as f:
            f.write(name)
        self.git("add", name)
        self.git("commit", "-q", "-m", name)

    def branch_with(self, name):
        self.git("checkout", "-q", "-b", "pr")
        self.commit(name)
        self.git("checkout", "-q", "dev")

    def run_script(self):
        return subprocess.run([SCRIPT], cwd=self.repo, env=self.env, capture_output=True, text=True).returncode

    def test_merge_of_up_to_date_green_branch_is_skipped(self):
        self.branch_with("feature.txt")
        self.git("merge", "-q", "--no-ff", "-m", "merge", "pr")
        self.assertEqual(self.run_script(), 0)

    def test_no_green_run_on_head_builds(self):
        self.branch_with("feature.txt")
        self.git("merge", "-q", "--no-ff", "-m", "merge", "pr")
        self.env["GREEN_RUN"] = ""
        self.assertEqual(self.run_script(), 1)

    def test_merge_bringing_other_changes_builds(self):
        self.branch_with("feature.txt")
        self.commit("other.txt")  # dev moved on: the merge tree differs from the PR head
        self.git("merge", "-q", "--no-ff", "-m", "merge", "pr")
        self.assertEqual(self.run_script(), 1)

    def test_plain_commit_builds(self):
        self.commit("direct.txt")
        self.assertEqual(self.run_script(), 1)


if __name__ == "__main__":
    unittest.main()
