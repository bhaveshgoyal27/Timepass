import time
import numpy as np
import cv2
from gaze_tracking import GazeTracking
fourcc = cv2.VideoWriter_fourcc(*'XVID')
out = cv2.VideoWriter('liedetector.avi',fourcc,20.0,(512,512))

flag=1
gaze = GazeTracking()
webcam = cv2.VideoCapture(0)
x=[]
y=[]
a=time.time()
while True:
    # We get a new frame from the webcam
    _, frame = webcam.read()
    # We send this frame to GazeTracking to analyze it
    gaze.refresh(frame)

    frame = gaze.annotated_frame()
    text = ""

    if gaze.is_blinking():
        text = "Blinking"
    elif gaze.is_right():
        text = "Looking right"
    elif gaze.is_left():
        text = "Looking left"
    elif gaze.is_center():
        text = "Looking center"
    
    cv2.putText(frame, text, (90, 60), cv2.FONT_HERSHEY_DUPLEX, 1.6, (147, 58, 31), 2)
    
    left_pupil = gaze.pupil_left_coords()
    right_pupil = gaze.pupil_right_coords()
    if left_pupil!=None:
        x.append(left_pupil)
    cv2.putText(frame, "Left pupil:  " + str(left_pupil), (90, 130), cv2.FONT_HERSHEY_DUPLEX, 0.9, (147, 58, 31), 1)
    cv2.putText(frame, "Right pupil: " + str(right_pupil), (90, 165), cv2.FONT_HERSHEY_DUPLEX, 0.9, (147, 58, 31), 1)
    frame = cv2.resize(frame,(512,512),interpolation=cv2.INTER_AREA)
    cv2.imshow("Demo", frame)
    out.write(frame)
    b=time.time()
    if(b-a>15) and flag==1:
        print(x)
        x1=np.asarray(x)
        print(x1)
        stdx=x1.std()
        cv2.putText(frame, "LIE", (90, 165), cv2.FONT_HERSHEY_DUPLEX, 0.9, (147, 58, 31), 1)
        frame = cv2.resize(frame,(512,512),interpolation=cv2.INTER_AREA)
        cv2.imshow("Demo", frame)
        out.write(frame)
        print(stdx)
        flag=0
    if cv2.waitKey(1) == 27:
        break
out.release()