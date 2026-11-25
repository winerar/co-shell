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
        shape = lambda t: {k: [len(s) if isinstance(s, tuple) else s for s in v] if isinstance(v, list) else 0  # noqa: E731
                           for k, v in t.items() if isinstance(v, list)}
        for lang in ("en", "es"):
            self.assertEqual(set(ml.TEXTS["ru"]), set(ml.TEXTS[lang]))
            self.assertEqual({k: len(v) for k, v in shape(ml.TEXTS["ru"]).items()}, {k: len(v) for k, v in shape(ml.TEXTS[lang]).items()})
            dialogs = lambda steps: [d is None for _, _, d in steps]  # noqa: E731
            self.assertEqual(dialogs(ml.TEXTS["ru"]["install"]), dialogs(ml.TEXTS[lang]["install"]), lang)

    def test_qr_points_to_the_page_of_the_same_language_on_github_pages(self):
        os.environ.pop("LANDING_URL", None)
        self.assertEqual("https://o.github.io/co-shell-releases/", ml.site_url(MANIFEST))
        page = ml.page(self.template, "es", MANIFEST)
        self.assertIn('class="card qr"', page)
        expected = ml.qr_code.svg("https://o.github.io/co-shell-releases/es/", module=4, quiet=2)
        self.assertIn(expected, page)

    def test_custom_domain_overrides_the_qr_address_and_unknown_apk_host_drops_the_qr(self):
        os.environ["LANDING_URL"] = "https://co-shell.app"
        try:
            self.assertEqual("https://co-shell.app/", ml.site_url(MANIFEST))
        finally:
            del os.environ["LANDING_URL"]
        self.assertNotIn('class="card qr"', ml.page(self.template, "ru", {**MANIFEST, "apk": "https://example.org/a.apk"}))

    def test_whats_new_is_on_the_russian_page_only(self):
        manifest = {**MANIFEST, "notes": "• Сканер QR читает чек издалека.\n• Исправлено: значок «Экспорт»."}
        ru = ml.page(self.template, "ru", manifest)
        self.assertIn("Что нового в 1.8", ru)
        self.assertIn("<li>Сканер QR читает чек издалека.</li>", ru)
        self.assertNotIn('class="news"', ml.page(self.template, "en", manifest))
        self.assertNotIn('class="news"', ml.page(self.template, "ru", {**MANIFEST, "notes": ""}))

    def test_install_steps_are_one_list_for_any_android_and_the_footer_links_the_policy(self):
        page = ml.page(self.template, "ru", MANIFEST)
        start = page.index('class="steps"')
        self.assertEqual(4, page.count("<li>", start, page.index("</ol>", start)))
        self.assertNotIn("Xiaomi", page)
        self.assertNotIn("{{", page)
        self.assertIn("Всё равно установить", page)
        self.assertIn('href="../privacy/ru/"', page)
        self.assertIn('href="../delete/ru/"', page)

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
            for name in ("ru/index.html", "en/index.html", "es/index.html", "index.html", ".nojekyll",
                         "img/home_light.png", "img/home_dark.png", "img/home_en.png", "img/home_es.png"):
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
