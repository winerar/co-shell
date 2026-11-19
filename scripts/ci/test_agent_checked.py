"""python3 -m unittest scripts/ci/test_agent_checked.py — PR CI skips Gradle only for code the agent checked."""
import os
import subprocess
import tempfile
import unittest

SCRIPT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "agent-checked.sh")
CHECKED = "Agent-Check: full"


class AgentCheckedTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.repo = self.tmp.name
        self.env = dict(os.environ, GIT_AUTHOR_NAME="t", GIT_AUTHOR_EMAIL="t@t",
                        GIT_COMMITTER_NAME="t", GIT_COMMITTER_EMAIL="t@t")
        self.git("init", "-q", "-b", "dev")
        self.commit("app.kt")

    def tearDown(self):
        self.tmp.cleanup()

    def git(self, *args):
        return subprocess.run(["git", *args], cwd=self.repo, env=self.env, check=True,
                              capture_output=True, text=True).stdout.strip()

    def commit(self, name, trailer=None):
        path = os.path.join(self.repo, name)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "a") as f:
            f.write(name + "\n")
        self.git("add", name)
        msg = ["-m", name] + (["-m", trailer] if trailer else [])
        self.git("commit", "-q", *msg)

    def pr(self, *names, trailer=CHECKED):
        """Branch `pr` from dev with the given files, last commit carries `trailer`; back on dev."""
        self.git("checkout", "-q", "-b", "pr")
        for i, name in enumerate(names):
            self.commit(name, trailer if i == len(names) - 1 else None)
        self.git("checkout", "-q", "dev")

    def merge_ref(self):
        """Like refs/pull/N/merge: dev (first parent) + pr (second parent), checked out detached."""
        self.git("checkout", "-q", "--detach", "dev")
        self.git("merge", "-q", "--no-ff", "-m", "merge", "pr")

    def run_script(self):
        return subprocess.run([SCRIPT], cwd=self.repo, env=self.env, capture_output=True, text=True).returncode

    def test_checked_head_on_unchanged_base_is_skipped(self):
        self.pr("feature.kt")
        self.merge_ref()
        self.assertEqual(self.run_script(), 0)

    def test_head_without_trailer_builds(self):
        self.pr("feature.kt", trailer=None)
        self.merge_ref()
        self.assertEqual(self.run_script(), 1)

    def test_quick_check_trailer_builds(self):
        self.pr("feature.kt", trailer="Agent-Check: quick")
        self.merge_ref()
        self.assertEqual(self.run_script(), 1)

    def test_base_with_new_code_builds(self):
        self.pr("feature.kt")
        self.commit("other.kt")  # dev moved on in code after the agent's check
        self.merge_ref()
        self.assertEqual(self.run_script(), 1)

    def test_base_with_only_docs_is_skipped(self):
        self.pr("feature.kt")
        self.commit("docs/STATUS.md")
        self.commit("scripts/agent/x.sh")
        self.merge_ref()
        self.assertEqual(self.run_script(), 0)

    def test_plain_commit_with_trailer_is_skipped(self):
        self.commit("feature.kt", CHECKED)
        self.assertEqual(self.run_script(), 0)


if __name__ == "__main__":
    unittest.main()
