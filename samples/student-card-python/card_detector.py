"""Small, local OpenCV card detector. No OCR, face recognition, or school classifier."""
from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).resolve().parent / ".packages"))
import cv2
import numpy as np


def find_card(frame):
    """Return four corners of the best card-shaped candidate, or None.

    This uses edges, size, aspect ratio and angles. A similarly shaped object can
    match; it does NOT determine whether an object is actually a student ID.
    """
    height, width = frame.shape[:2]
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    gray = cv2.GaussianBlur(gray, (5, 5), 0)
    edges = cv2.Canny(gray, 45, 135)
    edges = cv2.morphologyEx(edges, cv2.MORPH_CLOSE, np.ones((3, 3), np.uint8))
    contours, _ = cv2.findContours(edges, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    best, best_score = None, 0
    for contour in sorted(contours, key=cv2.contourArea, reverse=True)[:60]:
        area = cv2.contourArea(contour)
        if not 0.035 * width * height < area < 0.78 * width * height:
            continue
        corners = cv2.approxPolyDP(contour, 0.025 * cv2.arcLength(contour, True), True)
        if len(corners) != 4 or not cv2.isContourConvex(corners):
            continue
        points = corners.reshape(4, 2).astype(np.float32)
        # Reject the camera frame border and partly off-screen objects.
        if (points[:, 0].min() < 5 or points[:, 1].min() < 5
                or points[:, 0].max() > width - 6 or points[:, 1].max() > height - 6):
            continue
        _, (rw, rh), _ = cv2.minAreaRect(points)
        if min(rw, rh) < 25:
            continue
        ratio = max(rw, rh) / min(rw, rh)
        fill = area / (rw * rh)
        if not 1.30 <= ratio <= 1.95 or fill < 0.72:
            continue
        # Moderate perspective is allowed; strongly skewed quadrilaterals are not.
        valid_angles = True
        for i in range(4):
            a = points[(i - 1) % 4] - points[i]
            b = points[(i + 1) % 4] - points[i]
            cosine = np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b))
            if abs(cosine) > 0.58:
                valid_angles = False
                break
        if not valid_angles:
            continue
        score = area * fill / (1 + abs(ratio - 1.586))
        if score > best_score:
            best, best_score = points, score
    return best


def crop_card(frame, points):
    """Straighten the quadrilateral for the live detected-card preview."""
    center = points.mean(axis=0)
    angles = np.arctan2(points[:, 1] - center[1], points[:, 0] - center[0])
    ordered = points[np.argsort(angles)]
    ordered = np.roll(ordered, -np.argmin(ordered.sum(axis=1)), axis=0)
    tl, tr, br, bl = ordered
    width = int(max(np.linalg.norm(tr - tl), np.linalg.norm(br - bl)))
    height = int(max(np.linalg.norm(bl - tl), np.linalg.norm(br - tr)))
    target = np.array([[0, 0], [width - 1, 0], [width - 1, height - 1], [0, height - 1]], np.float32)
    matrix = cv2.getPerspectiveTransform(ordered.astype(np.float32), target)
    return cv2.warpPerspective(frame, matrix, (width, height))


class CardTracker:
    """Require several nearby detections to reduce flashing outlines."""
    def __init__(self, required_frames=5):
        self.required_frames = required_frames
        self.previous = None
        self.count = 0

    def update(self, points):
        if points is None:
            self.previous, self.count = None, 0
            return False
        box = cv2.boundingRect(points)
        overlap = 0
        if self.previous is not None:
            x, y, w, h = box
            px, py, pw, ph = self.previous
            common = max(0, min(x + w, px + pw) - max(x, px)) * max(0, min(y + h, py + ph) - max(y, py))
            overlap = common / (w * h + pw * ph - common)
        self.count = self.count + 1 if overlap > 0.60 else 1
        self.previous = box
        return self.count >= self.required_frames


def annotate(frame, points, stable):
    output = frame.copy()
    if points is not None:
        color = (76, 185, 45) if stable else (0, 190, 245)
        cv2.polylines(output, [points.astype(np.int32)], True, color, 3, cv2.LINE_AA)
        x, y = points.min(axis=0).astype(int)
        cv2.putText(output, "Card detected" if stable else "Hold steady", (x, max(26, y - 12)),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.65, color, 2, cv2.LINE_AA)
    return output


def demo_frame(tick=0, school="PUP"):
    """Synthetic moving rectangle used only by demo mode and tests."""
    frame = np.full((540, 960, 3), (52, 42, 33), np.uint8)
    dx = int(35 * np.sin(tick / 35))
    polygon = np.array([[220 + dx, 150], [655 + dx, 125], [675 + dx, 400], [235 + dx, 425]], np.int32)
    cv2.fillConvexPoly(frame, polygon, (246, 247, 249))
    from student_numbers import EXAMPLES
    cv2.putText(frame, school + " SAMPLE", (265 + dx, 210), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (95, 60, 35), 2)
    cv2.putText(frame, EXAMPLES[school], (265 + dx, 285), cv2.FONT_HERSHEY_SIMPLEX, 0.85, (20, 20, 20), 2, cv2.LINE_AA)
    cv2.putText(frame, "Not a real student ID", (265 + dx, 345), cv2.FONT_HERSHEY_SIMPLEX, 0.55, (95, 60, 35), 1)
    return frame
