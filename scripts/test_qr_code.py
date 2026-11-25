"""Unit tests for qr_code.py: python3 -m unittest discover -s scripts -p 'test_*.py'

Golden hashes were checked once (2026-11-25) against the `qrcode` library with the same mask and byte mode
(303 random strings, versions 1–10, all bit-identical) and decoded by OpenCV; here they guard against regressions.
"""
import hashlib
import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(__file__))
import qr_code  # noqa: E402

GOLDEN = {
    "A": (21, "2092585037e9da56"),
    "https://winerar.github.io/co-shell-releases/ru/": (33, "e987b6555d1fd43f"),
    "Привет, мир": (25, "ddf1e5aebb8ff54d"),
    "x" * 150: (49, "1336208e16162851"),
    "https://github.com/winerar/co-shell-releases/releases/latest/download/co-shell.apk": (37, "18002c97ed65ab1d"),
}


def digest(grid):
    return hashlib.sha256("".join("1" if x else "0" for row in grid for x in row).encode()).hexdigest()[:16]


class QrCodeTest(unittest.TestCase):
    def test_matrices_match_the_checked_golden_ones(self):
        for text, (size, expected) in GOLDEN.items():
            grid = qr_code.matrix(text)
            self.assertEqual(size, len(grid), text[:20])
            self.assertEqual(expected, digest(grid), text[:20])

    def test_finder_patterns_sit_in_three_corners(self):
        grid = qr_code.matrix("https://example.org/")
        n = len(grid)
        for r, c in ((0, 0), (0, n - 7), (n - 7, 0)):
            ring = [grid[r][c + i] for i in range(7)]
            self.assertEqual([True] * 7, ring)
            self.assertTrue(grid[r + 3][c + 3])
            self.assertFalse(grid[r + 1][c + 1])

    def test_format_bits_carry_level_m_and_a_valid_checksum(self):
        grid = qr_code.matrix("co-shell")
        n = len(grid)
        bits = [grid[8][n - 1 - i] for i in range(8)] + [grid[n - 7 + i][8] for i in range(7)]
        word = sum(int(b) << i for i, b in enumerate(bits)) ^ 0b101010000010010
        self.assertEqual(0b00, word >> 13)  # level M
        self.assertEqual(word, qr_code._bch(word >> 10, 0b10100110111, 10))

    def test_too_long_text_is_refused(self):
        with self.assertRaises(ValueError):
            qr_code.matrix("x" * 300)

    def test_svg_is_one_path_on_white_with_a_quiet_zone(self):
        out = qr_code.svg("A", module=4, quiet=4)
        self.assertIn('viewBox="0 0 29 29"', out)
        self.assertEqual(1, out.count("<path"))
        self.assertIn('fill="#fff"', out)


if __name__ == "__main__":
    unittest.main()
