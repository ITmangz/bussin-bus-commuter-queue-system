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


def reliable_text(rows, min_confidence=0.85):
    """Rejoin nearby fragments on the same printed line without inventing characters.

    Low-confidence boxes break a line, so an unreadable digit cannot disappear
    between two accepted fragments. Keep original boxes too to expose conflicts.
    """
    result = [str(row[1]) for row in rows if float(row[2]) >= min_confidence]
    boxes = []
    for points, text, confidence in rows:
        if len(points) != 4:
            continue
        xs, ys = [p[0] for p in points], [p[1] for p in points]
        boxes.append((min(xs), min(ys), max(xs), max(ys), str(text), float(confidence)))
    lines = []
    for box in sorted(boxes, key=lambda b: (b[1], b[0])):
        center = (box[1] + box[3]) / 2
        for line in lines:
            anchor = line[0]
            if abs(center - (anchor[1] + anchor[3]) / 2) <= 0.35 * min(box[3] - box[1], anchor[3] - anchor[1]):
                line.append(box)
                break
        else:
            lines.append([box])
    for line in lines:
        fragments, previous = [], None
        for box in sorted(line, key=lambda b: b[0]):
            separated = previous is not None and box[0] - previous[2] > 1.5 * max(box[3] - box[1], previous[3] - previous[1])
            if box[5] < min_confidence or separated:
                if len(fragments) > 1:
                    result.append(" ".join(fragments))
                fragments = []
            if box[5] >= min_confidence:
                fragments.append(box[4])
            previous = box
        if len(fragments) > 1:
            result.append(" ".join(fragments))
    return "\n".join(result)


class StudentNumberReader:
    """One OCR instance reused by a background worker; the camera stays responsive."""
    def __init__(self, min_confidence=0.85):
        self.min_confidence = min_confidence
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
            reliable = reliable_text(rows, getattr(self, "min_confidence", 0.85))
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
