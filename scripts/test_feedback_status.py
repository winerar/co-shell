"""Unit tests for feedback_status.py: python3 -m unittest discover -s scripts -p 'test_*.py'"""
import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(__file__))
import feedback_status as s  # noqa: E402
import feedback_to_issues as f  # noqa: E402

NOW = "2026-11-11T12:00:00Z"
RELEASES = [
    {"tag_name": "build-250", "published_at": "2026-11-13T10:00:00Z"},
    {"tag_name": "build-248", "published_at": "2026-11-12T10:00:00Z"},
    {"tag_name": "build-247", "published_at": "2026-11-10T14:55:15Z"},
    {"tag_name": "build-249", "published_at": "2026-11-12T11:00:00Z", "draft": True},
]


def issue(state="open", labels=(), reason=None, closed_at=None):
    return {"state": state, "state_reason": reason, "closed_at": closed_at, "created_at": "2026-11-10T15:46:10Z",
            "labels": [{"name": n} for n in labels]}


class FeedbackStatusTest(unittest.TestCase):
    def test_status_follows_labels_and_closing(self):
        self.assertEqual(s.issue_status(issue(labels=["status:triage"])), "received")
        self.assertEqual(s.issue_status(issue(labels=["status:next"])), "received")
        self.assertEqual(s.issue_status(issue(labels=["status:in-progress"])), "in_progress")
        self.assertEqual(s.issue_status(issue(labels=["status:backlog"])), "later")
        self.assertEqual(s.issue_status(issue("closed", reason="completed")), "done")
        self.assertEqual(s.issue_status(issue("closed", reason="not_planned")), "closed")
        self.assertEqual(s.issue_status(issue("closed", reason="duplicate")), "closed")

    def test_done_gets_the_first_release_after_closing(self):
        self.assertEqual(s.release_build("2026-11-11T11:22:44Z", RELEASES), 248)
        self.assertIsNone(s.release_build("2026-11-14T00:00:00Z", RELEASES))

    def test_fields_remember_when_work_started(self):
        first = s.status_fields(issue(labels=["status:in-progress"]), RELEASES, {}, NOW)
        self.assertEqual(first, {"status": "in_progress", "issueAt": "2026-11-10T15:46:10Z", "inProgressAt": NOW})
        done = s.status_fields(issue("closed", reason="completed", closed_at="2026-11-11T11:22:44Z"), RELEASES, first, "later")
        self.assertEqual(done["inProgressAt"], NOW)
        self.assertEqual((done["status"], done["doneAt"], done["doneBuild"]), ("done", "2026-11-11T11:22:44Z", 248))

    def test_only_changed_fields_are_written_and_settled_skipped(self):
        doc = {"status": "received", "issueAt": "2026-11-10T15:46:10Z"}
        self.assertEqual(s.changed(doc, s.status_fields(issue(), RELEASES, doc, NOW)), {})
        self.assertTrue(s.settled({"status": "done", "doneBuild": 248}))
        self.assertFalse(s.settled({"status": "done"}))
        self.assertTrue(s.settled({"status": "closed"}))

    def test_sync_patches_changed_documents(self):
        doc = {"name": "projects/p/databases/(default)/documents/feedback/abc", "fields": {
            "processed": {"booleanValue": True}, "issueNumber": {"integerValue": "157"},
            "status": {"stringValue": "received"}, "issueAt": {"timestampValue": "2026-11-10T15:46:10Z"}}}
        calls = []

        def request(method, url, token, body=None, headers=None):
            calls.append((method, url, body))
            if url.endswith("/releases?per_page=100"):
                return RELEASES
            if url.endswith(":runQuery"):
                return [{"document": doc}, {"readTime": "x"}]
            if url.endswith("/issues/157"):
                return issue(labels=["status:in-progress"])
            return None

        self.assertEqual(s.sync_statuses(request, "p", "t", "o/r", "g", f.decode_document, NOW), 1)
        method, url, body = calls[-1]
        self.assertEqual(method, "PATCH")
        self.assertIn("updateMask.fieldPaths=status", url)
        self.assertEqual(body["fields"]["status"], {"stringValue": "in_progress"})
        self.assertEqual(body["fields"]["inProgressAt"], {"timestampValue": NOW})


if __name__ == "__main__":
    unittest.main()
