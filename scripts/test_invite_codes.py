"""Unit tests for invite_codes.py: python3 -m unittest discover -s scripts -p 'test_*.py'"""
import datetime
import os
import random
import sys
import unittest

sys.path.insert(0, os.path.dirname(__file__))
import invite_codes as ic  # noqa: E402


class InviteCodesTest(unittest.TestCase):
    def test_codes_use_the_safe_alphabet_and_are_long(self):
        for _ in range(200):
            code = ic.new_code()
            self.assertEqual(len(code), 12)
            self.assertTrue(all(ch in ic.ALPHABET for ch in code))
        self.assertFalse(set("0O1IL") & set(ic.ALPHABET))

    def test_codes_differ(self):
        self.assertEqual(len({ic.new_code() for _ in range(500)}), 500)

    def test_display_groups_by_four(self):
        self.assertEqual(ic.display("K7QXM2PD9WTA"), "K7QX-M2PD-9WTA")

    def test_normalize_accepts_what_people_type(self):
        for typed in ("K7QX-M2PD-9WTA", "k7qx m2pd 9wta", " K7QXM2PD9WTA ", "k7qx–m2pd–9wta".replace("–", "-")):
            self.assertEqual(ic.normalize(typed), "K7QXM2PD9WTA")

    def test_normalize_rejects_what_cannot_be_a_code(self):
        for typed in ("", "K7QX-M2PD", "K7QX-M2PD-9WTA-1", "K7QX-M2PD-9WT0", "K7QX-M2PD-9WTI", "привет-мир-1234"):
            self.assertIsNone(ic.normalize(typed), typed)

    def test_document_fields_match_the_rules(self):
        now = datetime.datetime(2026, 11, 23, 12, 0, tzinfo=datetime.timezone.utc)
        fields = ic.document_fields("  Аня   Иванова ", 3, now)
        self.assertEqual(set(fields), {"label", "createdAt", "revoked", "maxDevices", "devices"})
        self.assertEqual(fields["label"]["stringValue"], "Аня Иванова")
        self.assertEqual(fields["revoked"], {"booleanValue": False})
        self.assertEqual(fields["maxDevices"], {"integerValue": "3"})
        self.assertEqual(fields["devices"], {"arrayValue": {}})
        self.assertEqual(fields["createdAt"]["timestampValue"], "2026-11-23T12:00:00Z")

    def test_label_is_cut(self):
        self.assertEqual(len(ic.clean_label("я" * 500)), ic.LABEL_LENGTH)

    def test_summaries(self):
        self.assertIn("K7QX-M2PD-9WTA", ic.summary_created("K7QXM2PD9WTA", "Аня", 3))
        self.assertIn("отозван", ic.summary_list([{"code": "K7QXM2PD9WTA", "label": "Аня", "revoked": True, "devices": 1, "max": 3}]))
        self.assertIn("пока нет", ic.summary_list([]))

    def test_rng_is_injectable(self):
        self.assertEqual(ic.new_code(random.Random(1)), ic.new_code(random.Random(1)))


if __name__ == "__main__":
    unittest.main()
