"""Live camera preview with automatic student-number recognition (Python/Tkinter)."""
from pathlib import Path
import argparse
import queue
import sys
import threading
import time
from concurrent.futures import ThreadPoolExecutor
import tkinter as tk
from tkinter import ttk, filedialog, messagebox

sys.path.insert(0, str(Path(__file__).resolve().parent / ".packages"))
import cv2
import numpy as np
from PIL import Image, ImageTk
from card_detector import find_card, crop_card, annotate, CardTracker, demo_frame
from student_numbers import StudentNumberReader, NumberConsensus, EXAMPLES


class CardCameraApp:
    def __init__(self, root, kiosk=False):
        self.kiosk = kiosk
        self.accepted_number = None
        self.result_sent = False
        self.root = root
        self.worker = None
        self.cancel = threading.Event()
        self.messages = queue.Queue(maxsize=2)
        self.session = 0
        self.closing = False
        self.frame = self.crop = None
        self.photos = {}
        self.ocr_pool = ThreadPoolExecutor(max_workers=1, thread_name_prefix="student-number")
        self.ocr_future = None
        self.reader = None
        self.ocr_source = None
        self.last_read = 0
        self.ocr_session = 0
        self.still_image = False
        self.last_result_time = 0
        self.consensus = NumberConsensus(required=3 if kiosk else 2)
        root.title("Bussin · Scan Student ID")
        root.geometry("1160x760")
        root.minsize(940, 680)
        root.configure(bg="#f4f6f9")
        root.protocol("WM_DELETE_WINDOW", self.close)
        style = ttk.Style()
        style.theme_use("clam")
        style.configure("TFrame", background="#f4f6f9")
        style.configure("TLabel", background="#f4f6f9", foreground="#263245", font=("Segoe UI", 11))
        style.configure("TButton", font=("Segoe UI", 10, "bold"), padding=(12, 8))
        style.configure("Title.TLabel", font=("Segoe UI", 23, "bold"))
        body = ttk.Frame(root, padding=22)
        body.pack(fill="both", expand=True)
        ttk.Label(body, text="Student ID number detection", style="Title.TLabel").pack(anchor="w")
        ttk.Label(body, text="Hold the number clearly in view. Automatically reads PUP, NU MOA, NU APC and FEU Tech formats.").pack(anchor="w", pady=(5, 18))
        controls = ttk.Frame(body)
        controls.pack(fill="x", pady=(0, 16))
        ttk.Label(controls, text="Camera").pack(side="left", padx=(0, 8))
        self.index = tk.StringVar(value="0")
        self.camera_choice = ttk.Combobox(controls, textvariable=self.index, values=[str(i) for i in range(6)], state="readonly", width=4)
        self.camera_choice.pack(side="left", padx=(0, 12))
        self.start_button = ttk.Button(controls, text="Start camera", command=self.start_camera)
        self.start_button.pack(side="left", padx=(0, 8))
        self.stop_button = ttk.Button(controls, text="Stop", command=self.stop, state="disabled")
        self.stop_button.pack(side="left", padx=(0, 8))
        self.demo_button = ttk.Button(controls, text="Try demo", command=lambda: self.start(True))
        self.demo_button.pack(side="left", padx=(0, 8))
        self.demo_school = tk.StringVar(value="PUP")
        self.demo_choice = ttk.Combobox(controls, textvariable=self.demo_school, values=list(EXAMPLES), state="readonly", width=10)
        self.demo_choice.pack(side="left", padx=(0, 8))
        self.image_button = ttk.Button(controls, text="Test image", command=self.open_image)
        self.image_button.pack(side="left")
        if kiosk:
            for control in (self.demo_button, self.demo_choice, self.image_button):
                control.pack_forget()
        content = ttk.Frame(body)
        content.pack(fill="both", expand=True)
        content.columnconfigure(0, weight=3)
        content.columnconfigure(1, weight=1)
        content.rowconfigure(1, weight=1)
        self.mode = ttk.Label(content, text="CAMERA OFF")
        self.mode.grid(row=0, column=0, sticky="w", pady=(0, 8))
        ttk.Label(content, text="DETECTED CARD · LIVE CROP").grid(row=0, column=1, sticky="w", padx=(18, 0), pady=(0, 8))
        video_container = tk.Frame(content, bg="#192333", width=520, height=400)
        video_container.grid(row=1, column=0, sticky="nsew")
        video_container.pack_propagate(False)
        self.preview = tk.Label(video_container, bg="#192333", fg="#c6d0df", text="Click Start camera", font=("Segoe UI", 15))
        self.preview.pack(fill="both", expand=True)
        sidebar = ttk.Frame(content, width=285)
        sidebar.grid(row=1, column=1, sticky="nsew", padx=(18, 0))
        sidebar.grid_propagate(False)
        sidebar.columnconfigure(0, weight=1)
        crop_container = tk.Frame(sidebar, bg="#e5eaf1", width=285, height=145)
        crop_container.grid(row=0, column=0, sticky="ew")
        crop_container.pack_propagate(False)
        self.card_preview = tk.Label(crop_container, bg="#e5eaf1", text="Waiting for a card", fg="#536176")
        self.card_preview.pack(fill="both", expand=True)
        self.state = tk.StringVar(value="Ready")
        ttk.Label(sidebar, textvariable=self.state, font=("Segoe UI", 14, "bold"), wraplength=255).grid(row=1, column=0, sticky="w", pady=(16, 10))
        self.school_result = tk.StringVar(value="Format: —")
        self.number_result = tk.StringVar(value="Waiting for an ID number")
        ttk.Label(sidebar, textvariable=self.school_result, font=("Segoe UI", 12, "bold")).grid(row=2, column=0, sticky="w")
        ttk.Label(sidebar, textvariable=self.number_result, font=("Consolas", 16, "bold"), wraplength=270).grid(row=3, column=0, sticky="w", pady=(6, 10))
        self.reading_status = tk.StringVar(value="Automatic reading starts with the camera.")
        ttk.Label(sidebar, textvariable=self.reading_status, wraplength=270).grid(row=4, column=0, sticky="w")
        ttk.Label(sidebar, text="Latest text read:").grid(row=5, column=0, sticky="w", pady=(12, 4))
        self.raw_text = tk.Text(sidebar, height=4, width=30, wrap="word", font=("Segoe UI", 9), state="disabled")
        self.raw_text.grid(row=6, column=0, sticky="ew")
        ttk.Label(sidebar, text="Check the number against the card.\nFormat matching does not verify enrollment.", wraplength=270).grid(row=7, column=0, sticky="w", pady=(10, 0))
        self.use_button = ttk.Button(controls, text="Use this number", command=self.use_number, state="disabled")
        self.status = tk.StringVar(value="Local processing · automatic number reading · no uploads or saved camera images")
        ttk.Label(body, textvariable=self.status, wraplength=1050).pack(anchor="w", pady=(16, 0))
        if kiosk:
            body.pack_forget()
            self.build_kiosk(root)
        root.after(40, self.poll)

    def build_kiosk(self, root):
        """Commuter camera screen; diagnostic/demo controls stay out of the kiosk."""
        red, background = "#e1002d", "#f8f8f8"
        root.geometry("1000x720")
        root.minsize(800, 650)
        root.configure(bg=background)
        root.bind("<Escape>", lambda event: self.close())
        header = tk.Frame(root, bg=red, height=80)
        header.pack(fill="x")
        header.pack_propagate(False)
        tk.Label(header, text="Bussin", bg=red, fg="white", font=("Segoe UI", 25, "bold")).pack(side="left", padx=32)
        tk.Label(header, text="STUDENT ID", bg=red, fg="white", font=("Segoe UI", 12, "bold")).pack(side="right", padx=32)
        body = tk.Frame(root, bg=background)
        body.pack(fill="both", expand=True, padx=36, pady=(18, 10))
        tk.Label(body, text="Scan Student ID", bg=background, fg=red,
                 font=("Segoe UI", 27, "bold")).pack()
        tk.Label(body, text="Hold one ID steady with the number clearly visible.", bg=background,
                 fg="#555e6c", font=("Segoe UI", 12)).pack(pady=(5, 16))
        video = tk.Frame(body, bg="#192333", highlightbackground="#dce1e8", highlightthickness=1)
        video.pack(fill="both", expand=True)
        self.preview = tk.Label(video, bg="#192333", fg="white", text="Opening camera…",
                                font=("Segoe UI", 16))
        self.preview.pack(fill="both", expand=True)
        tk.Label(body, textvariable=self.number_result, bg=background, fg=red,
                 font=("Segoe UI", 17, "bold")).pack(pady=(12, 3))
        tk.Label(body, textvariable=self.reading_status, bg=background, fg="#555e6c",
                 font=("Segoe UI", 11), wraplength=740).pack()
        tk.Label(body, text="Your number will fill in automatically. You can edit it afterward.",
                 bg=background, fg="#555e6c", font=("Segoe UI", 11)).pack(pady=(5, 8))
        footer = tk.Frame(root, bg="#f0f3f5", height=58)
        footer.pack(fill="x")
        tk.Button(footer, text="Cancel", command=self.close, bg="#f0f3f5", fg="#263245",
                  relief="flat", font=("Segoe UI", 13), pady=12, cursor="hand2").pack(side="left", fill="x", expand=True)
        self.retry_button = tk.Button(footer, text="Try camera again", command=self.start_camera,
                 bg=red, fg="white", relief="flat", font=("Segoe UI", 13, "bold"), pady=12, cursor="hand2")
        root.after(150, lambda: (root.lift(), root.focus_force()) if not self.closing else None)

    def use_number(self):
        if self.closing or self.accepted_number is None or time.monotonic() - self.last_result_time > 4:
            return
        self.result_sent = True
        print("BUSSIN_STUDENT_ID=" + self.accepted_number.number, flush=True)
        self.close()

    def start_camera(self):
        if self.kiosk:
            self.retry_button.pack_forget()
        self.start(False)

    def start(self, demo=False):
        if self.worker and self.worker.is_alive():
            return
        self.session += 1
        self.cancel = threading.Event()
        self.clear_images()
        self.still_image = False
        self.state.set("Opening camera..." if not demo else "Demo running")
        self.mode.configure(text="DEMO — SYNTHETIC CARD" if demo else f"LIVE CAMERA {self.index.get()}")
        self.set_running(True)
        self.worker = threading.Thread(target=self.stream, args=(int(self.index.get()), demo, self.session, self.cancel, self.demo_school.get()), daemon=True)
        self.worker.start()

    def publish(self, item):
        try:
            self.messages.put_nowait(item)
        except queue.Full:
            try:
                self.messages.get_nowait()
            except queue.Empty:
                pass
            self.messages.put_nowait(item)

    def stream(self, index, demo, session, cancel, demo_school="PUP"):
        """Camera access and processing stay off Tk's UI thread."""
        camera = None
        try:
            if not demo:
                backends = [cv2.CAP_DSHOW, cv2.CAP_MSMF] if sys.platform == "win32" else [cv2.CAP_ANY]
                for backend in backends:
                    if cancel.is_set():
                        return
                    camera = cv2.VideoCapture(index, backend)
                    if camera.isOpened():
                        break
                    camera.release()
                if camera is None or not camera.isOpened():
                    raise RuntimeError("Cannot open this camera. Try camera 1, close other camera apps, or check Windows camera permissions.")
                camera.set(cv2.CAP_PROP_FRAME_WIDTH, 1920)
                camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 1080)
                camera.set(cv2.CAP_PROP_AUTOFOCUS, 1)
            tracker = CardTracker()
            tick = failures = 0
            while not cancel.is_set():
                if demo:
                    ok, frame = True, demo_frame(tick, demo_school)
                else:
                    ok, frame = camera.read()
                if not ok or frame is None:
                    failures += 1
                    if failures >= 25:
                        raise RuntimeError("Camera stopped sending frames. Reconnect it and try again.")
                    cancel.wait(0.05)
                    continue
                failures = 0
                original = frame
                if frame.shape[1] > 960:
                    frame = cv2.resize(frame, (960, round(frame.shape[0] * 960 / frame.shape[1])))
                points = find_card(frame)
                stable = tracker.update(points)
                crop = crop_card(frame, points) if stable else None
                state = "Card detected" if stable else ("Hold steady..." if points is not None else "Looking for a card...")
                # Read a steady card crop when available; otherwise read the whole frame.
                # A number can be recognized even if the card's edges are obscured.
                source = original
                if stable:
                    scale = np.array([original.shape[1] / frame.shape[1], original.shape[0] / frame.shape[0]], np.float32)
                    source = crop_card(original, points * scale)
                self.publish((session, "frame", (annotate(frame, points, stable), crop, state, source)))
                tick += 1
                cancel.wait(0.035)
        except Exception as error:
            self.publish((session, "error", str(error)))
        finally:
            if camera is not None:
                camera.release()

    def set_running(self, active):
        for button in (self.start_button, self.demo_button, self.image_button):
            button.configure(state="disabled" if active else "normal")
        self.stop_button.configure(state="normal" if active else "disabled")
        self.camera_choice.configure(state="disabled" if active else "readonly")
        self.demo_choice.configure(state="disabled" if active else "readonly")

    def stop(self):
        self.cancel.set()
        self.session += 1  # Ignore any old frames already queued by the worker.
        self.clear_images()
        self.state.set("Camera stopped")
        self.mode.configure(text="CAMERA OFF")
        self.stop_button.configure(state="disabled")
        self.status.set("Releasing camera...")

    def poll(self):
        if self.closing:
            return
        try:
            while True:
                session, kind, payload = self.messages.get_nowait()
                if session != self.session:
                    continue
                if kind == "error":
                    self.clear_images()
                    self.state.set("Camera unavailable")
                    self.status.set(payload)
                    self.mode.configure(text="CAMERA OFF")
                    if self.kiosk:
                        self.number_result.set("Camera unavailable")
                        self.reading_status.set("Close other camera apps, check camera permissions, then try again.")
                        self.retry_button.pack(side="right", fill="x", expand=True)
                else:
                    self.frame, self.crop, state, self.ocr_source = payload
                    self.state.set(state)
                    self.status.set("Automatic detection active · No images are uploaded or saved")
        except queue.Empty:
            pass
        if self.worker and not self.worker.is_alive():
            self.worker = None
            self.set_running(False)
            if self.cancel.is_set():
                self.status.set("Camera released. Start again whenever you are ready.")
        self.show_image(self.preview, self.frame, "Click Start camera")
        self.show_image(self.card_preview, self.crop, "Waiting for a steady card")
        self.poll_numbers()
        if not self.closing:
            self.root.after(40, self.poll)

    def read_numbers(self, frame):
        if self.reader is None:
            self.reader = StudentNumberReader(min_confidence=0.92 if self.kiosk else 0.85)
        return self.reader.read(frame)

    def poll_numbers(self):
        now = time.monotonic()
        if self.ocr_future is not None and self.ocr_future.done():
            future = self.ocr_future
            self.ocr_future = None
            if self.ocr_session == self.session and self.ocr_source is not None:
                try:
                    matches, text = future.result()
                    self.raw_text.configure(state="normal")
                    self.raw_text.delete("1.0", "end")
                    self.raw_text.insert("1.0", text or "No readable text yet")
                    self.raw_text.configure(state="disabled")
                    match = self.consensus.update(matches)
                    self.accepted_number = match
                    self.use_button.configure(state="normal" if match is not None else "disabled")
                    if match is not None:
                        self.school_result.set("Format: " + match.school)
                        self.number_result.set(match.number)
                        self.reading_status.set("Repeated readings agree — check against the ID.")
                    elif len(matches) == 1:
                        self.school_result.set("Checking number...")
                        self.number_result.set("Hold steady to confirm")
                        self.reading_status.set("Keep still while repeated readings are checked.")
                    elif len(matches) > 1:
                        self.school_result.set("Multiple numbers found")
                        self.number_result.set("Show one student ID at a time")
                        self.reading_status.set("Move closer to the correct ID number.")
                    else:
                        self.school_result.set("Format: —")
                        self.number_result.set("No matching number yet")
                        self.reading_status.set("Hold the number closer, steady and well lit.")
                    self.last_result_time = now
                    if self.kiosk and match is not None:
                        self.use_number()
                        return
                except Exception as error:
                    self.accepted_number = None
                    self.use_button.configure(state="disabled")
                    self.consensus.reset()
                    self.school_result.set("Format: —")
                    self.number_result.set("Number reader unavailable")
                    self.reading_status.set(str(error)[:160])
        # Expire old readings instead of leaving a previous person's number visible.
        if not self.still_image and self.last_result_time and now - self.last_result_time > 4:
            self.accepted_number = None
            self.use_button.configure(state="disabled")
            self.consensus.reset()
            self.school_result.set("Format: —")
            self.number_result.set("Reading current frame...")
            self.last_result_time = 0
        if self.ocr_source is not None and self.ocr_future is None and now - self.last_read >= 1.2:
            if not self.last_result_time:
                self.reading_status.set("Reading the number automatically...")
            self.last_read = now
            self.ocr_session = self.session
            self.ocr_future = self.ocr_pool.submit(self.read_numbers, self.ocr_source.copy())

    def show_image(self, label, frame, placeholder):
        if frame is None:
            label.configure(image="", text=placeholder)
            self.photos.pop(label, None)
            return
        image = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
        image.thumbnail((max(1, label.winfo_width() - 8), max(1, label.winfo_height() - 8)))
        photo = ImageTk.PhotoImage(image)
        self.photos[label] = photo
        label.configure(image=photo, text="")

    def clear_images(self):
        self.accepted_number = None
        self.use_button.configure(state="disabled")
        self.frame = self.crop = None
        self.consensus.reset()
        self.ocr_source = None
        self.school_result.set("Format: —")
        self.number_result.set("Waiting for an ID number")
        self.reading_status.set("Automatic reading starts with the camera.")
        self.raw_text.configure(state="normal")
        self.raw_text.delete("1.0", "end")
        self.raw_text.configure(state="disabled")
        self.last_result_time = 0
        self.last_read = 0

    def open_image(self):
        path = filedialog.askopenfilename(filetypes=[("Images", "*.png *.jpg *.jpeg *.bmp")])
        if not path:
            return
        try:
            self.session += 1
            self.clear_images()
            self.still_image = True
            with Image.open(path) as image:
                image.thumbnail((2200, 2200))
                frame = cv2.cvtColor(np.array(image.convert("RGB")), cv2.COLOR_RGB2BGR)
            points = find_card(frame)
            self.frame = annotate(frame, points, points is not None)
            self.crop = crop_card(frame, points) if points is not None else None
            self.ocr_source = self.crop if self.crop is not None else frame
            self.mode.configure(text="STILL IMAGE — CAMERA OFF")
            self.state.set("Card detected" if points is not None else "No card-shaped object found")
            self.status.set("Reading student numbers from this image...")
        except Exception as error:
            messagebox.showerror("Cannot open image", str(error))

    def close(self):
        if self.closing:
            return
        if self.kiosk and not self.result_sent:
            print("BUSSIN_SCAN_CANCELLED", flush=True)
        self.closing = True
        self.cancel.set()
        self.session += 1
        self.ocr_pool.shutdown(wait=False, cancel_futures=True)
        self.clear_images()
        # Wait briefly without blocking Tk so VideoCapture can release the device.
        deadline = time.monotonic() + 3
        def finish():
            if self.worker and self.worker.is_alive() and time.monotonic() < deadline:
                self.root.after(50, finish)
            else:
                self.root.destroy()
        finish()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--demo", action="store_true", help="Use a synthetic moving card; camera stays off")
    parser.add_argument("--kiosk", action="store_true", help="Scan for the kiosk; return a reviewed number via stdout")
    args = parser.parse_args()
    if args.kiosk and args.demo:
        parser.error("Demo mode is not available for kiosk scanning")
    root = tk.Tk()
    app = CardCameraApp(root, kiosk=args.kiosk)
    if args.kiosk:
        root.after(100, app.start_camera)
    elif args.demo:
        root.after(100, lambda: app.start(True))
    root.mainloop()


if __name__ == "__main__":
    main()
