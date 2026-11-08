"""Unit tests for feedback_to_issues.py: python3 -m unittest discover -s scripts -p 'test_*.py'"""
import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(__file__))
import feedback_to_issues as f  # noqa: E402

DOCUMENT = {
    "name": "projects/p/databases/(default)/documents/feedback/abc123",
    "fields": {
        "text": {"stringValue": "Не сохраняется заметка\n\nПри переводе между счетами"},
        "createdAt": {"timestampValue": "2026-11-08T10:00:00.123Z"},
        "appVersion": {"stringValue": "0.1.0"},
        "versionCode": {"integerValue": "27"},
        "device": {"stringValue": "Google Pixel 8"},
        "os": {"stringValue": "Android 15 (API 35)"},
        "installationId": {"stringValue": "0123456789abcdef"},
        "processed": {"booleanValue": False},
    },
}


class FeedbackToIssuesTest(unittest.TestCase):
    def test_decodes_document(self):
        doc = f.decode_document(DOCUMENT)
        self.assertEqual(doc["versionCode"], 27)
        self.assertIs(doc["processed"], False)
        self.assertEqual(doc["_id"], "abc123")

    def test_title_uses_first_non_empty_line(self):
        self.assertEqual(f.issue_title("\n  Нет итога  \nещё"), "Отзыв: Нет итога")
        self.assertEqual(f.issue_title("   "), "Отзыв: Без текста")

    def test_long_title_is_shortened(self):
        title = f.issue_title("а" * 200)
        self.assertEqual(len(title), len("Отзыв: ") + f.TITLE_LENGTH)
        self.assertTrue(title.endswith("…"))

    def test_body_quotes_text_and_lists_device(self):
        body = f.issue_body(f.decode_document(DOCUMENT))
        self.assertTrue(body.startswith("> Не сохраняется заметка\n>\n> При переводе"))
        self.assertIn("| Отправлено (UTC) | 2026-11-08 10:00:00 |", body)
        self.assertIn("| Версия | 0.1.0 (сборка 27) |", body)
        self.assertIn("| Установка | 01234567 |", body)
        self.assertIn("`feedback/abc123`", body)

    def test_oldest_first(self):
        items = [{"createdAt": "2026-11-08T10:00:00Z"}, {"createdAt": "2026-11-07T10:00:00Z"}]
        self.assertEqual([i["createdAt"][:10] for i in f.oldest_first(items)], ["2026-11-07", "2026-11-08"])


if __name__ == "__main__":
    unittest.main()
