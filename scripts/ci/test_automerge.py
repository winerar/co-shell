"""python3 -m unittest scripts/ci/test_automerge.py — when automerge.sh merges an agent PR into dev."""
import json
import os
import subprocess
import tempfile
import unittest

SCRIPT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "automerge.sh")

# Fake gh: answers from $PR_JSON / $RUNS_JSON, logs every call to $GH_LOG.
FAKE_GH = r"""#!/usr/bin/env bash
echo "$*" >> "$GH_LOG"
case "$1 $2" in
  "pr view")
    q=""; prev=""; for a in "$@"; do [ "$prev" = -q ] && q=$a; prev=$a; done
    if [ -n "$q" ]; then jq -r "$q" <<<"$PR_JSON"; else printf '%s' "$PR_JSON"; fi ;;
  "run list") printf '%s' "$RUNS_JSON" ;;
  *) : ;;
esac
"""


class AutomergeTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        bin_dir = os.path.join(self.tmp.name, "bin")
        os.makedirs(bin_dir)
        with open(os.path.join(bin_dir, "gh"), "w") as f:
            f.write(FAKE_GH)
        os.chmod(os.path.join(bin_dir, "gh"), 0o755)
        self.log = os.path.join(self.tmp.name, "gh.log")
        self.pr = {
            "number": 7, "state": "OPEN", "isDraft": False, "baseRefName": "dev",
            "headRefName": "ccr-abc", "headRefOid": "sha1", "labels": [], "mergeable": "MERGEABLE",
            "comments": [], "closingIssuesReferences": [],
        }
        self.env = dict(os.environ, PATH=bin_dir + os.pathsep + os.environ["PATH"], GH_LOG=self.log,
                        GH_REPO="o/r", RUNS_JSON="[]")
        self.env.pop("CI_GREEN_SHA", None)

    def tearDown(self):
        self.tmp.cleanup()

    def run_script(self, **env):
        e = dict(self.env, PR_JSON=json.dumps(self.pr), **env)
        out = subprocess.run(["bash", SCRIPT, "ccr-abc"], env=e, capture_output=True, text=True)
        self.assertEqual(out.returncode, 0, out.stderr)
        calls = ""
        if os.path.exists(self.log):
            with open(self.log) as f:
                calls = f.read()
        return out.stdout, calls

    def test_waits_without_a_green_run(self):
        stdout, calls = self.run_script(RUNS_JSON='[{"status":"in_progress","conclusion":""}]')
        self.assertIn("skip", stdout)
        self.assertNotIn("pr merge", calls)

    def test_merges_once_a_green_run_exists(self):
        _, calls = self.run_script(RUNS_JSON='[{"status":"completed","conclusion":"success"}]')
        self.assertIn("pr merge 7", calls)

    def test_green_ci_run_of_the_head_merges_without_looking_up_runs(self):
        _, calls = self.run_script(CI_GREEN_SHA="sha1")
        self.assertIn("pr merge 7", calls)
        self.assertNotIn("run list", calls)

    def test_green_flag_for_another_commit_does_not_count(self):
        stdout, calls = self.run_script(CI_GREEN_SHA="old")
        self.assertIn("skip", stdout)
        self.assertNotIn("pr merge", calls)

    def test_draft_is_never_merged(self):
        self.pr["isDraft"] = True
        _, calls = self.run_script(CI_GREEN_SHA="sha1")
        self.assertNotIn("pr merge", calls)


if __name__ == "__main__":
    unittest.main()
