import base64
import unittest
from card_detector import demo_frame, cv2
from student_numbers import StudentNumber
from embedded_scanner import run


class Camera:
    def __init__(self, opened=True):
        self.opened, self.released = opened, False
    def isOpened(self): return self.opened
    def set(self, *args): pass
    def read(self): return True, demo_frame()
    def release(self): self.released = True


class EmbeddedTests(unittest.TestCase):
    def test_streams_preview_and_fills_after_three_reads_without_window(self):
        camera, output = Camera(), []
        class Reader:
            calls = 0
            def read(self, frame):
                self.calls += 1
                return [StudentNumber('PUP', '2025-09011-MN-0')], ''
        reader = Reader()
        run(lambda: camera, lambda: reader, lambda line, **kw: output.append(line), timeout=8)
        self.assertTrue(camera.released)
        self.assertEqual(reader.calls, 3)
        self.assertEqual(output[-1], 'BUSSIN_STUDENT_ID=2025-09011-MN-0')
        frames = [line[6:] for line in output if line.startswith('FRAME=')]
        self.assertTrue(frames)
        import numpy as np
        image = cv2.imdecode(np.frombuffer(base64.b64decode(frames[0]), np.uint8), cv2.IMREAD_COLOR)
        self.assertEqual(image.shape[1], 640)

    def test_camera_error_releases_device(self):
        camera, output = Camera(False), []
        run(lambda: camera, output=lambda line, **kw: output.append(line))
        self.assertTrue(camera.released)
        self.assertTrue(output[-1].startswith('ERROR='))
        self.assertFalse(any(line.startswith('BUSSIN_STUDENT_ID=') for line in output))

    def test_uncertain_reading_times_out_without_filling(self):
        camera, output = Camera(), []
        class Reader:
            def read(self, frame): return [], ''
        run(lambda: camera, Reader, lambda line, **kw: output.append(line), timeout=.5)
        self.assertTrue(camera.released)
        self.assertTrue(output[-1].startswith('ERROR=Scan timed out'))


if __name__ == '__main__': unittest.main(verbosity=2)
