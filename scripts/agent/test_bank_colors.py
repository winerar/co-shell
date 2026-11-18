import importlib.util
import unittest
from pathlib import Path

spec = importlib.util.spec_from_file_location("bank_colors", Path(__file__).with_name("bank-colors.py"))
bc = importlib.util.module_from_spec(spec)
spec.loader.exec_module(bc)


class BankColorsScriptTest(unittest.TestCase):
    def test_every_preset_has_a_domain(self):
        presets = bc.parse_presets(bc.SRC.read_text(encoding="utf8"))
        self.assertGreater(len(presets), 20)
        self.assertEqual([], [n for n, _ in presets if n not in bc.DOMAINS])

    def test_parse_reads_name_and_rgb(self):
        self.assertEqual([("Galicia", 0xF26E22)], bc.parse_presets('Preset("Galicia", "GA", 0xFFF26E22),'))

    def test_dominant_ignores_white_grey_and_transparent(self):
        px = [(255, 255, 255, 255)] * 50 + [(120, 120, 120, 255)] * 50 + [(250, 100, 0, 0)] * 50 + [(250, 100, 0, 255)] * 10
        self.assertEqual((250, 100, 0, 10), bc.dominant(px))

    def test_dominant_none_for_grey_icon(self):
        self.assertIsNone(bc.dominant([(10, 10, 10, 255)] * 20))


if __name__ == "__main__":
    unittest.main()
