import importlib.util
import json
import pathlib
import tempfile
import unittest

_spec = importlib.util.spec_from_file_location("make_update_files", pathlib.Path(__file__).with_name("make-update-files.py"))
m = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(m)


def rel(code):
    return {"versionCode": code, "versionName": f"0.{code}", "date": "2026-11-01", "notes": "• x"}


class HistoryTest(unittest.TestCase):
    def test_new_release_goes_first(self):
        self.assertEqual([3, 2, 1], [r["versionCode"] for r in m.history(rel(3), [rel(2), rel(1)])])

    def test_rebuild_of_same_code_replaces_the_old_entry(self):
        new = dict(rel(2), notes="• new")
        out = m.history(new, [rel(2), rel(1)])
        self.assertEqual([2, 1], [r["versionCode"] for r in out])
        self.assertEqual("• new", out[0]["notes"])

    def test_keeps_at_most_history_size(self):
        out = m.history(rel(100), [rel(c) for c in range(1, 40)])
        self.assertEqual(m.HISTORY_SIZE, len(out))
        self.assertEqual(100, out[0]["versionCode"])


class ReadHistoryTest(unittest.TestCase):
    def read(self, text):
        with tempfile.TemporaryDirectory() as d:
            p = pathlib.Path(d) / "releases.json"
            p.write_text(text, encoding="utf-8")
            return m.read_history(str(p))

    def test_missing_or_broken_file_is_empty(self):
        self.assertEqual([], m.read_history(None))
        self.assertEqual([], m.read_history("/nonexistent/releases.json"))
        self.assertEqual([], self.read("<html>Not Found</html>"))
        self.assertEqual([], self.read('{"versionCode": 1}'))

    def test_skips_bad_entries(self):
        self.assertEqual([rel(1)], self.read(json.dumps([rel(1), {"versionName": "x"}, "y"])))


if __name__ == "__main__":
    unittest.main()
