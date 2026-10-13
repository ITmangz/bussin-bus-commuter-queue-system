"""Run without a physical camera: python test_detector.py."""
from pathlib import Path
import queue
import threading
import unittest
from unittest.mock import patch

from card_detector import cv2, np, find_card, crop_card, CardTracker, annotate, demo_frame
from camera_app import CardCameraApp


class DetectionTests(unittest.TestCase):
    def canvas(self):
        return np.full((540, 960, 3), 35, np.uint8)

    def test_landscape_portrait_and_rotation(self):
        for size, angle in [((420, 265), 0), ((265, 420), 0), ((420, 265), 28), ((420, 265), -20)]:
            with self.subTest(size=size, angle=angle):
                frame = self.canvas()
                corners = cv2.boxPoints(((480, 270), size, angle)).astype(np.int32)
                cv2.fillConvexPoly(frame, corners, (240, 240, 240))
                result = find_card(frame)
                self.assertIsNotNone(result)
                self.assertGreater(crop_card(frame, result).size, 1000)

    def test_perspective_and_blur(self):
        frame = cv2.GaussianBlur(demo_frame(), (7, 7), 1.5)
        self.assertIsNotNone(find_card(frame))

    def test_reject_blank_circle_square_tiny_and_clipped(self):
        cases = []
        cases.append(self.canvas())
        frame = self.canvas()
        cv2.circle(frame, (480, 270), 140, (245, 245, 245), -1)
        cases.append(frame)
        frame = self.canvas()
        cv2.rectangle(frame, (310, 100), (650, 440), (245, 245, 245), -1)
        cases.append(frame)
        frame = self.canvas()
        cv2.rectangle(frame, (100, 100), (145, 128), (245, 245, 245), -1)
        cases.append(frame)
        frame = self.canvas()
        cv2.rectangle(frame, (-10, 60), (410, 325), (245, 245, 245), -1)
        cases.append(frame)
        for i, frame in enumerate(cases):
            with self.subTest(case=i):
                self.assertIsNone(find_card(frame))

    def test_tracking_stability_loss_and_changed_object(self):
        points = find_card(demo_frame())
        tracker = CardTracker(required_frames=5)
        for _ in range(4):
            self.assertFalse(tracker.update(points))
        self.assertTrue(tracker.update(points))
        self.assertFalse(tracker.update(points + np.array([300, 0], np.float32)))
        self.assertFalse(tracker.update(None))
        self.assertEqual(tracker.count, 0)

    def test_camera_stop_releases_device(self):
        app = CardCameraApp.__new__(CardCameraApp)
        app.messages = queue.Queue(maxsize=2)
        cancel = threading.Event()
        class FakeCamera:
            released = False
            def isOpened(self): return True
            def set(self, *args): pass
            def read(self):
                cancel.set()
                return True, demo_frame()
            def release(self): self.released = True
        camera = FakeCamera()
        with patch("camera_app.cv2.VideoCapture", return_value=camera):
            app.stream(0, False, 1, cancel)
        self.assertTrue(camera.released)

    def test_failed_camera_reports_error_and_releases_device(self):
        app = CardCameraApp.__new__(CardCameraApp)
        app.messages = queue.Queue(maxsize=2)
        class FakeCamera:
            released = False
            def isOpened(self): return False
            def release(self): self.released = True
        camera = FakeCamera()
        with patch("camera_app.cv2.VideoCapture", return_value=camera):
            app.stream(0, False, 9, threading.Event())
        self.assertTrue(camera.released)
        session, kind, message = app.messages.get_nowait()
        self.assertEqual((session, kind), (9, "error"))
        self.assertIn("Cannot open", message)


if __name__ == "__main__":
    output = Path(__file__).resolve().parent / "test-output"
    output.mkdir(exist_ok=True)
    frame = demo_frame()
    points = find_card(frame)
    cv2.imwrite(str(output / "synthetic-card.png"), frame)
    cv2.imwrite(str(output / "detected-card.png"), annotate(frame, points, True))
    unittest.main(verbosity=2)
