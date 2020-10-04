import requests
import cv2
import numpy as np
import io
import base64
from PIL import Image
import json

url = "http://127.0.0.1:5000/api/test"


#img = cv2.imread("1.png")
# encode image as jpeg
# img_encode = cv2.imencode('.png', img)[1]

# data_encode = np.array(img_encode)
# str_encode = data_encode.tostring()

# image = {'image':str_encode}
# r = requests.post(url, image)
# print(r)
# r = r.json()
# print(r)
content_type = 'image/jpeg'
headers = {'content-type': content_type}

img = cv2.imread('lena.jpg')
print(img.shape)
# encode image as jpeg
_, img_encoded = cv2.imencode('.jpg', img)
# send http request with image and receive response
r = requests.post(url, data=img_encoded.tostring(), headers=headers)
#print(r)
#print(type(r))
#a = r.json()
a = json.loads(r.text)['message']
b = a['py/b64']
c = base64.b64decode(b)
nparr = np.fromstring(c, np.uint8)
print(nparr.shape)
img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
print(type(img))
print(img.shape)
cv2.imwrite('lena1.jpg',img)




# with open("1.png", "rb") as image:
#     img = base64.b64encode(image.read())
#     query={'image':img}
#     r = requests.post(url,query)
#     print(r)
#     r= r.json()
#     print(r)
    # img = io.BytesIO(base64.b64decode(img))
    # img = Image.open(img)
    # img = np.array(img)
    # print(type(img))
    # print(img.shape)
#r = r.json()
#print(r)