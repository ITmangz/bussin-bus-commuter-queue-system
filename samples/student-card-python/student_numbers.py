"""Read student numbers locally, then match the four user-supplied formats."""
from dataclasses import dataclass
from pathlib import Path
import re
import sys

sys.path.insert(0, str(Path(__file__).resolve().parent / ".packages"))
import cv2
from collections import Counter

EXAMPLES = {
    "PUP": "2025-09011-MN-0",
    "NU MOA": "2025-1058827",
    "NU APC": "2025-140065",
    "FEU TECH": "202511062",
}
PATTERNS = {
    "PUP": r"20\d{2}-\d{5}-[A-Z]{2}-\d",
    "NU MOA": r"20\d{2}-\d{7}",
    "NU APC": r"20\d{2}-\d{6}",
    "FEU TECH": r"20\d{7}",
}


@dataclass(frozen=True)
class StudentNumber:
    school: str
    number: str


def match_numbers(text):
    """Normalize separator spacing, but never guess or replace an unreadable digit."""
    text = re.sub(r"[\u2010-\u2015\u2212]", "-", text.upper())
    text = re.sub(r"[ \t]*-[ \t]*", "-", text)
    text = re.sub(r"(?<=\d)[ \t]+(?=\d)", "", text)
    found = []
    for school, pattern in PATTERNS.items():
        for match in re.finditer(r"(?<![A-Z0-9-])(" + pattern + r")(?![A-Z0-9-])", text):
            candidate = StudentNumber(school, match.group(1))
            if candidate not in found:
                found.append(candidate)
    return found


class StudentNumberReader:
    """One OCR instance reused by a background worker; the camera stays responsive."""
    def __init__(self):
        from rapidocr_onnxruntime import RapidOCR
        self.engine = RapidOCR(intra_op_num_threads=2, inter_op_num_threads=1)

    def read(self, frame):
        # Preserve camera detail; enlarge small card crops rather than a tiny preview.
        scale = min(2.0, 2200 / max(frame.shape[:2]))
        if abs(scale - 1) > 0.05:
            frame = cv2.resize(frame, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        contrast = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8)).apply(gray)
        enhanced = cv2.addWeighted(contrast, 1.4, cv2.GaussianBlur(contrast, (0, 0), 1), -0.4, 0)
        versions = [frame, cv2.cvtColor(enhanced, cv2.COLOR_GRAY2BGR)]
        votes = Counter()
        readings = []
        for index in range(3):
            if index == 2:
                # Only spend time on a third view when the first two do not agree.
                if len(votes) == 1 and max(votes.values()) == 2:
                    break
                binary = cv2.adaptiveThreshold(gray, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
                                               cv2.THRESH_BINARY, 41, 11)
                versions.append(cv2.cvtColor(binary, cv2.COLOR_GRAY2BGR))
            rows, _ = self.engine(versions[index])
            rows = rows or []
            readings.append("\n".join(str(row[1]) for row in rows))
            reliable = "\n".join(str(row[1]) for row in rows if float(row[2]) >= 0.85)
            votes.update(set(match_numbers(reliable)))
        # Disagreeing valid numbers stay ambiguous instead of guessing a winner.
        if len(votes) > 1:
            matches = list(votes)
        else:
            matches = [number for number, count in votes.items() if count >= 2]
        text = "\n\n".join(f"Reading {i + 1}:\n{text or 'No readable text'}" for i, text in enumerate(readings))
        if not matches:
            text += "\n\nNo consistent reading. Hold still, move closer, and avoid glare."
        return matches, text


class NumberConsensus:
    """Require two successive OCR jobs to agree; clear immediately on disagreement."""
    def __init__(self, required=2):
        self.required = required
        self.reset()

    def reset(self):
        self.candidate = None
        self.count = 0

    def update(self, matches):
        if len(matches) != 1:
            self.reset()
            return None
        candidate = matches[0]
        self.count = self.count + 1 if candidate == self.candidate else 1
        self.candidate = candidate
        return candidate if self.count >= self.required else None
