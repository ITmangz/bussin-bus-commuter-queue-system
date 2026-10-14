# Python live student-ID number reader

## Kiosk integration

Click **Scan ID** at the upper right of the Student passenger form. The live camera
replaces the form inside Passenger Details; no second window opens. The header and
steps remain visible. Hold one card steady: after three agreeing OCR
jobs match a supported number format, the form returns and fills the existing
Student ID field automatically. The field remains editable. There is no capture
or confirmation button. Scanning does not advance the booking or verify eligibility.
Closing/cancelling the scanner leaves the previous field value unchanged. The
kiosk scan is limited to three minutes and offers manual entry if Python fails.

The kiosk runs the windowless `embedded_scanner.py` worker. Python captures the
camera and runs OCR; Java displays JPEG previews streamed through stdout in memory.
There is no Tk window, Java camera capture, image file, or local HTTP server.
Back becomes Cancel scan and Continue is disabled until the form returns. Cancel,
Start Over, leaving the passenger screen, and completion terminate the worker;
errors restore manual entry with an inline explanation. Three consecutive
OCR jobs must agree (the standalone sample requires two). Nearby text boxes on
the same printed line are reassembled to read split student numbers; low-confidence
or distant fragments are never joined. Contradictory readings are withheld.
Kiosk OCR requires a confidence score of at least 0.92 per text box, plus agreement
across at least two image preprocessing passes in each of the three jobs. This is
an OCR score threshold, not a 92% or 100% real-world accuracy guarantee. Commuters
should review the editable field before continuing.

Run Java from the QueueSystem directory. It uses the installed bundled Python
when available, otherwise `python` on PATH. To use a different Python executable,
set `QPAL_PYTHON` or pass `-Dqpal.python=C:/path/to/python.exe` to Java. Install
the requirements below in that interpreter. The standalone Java camera demo has
been removed; all camera capture and OCR run in Python.

Double-click **run-camera.cmd**, click **Start camera**, and hold the student ID number
clearly in view. The camera stays inside the window. Reading is automatic: the matching
format and student number appear on the right. No Capture button is needed.

## Supported formats

| Format label | Your example | Rule |
|---|---|---|
| PUP | `2025-09011-MN-0` | 20xx + 5 digits + 2 letters + 1 digit, separated by hyphens |
| NU MOA | `2025-1058827` | 20xx + hyphen + 7 digits |
| NU APC | `2025-140065` | 20xx + hyphen + 6 digits |
| FEU TECH | `202511062` | 9 digits beginning with 20 |

These rules come from your examples. The school label means matching number format,
not verified school affiliation or enrollment. Check the number against the actual ID.

## Controls

- **Start camera / Stop:** open/release the selected webcam. Try camera 1 if 0 fails.
- **Try demo:** choose a school in the adjacent dropdown, then run a moving synthetic
  card carrying your example number. Stop before choosing another sample.
- **Test image:** automatically read a saved photo or a generated `test-output/*-sample.png`.

Show one ID at a time. Use even light, avoid glare and hold the number close enough to
be sharp. Card edges help produce a crop, but the whole frame is read if no outline is
found. Multiple matching numbers are flagged instead of selecting one arbitrarily.

## How it works

OpenCV provides the preview and optional card outline. RapidOCR reads text locally;
regular expressions match the four formats. A background worker reads about every 1.2
seconds (or when the previous reading finishes), keeping the preview responsive.
Readings below 85% OCR confidence are not accepted (the score is not a guarantee of correctness).
The reader compares original and contrast-enhanced images, with an additional thresholded
image when needed. Conflicting numbers are flagged. It then waits for two consecutive
OCR jobs to agree before displaying a confirmed reading. This may take a few seconds.
Separator spacing is normalized; unreadable
letters are not silently replaced with digits. Latest OCR text remains visible.

Stopping/changing inputs clears results; old jobs cannot restore them. Live readings
expire after four seconds without a fresh result. Camera images and numbers are not
uploaded or saved. Java receives preview frames and the detected number in memory, without temporary image or result files.

## Files and setup

- `camera_app.py`: live camera UI and automatic background reading.
- `student_numbers.py`: OCR engine and four number patterns.
- `card_detector.py`: optional card outline, crop and synthetic sample cards.

Dependencies/models are installed locally on this computer. On another computer, use
Python 3.12 with Tkinter:

```powershell
python -m pip install -r requirements.txt
python camera_app.py
```

Do not copy `.packages` between Python versions. First installation needs internet;
normal reading uses bundled local models, without an API key. For camera failures,
close other camera apps and check Windows desktop camera permissions.

Tests use synthetic data and do not open a physical camera:

```powershell
python test_detector.py
python test_student_numbers.py
python test_ocr_quality.py
python test_kiosk_scanner.py
```

The camera requests 1080p and autofocus when supported. OCR uses the original camera
resolution/card crop, separately from the smaller live preview. Synthetic robustness
tests cover blur, dim lighting, smaller text, rotation and glare hiding digits; these
do not establish an accuracy percentage for real student IDs or your webcam.

References: [RapidOCR](https://github.com/RapidAI/RapidOCR),
[OpenCV contours](https://docs.opencv.org/4.x/dd/d49/tutorial_py_contour_features.html).
