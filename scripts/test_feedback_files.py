import base64
import unittest

import feedback_files


def part(file, n, parts, data, name="chek.pdf"):
    return {"file": file, "part": n, "parts": parts, "data": data, "name": name, "mime": "application/pdf", "_name": f"x/{file}{n}"}


class FeedbackFilesTest(unittest.TestCase):
    def test_glues_parts_in_order(self):
        raw = bytes(range(256)) * 10
        b64 = base64.b64encode(raw).decode()
        parts = [part(0, 1, 2, b64[1000:]), part(0, 0, 2, b64[:1000])]
        self.assertEqual([("chek.pdf", "application/pdf", raw)], feedback_files.glue(parts))

    def test_skips_a_file_with_a_missing_part(self):
        ok = base64.b64encode(b"ok").decode()
        parts = [part(0, 0, 2, "AAAA"), part(1, 0, 1, ok, "shot.png")]
        self.assertEqual([("shot.png", "application/pdf", b"ok")], feedback_files.glue(parts))

    def test_safe_name(self):
        self.assertEqual("chek_4807.pdf", feedback_files.safe_name("../../chek 4807.pdf"))
        self.assertEqual("file", feedback_files.safe_name("///"))

    def test_links_section(self):
        self.assertEqual("", feedback_files.links_section([]))
        text = feedback_files.links_section([("chek.pdf", 17904, "https://x/y")])
        self.assertIn("[chek.pdf](https://x/y) · 17 КБ", text)


if __name__ == "__main__":
    unittest.main()
