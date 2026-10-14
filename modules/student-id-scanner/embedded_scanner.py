"""Windowless Python camera/OCR worker for the Swing passenger panel.

Stdout protocol: FRAME=<base64 JPEG>, STATUS=<message>, BUSSIN_STUDENT_ID=<number>.
Images are streamed in memory only. No Tk root, OpenCV window, or network server.
"""
import base64
import sys
import time
from concurrent.futures import ThreadPoolExecutor
from card_detector import cv2, np, find_card, crop_card, CardTracker, annotate
from student_numbers import StudentNumberReader, NumberConsensus


def run(camera_factory=None, reader_factory=None, output=print, timeout=180):
    camera = None
    pool = ThreadPoolExecutor(max_workers=1)
    reader = None
    future = None
    consensus = NumberConsensus(required=3)
    tracker = CardTracker()

    def emit(message):
        output(message, flush=True)

    def read(frame):
        nonlocal reader
        if reader is None:
            reader = (reader_factory or (lambda: StudentNumberReader(min_confidence=0.92)))()
        return reader.read(frame)

    try:
        emit('STATUS=Opening camera')
        if camera_factory:
            camera = camera_factory()
        else:
            for backend in ([cv2.CAP_DSHOW, cv2.CAP_MSMF] if sys.platform == 'win32' else [cv2.CAP_ANY]):
                camera = cv2.VideoCapture(0, backend)
                if camera.isOpened():
                    break
                camera.release()
        if camera is None or not camera.isOpened():
            emit('ERROR=Camera unavailable. Close other camera apps and check camera permissions.')
            return
        camera.set(cv2.CAP_PROP_FRAME_WIDTH, 1920)
        camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 1080)
        camera.set(cv2.CAP_PROP_AUTOFOCUS, 1)
        deadline = time.monotonic() + timeout
        last_preview = last_read = 0
        failures = 0
        emit('STATUS=Hold one student ID steady with the number clearly visible.')
        while time.monotonic() < deadline:
            ok, original = camera.read()
            if not ok or original is None:
                failures += 1
                if failures >= 15:
                    emit('ERROR=Camera stopped sending images. Try scanning again.')
                    return
                time.sleep(.05)
                continue
            failures = 0
            now = time.monotonic()
            preview = cv2.resize(original, (640, max(1, round(original.shape[0] * 640 / original.shape[1]))))
            points = find_card(preview)
            stable = tracker.update(points)
            if now - last_preview >= .15:
                encoded, jpeg = cv2.imencode('.jpg', annotate(preview, points, stable), [cv2.IMWRITE_JPEG_QUALITY, 75])
                if encoded:
                    emit('FRAME=' + base64.b64encode(jpeg).decode('ascii'))
                last_preview = now
            if future is not None and future.done():
                matches, _ = future.result()
                future = None
                match = consensus.update(matches)
                if match is not None:
                    emit('BUSSIN_STUDENT_ID=' + match.number)
                    return
                if len(matches) == 1:
                    emit('STATUS=Checking the number. Keep your ID steady.')
                elif len(matches) > 1:
                    emit('STATUS=Multiple numbers found. Show only one ID.')
                else:
                    emit('STATUS=Move the ID closer and avoid glare on the number.')
            if future is None and now - last_read >= 1.2:
                source = original
                if stable:
                    scale = np.array([original.shape[1] / preview.shape[1], original.shape[0] / preview.shape[0]], np.float32)
                    source = crop_card(original, points * scale)
                future = pool.submit(read, source.copy())
                last_read = now
            time.sleep(.025)
        emit('ERROR=Scan timed out. Try again or enter the number manually.')
    except BrokenPipeError:
        pass  # Kiosk cancelled or left the passenger screen.
    except Exception:
        emit('ERROR=Scanner unavailable. Check the Python setup or enter the number manually.')
    finally:
        if camera is not None:
            camera.release()
        pool.shutdown(wait=False, cancel_futures=True)


if __name__ == '__main__':
    run()
