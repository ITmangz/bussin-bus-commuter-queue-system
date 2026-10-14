import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parent))
from student_numbers import StudentNumberReader, EXAMPLES, StudentNumber
from card_detector import demo_frame, find_card, crop_card, cv2, np
reader=StudentNumberReader(min_confidence=0.92 if '--kiosk' in sys.argv else 0.85)
counts = {'correct':0, 'withheld':0, 'wrong':0}
for school, number in EXAMPLES.items():
    frame=demo_frame(0,school)
    card=crop_card(frame,find_card(frame))
    h,w=card.shape[:2]
    rotation=cv2.getRotationMatrix2D((w/2,h/2), 7, 1)
    glare=card.copy()
    cv2.rectangle(glare,(145,90),(260,160),(255,255,255),-1)
    cases={'blur':cv2.GaussianBlur(card,(7,7),1.2), 'dim':(card*.4).astype(np.uint8),
           'small':cv2.resize(card,None,fx=.5,fy=.5),
           'rotated':cv2.warpAffine(card,rotation,(w,h),borderValue=(255,255,255)), 'glare':glare}
    for name, image in cases.items():
        matches,text=reader.read(image)
        expected=StudentNumber(school,number)
        state='correct' if matches==[expected] else 'withheld' if not matches else 'wrong'
        counts[state]+=1
        assert state != 'wrong', (school, name, matches)
        if name != 'glare':
            assert state == 'correct', (school, name, text)
        print(school,name,state,matches,flush=True)
print('TOTAL',counts)

