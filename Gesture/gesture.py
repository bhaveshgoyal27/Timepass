import cv2
import numpy as np
import math
import time
'''img=cv2.imread('/Users/sahaaj/Downloads/pics/IMG_5267.jpg-e1558184821358.jpg')
img=cv2.resize(img,(400,600))
img=cv2.cvtColor(img,cv2.COLOR_BGR2HSV)'''
fourcc = cv2.VideoWriter_fourcc(*'XVID')
out = cv2.VideoWriter('gesture.avi',fourcc,20.0,(512,512))
def draw_contours(img,lb,ub,c1,a,b):
    mask=cv2.inRange(img,lb,ub)
    ker=np.ones((7,7))
    mask=cv2.erode(mask,ker,iterations=a)
    mask=cv2.dilate(mask,ker,iterations=b)
    c,_=cv2.findContours(mask, cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)
    main=0
    for cnt in c:
        if main!=0:
            break
        a=cv2.moments(cnt)
        cX = int(a["m10"]/a["m00"])
        cY = int(a["m01"]/a["m00"])
        cv2.circle(imga,(cX,cY),7,(255,255,255),-1)
        cv2.drawContours(imga,c,-1,c1,3)
        lista.append((cX,cY))
        main+=1
def movements():
    count =0
    c=0
    l=0
    if(len(lista)!=len(listap)):
        return 0
    for j in range(0,len(lista)):
        if(listap[j]<lista[j]):
            count+=1
        elif (listap[j]>lista[j]):
            c+=1
        l=len(lista[j])
    if(count>(l/2)):
        return 10 # moved right
    elif (c>l/2):
        return 5 # moved left
    else:
        return 1
def close_gesture():
    count =0
    if(len(distap)!=len(distl)):
        return 0
    for j in range(0,len(distl)):
        if(distl[j]>350):
            count+=1
    if(count>0):
        return 10 # opened
    else:
        return 5 # closed


lower_b=np.array([100, 60, 2]) #lower_r=np.array([30, 84, 60]) upper_r=np.array([255, 255, 255])
upper_b=np.array([140, 255, 255])
lower_r=np.array([2, 60, 100])
upper_r=np.array([255, 255, 140])
lower_g=np.array([60,2,100])
upper_g=np.array([255, 255,140]) #40,80,150]) upper_g=np.array([255,255,255])111
lower_y=np.array([23,31,133])
upper_y=np.array([40,150,255])

distap=[]
listap=[]
cap=cv2.VideoCapture(0)
flag=0
f=1
movs=[]
closs=[]
while True:
    if flag==0:
        flag=1
        a=time.time()
    ret,imga=cap.read()
    img=cv2.cvtColor(imga,cv2.COLOR_BGR2HSV)
    lista=[]
    draw_contours(img,lower_b,upper_b,(255,0,0),5,6)# 7,7 kernel erode-5 dilate 6
    #draw_contours(img,lower_r,upper_r,(0,0,255),2,13)
    draw_contours(img,lower_g,upper_g,(0,255,0),5,6)
    #draw_contours(img,lower_y,upper_y,(0,0,255),5,6)
    lista.sort()
    #print(lista)
    #individual line distance between the center of two
    distl=[]
    for i in range(1,len(lista)):
        
        cv2.line(imga,lista[i-1],lista[i],(255,255,255))
        dist=math.sqrt(math.pow(lista[1][0]-lista[0][1],2)+math.pow(lista[1][1]-lista[0][1],2))
        distl.append(dist)
        print(dist)
    
    mov=movements()
    clos=close_gesture()
    if mov!=0:
        movs.append(mov)
    if clos!=0:
        closs.append(clos)
    b=time.time()
    if(b-a>1):
        flag=0
        f=0
        m=sum(movs)//len(movs)
        c=sum(closs)//len(closs)
        movs=[]
        closs=[]
    if(f==0):
        if m==10:
            cv2.putText(imga,'MOVED RIGHT',(50,50),cv2.FONT_HERSHEY_SIMPLEX, fontScale = 1,color = (255,255,2555),thickness = 2)
        elif m==5:
            cv2.putText(imga,'MOVED LEFT',(50,50),cv2.FONT_HERSHEY_SIMPLEX, fontScale = 1,color = (255, 255,255),thickness = 2)
        else:
            cv2.putText(imga,'DID NOT MOVE',(50,50),cv2.FONT_HERSHEY_SIMPLEX, fontScale = 1,color = (255, 255, 255),thickness = 2)
        if c==10:
            cv2.putText(imga,'OPENED',(80,100),cv2.FONT_HERSHEY_SIMPLEX, fontScale = 1,color = (255, 255, 255),thickness = 2)
        else:
            cv2.putText(imga,'CLOSED',(80,100),cv2.FONT_HERSHEY_SIMPLEX, fontScale = 1,color = (255, 255, 255),thickness = 2)
    elif f==1:
         cv2.putText(imga,'WAIT',(50,50),cv2.FONT_HERSHEY_SIMPLEX, fontScale = 1,color = (0, 0, 0),thickness = 2)
    cv2.imshow('Mask',imga)
    imga = cv2.resize(imga,(512,512))
    out.write(imga)
    k=cv2.waitKey(1)
    if k==27:
        break
    distap=distl
    listap=lista
    movp=mov
    closp=clos
out.release()
cv2.destroyAllWindows()
