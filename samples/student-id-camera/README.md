# Student ID camera sample (Java)

Double-click **run-camera-test.cmd**, choose your webcam, and click **Start camera**.
Hold the student ID upright, close enough that its number is readable, with even lighting.
Click **Capture & read**, or enable **Auto-read every 3 seconds**.
The captured image, OCR text, and matching student-number format appear on the right.
Correct OCR mistakes in the text box and click **Check edited text** if needed.

**Open image** reads a PNG/JPG/BMP instead of using the camera. Use `test-sheet.png`
to try the four supplied samples (open it from another screen or print it for a camera test).
**Stop camera** releases the webcam. Closing the window also releases it.

## Sample formats

| Format label | Example | Pattern used in this demo |
|---|---|---|
| PUP | 2025-09011-MN-0 | 20xx + 5 digits + 2 letters + 1 digit |
| NU MOA | 2025-1058827 | 20xx + 7 digits |
| NU APC | 2025-140065 | 20xx + 6 digits |
| FEU TECH | 202511062 | 9 digits beginning with 20 |

These rules are inferred from your examples, not official school validation rules.
A matching number does not establish school affiliation, ID authenticity, or current enrollment.
This first sample detects student numbers; name extraction is not implemented yet.
Different formatting or OCR mistakes can produce no match. It does not silently replace letters with digits.
The OCR panel shows a full-text reading and a second reading restricted to digits, hyphens, M and N
(the characters in your supplied examples). Compare both with the actual card. Spaces inside digit
sequences and around hyphens are removed for matching. Other PUP campus codes rely on the full-text reading.

## Files to read

- `src/main/java/StudentIdCameraDemo.java`: Swing window, camera capture, and OCR.
- `src/main/java/StudentIdFormats.java`: the four understandable regular-expression rules.
- `src/main/java/SampleChecks.java`: number parsing tests and real OCR test using synthetic text.
- `pom.xml`: separate dependencies for this sample; the main QueueSystem is untouched.

## Build again

Requires a 64-bit JDK 17+ and Maven. In this directory:

```powershell
mvn package
.\run-camera-test.cmd
```

First build needs internet to download the libraries. OCR then runs locally without an API key.
No ID image/text is uploaded or written to disk by the scanner. The self-test creates only the synthetic test sheet.

Run tests without opening the camera:

```powershell
.\run-camera-test.cmd --self-test
```

If the camera fails, close other programs using it and check Windows Settings → Privacy & security → Camera →
Let desktop apps access your camera. Relaunch the sample after plugging in a different webcam.
If OCR reports a missing DLL/runtime, install the Microsoft Visual C++ 2015–2022 x64 Redistributable,
as described in the [Tess4J setup guide](https://tess4j.sourceforge.net/tutorial/).

Libraries: [Webcam Capture](https://github.com/sarxos/webcam-capture),
[Tess4J / Tesseract OCR](https://github.com/nguyenq/tess4j).
