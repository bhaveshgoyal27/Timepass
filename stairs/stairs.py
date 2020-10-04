import cv2
import math
import numpy as np

cap = cv2.VideoCapture('up.mp4')
fourcc = cv2.VideoWriter_fourcc(*'XVID')
out = cv2.VideoWriter('up1.avi',fourcc,20.0,(512,512))
e=10
while(cap.isOpened()):
    ret,img = cap.read()
    if img is not None:
        img = cv2.resize(img,(512,512),interpolation=cv2.INTER_AREA)
        img = cv2.rotate(img, cv2.cv2.ROTATE_90_CLOCKWISE) 
        img1 = cv2.GaussianBlur(img,(3,3),cv2.BORDER_DEFAULT)

        img2 = cv2.Canny(img1,100,180,3)

        lines=cv2.HoughLinesP(img2,1,np.pi/180,30,minLineLength=100,maxLineGap=0.5)
        c=0
        if lines is not None:
            for line in lines:
                x1,y1,x2,y2=line[0]
                d = math.atan2(abs(y2-y1),abs(x2-x1))
                if d<0.04:
                    cv2.line(img,(x1,y1),(x2,y2),(0,0,255),2)
                    c+=1
            #c = c//7
            #if c == min(c,e):
            #    c = e-c
            #e=c
            img = cv2.putText(img,'steps:'+str(c),(10,50),cv2.FONT_HERSHEY_SIMPLEX,1,(255, 0, 0) ,2, cv2.LINE_AA)
            print(c)
            out.write(img)
        else:
            out.write(img)
cap.release()
out.release()
cv2.destroyAllWindows()
