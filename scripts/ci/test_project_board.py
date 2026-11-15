"""python3 -m unittest scripts/ci/test_project_board.py"""
import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(__file__))
from project_board import DONE, decide, decide_type, fatal_errors, status_labels_at, type_of  # noqa: E402


def issue(*labels, open=True, at="2026-11-10T10:00:00Z"):
    return {"open": open, "labels": list(labels), "labels_at": at}


def item(status, at="2026-11-10T09:00:00Z"):
    return {"status": status, "updated_at": at}


class DecideTest(unittest.TestCase):
    def test_new_issue_goes_to_its_column(self):
        self.assertEqual(decide(issue("type:feature", "status:next"), None), ("Очередь", None, False))

    def test_issue_without_status_goes_to_triage(self):
        self.assertEqual(decide(issue("source:app"), None), ("Разобрать", None, False))

    def test_in_sync_does_nothing(self):
        self.assertEqual(decide(issue("status:next"), item("Очередь")), (None, None, False))

    def test_label_changed_later_moves_card(self):
        self.assertEqual(decide(issue("status:in-progress"), item("Очередь")), ("В работе", None, False))

    def test_card_moved_later_changes_label(self):
        moved = item("Ждёт владельца", at="2026-11-10T11:00:00Z")
        self.assertEqual(decide(issue("status:next"), moved), (None, "status:needs-owner", False))

    def test_card_bumped_right_after_label_change_keeps_label(self):
        bumped = item("Разобрать", at="2026-11-10T10:00:03Z")
        self.assertEqual(decide(issue("status:backlog"), bumped), ("Бэклог", None, False))

    def test_card_moved_to_done_closes_issue(self):
        self.assertEqual(decide(issue("status:next"), item(DONE, at="2026-11-10T11:00:00Z")), (None, None, True))

    def test_card_without_status_takes_label(self):
        self.assertEqual(decide(issue("status:backlog"), item(None, at="2026-11-10T11:00:00Z")), ("Бэклог", None, False))

    def test_unknown_column_is_overwritten(self):
        self.assertEqual(decide(issue("status:next"), item("Todo", at="2026-11-10T11:00:00Z")), ("Очередь", None, False))

    def test_closed_issue_goes_to_done(self):
        self.assertEqual(decide(issue(open=False), item("В работе")), (DONE, None, False))
        self.assertEqual(decide(issue(open=False), item(DONE)), (None, None, False))

    def test_unknown_label_time_keeps_label(self):
        self.assertEqual(decide(issue("status:backlog", at=None), item("В работе")), ("Бэклог", None, False))

    def test_status_labels_at_takes_last_status_event(self):
        events = [
            {"event": "labeled", "label": {"name": "status:next"}, "created_at": "2026-11-10T10:00:00Z"},
            {"event": "unlabeled", "label": {"name": "status:next"}, "created_at": "2026-11-15T18:00:00Z"},
            {"event": "labeled", "label": {"name": "design"}, "created_at": "2026-11-15T19:00:00Z"},
            {"event": "renamed", "created_at": "2026-11-15T20:00:00Z"},
        ]
        self.assertEqual(status_labels_at(events), "2026-11-15T18:00:00Z")
        self.assertIsNone(status_labels_at([]))


class TypeTest(unittest.TestCase):
    def test_type_label_maps_to_option(self):
        self.assertEqual(type_of(["status:next", "type:story", "design"]), "История")
        self.assertEqual(type_of(["type:bug", "source:app"]), "Баг")
        self.assertEqual(type_of(["type:question", "status:needs-owner"]), "Вопрос")
        self.assertEqual(type_of(["type:owner"]), "Владельцу")

    def test_feedback_without_type_has_none(self):
        self.assertIsNone(type_of(["source:app", "status:triage"]))

    def test_type_follows_label(self):
        self.assertEqual(decide_type(issue("type:feature"), None), (True, "Фича"))
        self.assertEqual(decide_type(issue("type:feature"), "Фича"), (False, "Фича"))
        self.assertEqual(decide_type(issue("type:bug"), "Фича"), (True, "Баг"))

    def test_type_label_removed_clears_field(self):
        self.assertEqual(decide_type(issue("status:triage"), "Баг"), (True, None))

    def test_closed_issue_keeps_type(self):
        self.assertEqual(decide_type(issue(open=False), "Баг"), (False, "Баг"))


class FatalErrorsTest(unittest.TestCase):
    def test_missing_type_field_is_not_fatal(self):
        error = {"type": "NOT_FOUND", "path": ["user", "projectV2", "kind"],
                 "message": "Could not resolve to a Unions::ProjectV2FieldConfiguration with the name Тип"}
        self.assertEqual(fatal_errors([error]), [])

    def test_missing_project_is_fatal(self):
        error = {"type": "NOT_FOUND", "path": ["user", "projectV2"]}
        self.assertEqual(fatal_errors([error]), [error])

    def test_other_errors_are_fatal(self):
        error = {"type": "FORBIDDEN", "path": ["user", "projectV2", "kind"]}
        self.assertEqual(fatal_errors([error]), [error])

    def test_no_errors(self):
        self.assertEqual(fatal_errors(None), [])


if __name__ == "__main__":
    unittest.main()
