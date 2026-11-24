"""python3 -m unittest scripts/ci/test_task_status.py — which issues a PR moves on the board."""
import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from task_status import mentions, plan_merged, plan_ready  # noqa: E402


def issue(n, status=None, state="OPEN", is_pr=False, extra=()):
    labels = ["type:feature", *extra] + ([status] if status else [])
    return {"number": n, "state": state, "labels": labels, "is_pr": is_pr}


class MentionsTest(unittest.TestCase):
    def test_finds_numbers_in_title_and_body(self):
        text = "Add debts (#552) and the PIN hint (#602)\nRelated: #584, Fixes #552"
        self.assertEqual(mentions(text), [552, 602, 584])

    def test_ignores_anchors_urls_and_entities(self):
        text = "see https://x.org/a#12 and a/b#7 and &#39; colour #fff"
        self.assertEqual(mentions(text), [])

    def test_empty_body(self):
        self.assertEqual(mentions(None), [])


class ReadyTest(unittest.TestCase):
    def test_takes_queue_backlog_triage_and_no_status(self):
        moves = plan_ready([issue(1, "status:next"), issue(2, "status:backlog"), issue(3, "status:triage"), issue(4)])
        self.assertEqual([m[0] for m in moves], [1, 2, 3, 4])
        self.assertTrue(all(m[2] == "status:in-progress" for m in moves))

    def test_leaves_owner_questions_closed_issues_prs_and_in_progress(self):
        moves = plan_ready([
            issue(1, "status:needs-owner"), issue(2, "status:next", state="CLOSED"),
            issue(3, "status:next", is_pr=True), issue(4, "status:in-progress"),
        ])
        self.assertEqual(moves, [])


class MergedTest(unittest.TestCase):
    def test_returns_unfinished_in_progress_to_queue(self):
        moves = plan_merged([issue(5, "status:in-progress")], fixed=set(), named_elsewhere=set())
        self.assertEqual(moves, [(5, "status:in-progress", "status:next")])

    def test_keeps_fixed_and_issues_of_other_open_prs(self):
        issues = [issue(5, "status:in-progress"), issue(6, "status:in-progress")]
        self.assertEqual(plan_merged(issues, fixed={5}, named_elsewhere={6}), [])

    def test_does_not_touch_other_statuses(self):
        issues = [issue(7, "status:needs-owner"), issue(8, "status:next"), issue(9, "status:in-progress", is_pr=True)]
        self.assertEqual(plan_merged(issues, fixed=set(), named_elsewhere=set()), [])


if __name__ == "__main__":
    unittest.main()
