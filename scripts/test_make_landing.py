"""Unit tests for make_landing.py: python3 -m unittest discover -s scripts -p 'test_*.py'"""
import os
import sys
import tempfile
import unittest

sys.path.insert(0, os.path.dirname(__file__))
import make_landing as ml  # noqa: E402

MANIFEST = {"versionCode": 1110, "versionName": "1.8", "apk": "https://github.com/o/co-shell-releases/releases/download/build-1110/co-shell-1110.apk",
            "sha256": "a3f1" + "0" * 56 + "c92e", "size": 6_979_729, "notes": "x"}


class MakeLandingTest(unittest.TestCase):
    def setUp(self):
        with open(ml.TEMPLATE, encoding="utf-8") as f:
            self.template = f.read()

    def test_every_language_gets_the_release_and_no_placeholder_is_left(self):
        for lang, _ in ml.LANGS:
            page = ml.page(self.template, lang, MANIFEST)
            self.assertIn(f'lang="{lang}"', page)
            self.assertIn(MANIFEST["apk"], page)
            self.assertIn(MANIFEST["sha256"], page)
            self.assertIn("1.8", page)
            self.assertNotIn("{{", page, lang)

    def test_size_is_in_megabytes_with_the_decimal_mark_of_the_language(self):
        self.assertIn("7,0 МБ", ml.page(self.template, "ru", MANIFEST))
        self.assertIn("7.0 MB", ml.page(self.template, "en", MANIFEST))
        self.assertIn("7,0 MB", ml.page(self.template, "es", MANIFEST))

    def test_the_page_says_the_app_needs_a_code(self):
        self.assertIn("код приглашения", ml.page(self.template, "ru", MANIFEST).lower())
        self.assertIn("invitation code", ml.page(self.template, "en", MANIFEST).lower())
        self.assertIn("código de invitación", ml.page(self.template, "es", MANIFEST).lower())

    def test_all_languages_have_the_same_texts(self):
        self.assertEqual(set(ml.TEXTS["ru"]), set(ml.TEXTS["en"]))
        self.assertEqual(set(ml.TEXTS["ru"]), set(ml.TEXTS["es"]))

    def test_the_language_switch_marks_the_current_one_and_links_the_others(self):
        page = ml.page(self.template, "en", MANIFEST)
        self.assertIn("<b>EN</b>", page)
        self.assertIn('<a href="../ru/">RU</a>', page)
        self.assertNotIn('<a href="../en/">', page)

    def test_values_are_escaped(self):
        page = ml.page(self.template, "ru", {**MANIFEST, "versionName": '<script>alert(1)</script>'})
        self.assertNotIn("<script>alert(1)", page)

    def test_build_writes_pages_the_chooser_and_nojekyll(self):
        with tempfile.TemporaryDirectory() as out:
            ml.build(MANIFEST, out, self.template)
            for name in ("ru/index.html", "en/index.html", "es/index.html", "index.html", ".nojekyll"):
                self.assertTrue(os.path.exists(os.path.join(out, name)), name)
            chooser = open(os.path.join(out, "index.html"), encoding="utf-8").read()
            self.assertIn("navigator.languages", chooser)
            self.assertIn('href="es/"', chooser)


if __name__ == "__main__":
    unittest.main()


class PrivacyPagesTest(unittest.TestCase):
    def test_same_blocks_in_every_language(self):
        import privacy_pages as pp
        for kind in ("privacy", "delete"):
            shapes = {lang: [(k, len(v) if k == "ul" else 0) for k, v in pp.TEXTS[lang][kind]] for lang in pp.LANGS}
            self.assertEqual(shapes["ru"], shapes["en"])
            self.assertEqual(shapes["ru"], shapes["es"])

    def test_writes_all_pages(self):
        import tempfile, os, privacy_pages as pp
        with tempfile.TemporaryDirectory() as d:
            pp.write_all(d)
            for kind in ("privacy", "delete"):
                for lang in pp.LANGS:
                    self.assertTrue(os.path.getsize(os.path.join(d, kind, lang, "index.html")) > 500)
