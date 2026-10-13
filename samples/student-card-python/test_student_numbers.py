"""Format, real OCR and UI tests; no physical camera is opened."""
import time
import unittest
from pathlib import Path
from concurrent.futures import Future
from student_numbers import EXAMPLES, match_numbers, StudentNumber, StudentNumberReader, NumberConsensus
from card_detector import demo_frame, find_card, crop_card, cv2


class NumberTests(unittest.TestCase):
    def test_consensus_requires_repeated_readings_and_resets_on_changes(self):
        consensus = NumberConsensus()
        pup = StudentNumber("PUP", EXAMPLES["PUP"])
        nu = StudentNumber("NU MOA", EXAMPLES["NU MOA"])
        self.assertIsNone(consensus.update([pup]))
        self.assertEqual(consensus.update([pup]), pup)
        self.assertIsNone(consensus.update([nu]))
        self.assertIsNone(consensus.update([]))
        self.assertIsNone(consensus.update([nu]))
        self.assertIsNone(consensus.update([pup, nu]))
        self.assertIsNone(consensus.update([pup]))

    def test_preprocessing_disagreement_is_not_silently_resolved(self):
        reader = StudentNumberReader.__new__(StudentNumberReader)
        readings = iter(["202511062", "202511063", "202511062"])
        reader.engine = lambda frame: ([[[], next(readings), 0.99]], None)
        matches, _ = reader.read(demo_frame())
        self.assertEqual(len(matches), 2)
        self.assertIsNone(NumberConsensus().update(matches))

    def test_one_good_pass_is_not_enough(self):
        reader = StudentNumberReader.__new__(StudentNumberReader)
        readings = iter(["202511062", "unclear", "unclear"])
        reader.engine = lambda frame: ([[[], next(readings), 0.99]], None)
        matches, _ = reader.read(demo_frame())
        self.assertEqual(matches, [])

    def test_exact_formats_and_year_variants(self):
        for school, number in EXAMPLES.items():
            self.assertEqual(match_numbers(number), [StudentNumber(school, number)])
            another_year = number.replace("2025", "2024")
            self.assertEqual(match_numbers(another_year), [StudentNumber(school, another_year)])

    def test_spacing_unicode_and_multiple_matches(self):
        self.assertEqual(match_numbers("2025 – 09011 – MN – 0"), [StudentNumber("PUP", EXAMPLES["PUP"])])
        self.assertEqual(len(match_numbers("\n".join(EXAMPLES.values()))), 4)
        self.assertEqual(len(match_numbers("202511062\n202511062")), 1)

    def test_invalid_numbers_do_not_match_a_shorter_format(self):
        for value in ("2025110620", "1202511062", "2025-10588278", "2025-14006", "2025-140065-8",
                      "2025-09011-MN-O", "2025-14O065", "nothing readable"):
            self.assertEqual(match_numbers(value), [], value)

    def test_real_ocr_all_four_samples(self):
        reader = StudentNumberReader()
        output = Path(__file__).resolve().parent / "test-output"
        output.mkdir(exist_ok=True)
        for school, number in EXAMPLES.items():
            with self.subTest(school=school):
                frame = demo_frame(0, school)
                cv2.imwrite(str(output / (school.lower().replace(" ", "-") + "-sample.png")), frame)
                points = find_card(frame)
                self.assertIsNotNone(points)
                matches, text = reader.read(crop_card(frame, points))
                self.assertIn(StudentNumber(school, number), matches, text)

    def test_low_confidence_text_is_not_accepted(self):
        reader = StudentNumberReader.__new__(StudentNumberReader)
        reader.engine = lambda frame: ([[[], "202511062", 0.2]], None)
        matches, text = reader.read(demo_frame())
        self.assertEqual(matches, [])
        self.assertIn("202511062", text)

    def test_live_ui_detects_numbers_and_clears_after_stop(self):
        import tkinter as tk
        from camera_app import CardCameraApp
        root = tk.Tk()
        root.attributes("-alpha", 0)
        app = CardCameraApp(root)
        root.update()
        try:
            for school, number in EXAMPLES.items():
                app.demo_school.set(school)
                app.start(True)
                deadline = time.monotonic() + 15
                while time.monotonic() < deadline and app.number_result.get() != number:
                    root.update()
                    time.sleep(0.025)
                self.assertEqual(app.number_result.get(), number, app.reading_status.get())
                self.assertEqual(app.school_result.get(), "Format: " + school)
                self.assertTrue(app.preview.cget("image"))
                app.stop()
                deadline = time.monotonic() + 3
                while app.worker is not None and time.monotonic() < deadline:
                    root.update()
                    time.sleep(0.025)
                self.assertEqual(app.number_result.get(), "Waiting for an ID number")
                app.ocr_future = Future()
                app.ocr_session = app.session - 1
                app.ocr_future.set_result(([StudentNumber(school, number)], number))
                app.poll_numbers()
                self.assertEqual(app.number_result.get(), "Waiting for an ID number")
        finally:
            app.close()


if __name__ == "__main__":
    unittest.main(verbosity=2)
