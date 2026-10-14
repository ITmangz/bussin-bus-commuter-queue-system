import contextlib
import io
import time
import unittest
from unittest.mock import patch
from concurrent.futures import Future
from student_numbers import reliable_text, match_numbers, StudentNumber, EXAMPLES


def row(x, text, confidence=0.99, y=10):
    return [[[x, y], [x + 90, y], [x + 90, y + 25], [x, y + 25]], text, confidence]


class KioskScannerTests(unittest.TestCase):
    def test_automatic_fill_only_after_three_agreeing_jobs(self):
        import tkinter as tk
        from camera_app import CardCameraApp
        from card_detector import demo_frame
        root = tk.Tk()
        root.withdraw()
        app = CardCameraApp(root, kiosk=True)
        match = StudentNumber('PUP', EXAMPLES['PUP'])
        try:
            self.assertFalse(app.use_button.winfo_manager())
            app.ocr_source = demo_frame()
            output = io.StringIO()
            with contextlib.redirect_stdout(output), patch.object(app, 'close') as close:
                for index in range(3):
                    app.last_read = time.monotonic()
                    app.ocr_future = Future()
                    app.ocr_session = app.session
                    app.ocr_future.set_result(([match], match.number))
                    app.poll_numbers()
                    if index < 2:
                        self.assertEqual(output.getvalue(), '')
                        close.assert_not_called()
                self.assertEqual(output.getvalue(), 'BUSSIN_STUDENT_ID=2025-09011-MN-0\n')
                close.assert_called_once()
        finally:
            app.close()

    def test_stricter_confidence_does_not_accept_uncertain_text(self):
        self.assertEqual(match_numbers(reliable_text([row(10, '202511062', .90)], .92)), [])

    def test_fragmented_id_is_reassembled(self):
        rows = [row(10, '2025 -'), row(105, '09011 -'), row(200, 'MN - 0')]
        self.assertIn(StudentNumber('PUP', EXAMPLES['PUP']), match_numbers(reliable_text(rows)))

    def test_low_confidence_and_distant_fragments_are_not_joined(self):
        for rows in ([row(10, '2025 -'), row(105, '09011 -', .4), row(200, 'MN - 0')],
                     [row(10, '2025 -'), row(405, '09011 -'), row(500, 'MN - 0')],
                     [row(10, '2025 -'), row(105, '09011 -', y=60), row(200, 'MN - 0')]):
            self.assertEqual(match_numbers(reliable_text(rows)), [])

    def test_confirmation_protocol_stale_and_cancel(self):
        import tkinter as tk
        from camera_app import CardCameraApp
        root = tk.Tk()
        root.withdraw()
        app = CardCameraApp(root, kiosk=True)
        try:
            self.assertFalse(app.demo_button.winfo_manager())
            self.assertFalse(app.image_button.winfo_manager())
            self.assertEqual(app.consensus.required, 3)
            output = io.StringIO()
            with contextlib.redirect_stdout(output), patch.object(app, 'close') as close:
                app.use_number()
                close.assert_not_called()
                app.accepted_number = StudentNumber('PUP', EXAMPLES['PUP'])
                app.last_result_time = time.monotonic() - 10
                app.use_number()
                self.assertEqual(output.getvalue(), '')
                app.last_result_time = time.monotonic()
                app.use_number()
                self.assertEqual(output.getvalue(), 'BUSSIN_STUDENT_ID=2025-09011-MN-0\n')
                close.assert_called_once()
            app.stop()
            self.assertIsNone(app.accepted_number)
            self.assertTrue(app.use_button.instate(['disabled']))
        finally:
            app.close()


if __name__ == '__main__':
    unittest.main(verbosity=2)
