from flask import Flask, request
from flask_restful import reqparse, abort, Api, Resource
import numpy as np
import json
import cv2

app = Flask(__name__)
api = Api(app)

parser = reqparse.RequestParser()
parser.add_argument('image')

class YOLO(Resource):
    def post(self):
        args = parser.parse_args()
        image = args['image']
        print(len(image))
        img = json.loads(image)
        print(type(img))
        img = np.array(img,dtype='uint8')
        #nparr = np.fromstring(image, dtype='uint8')
        #print(nparr.shape)
        #img = nparr.reshape((512,512,3))
        img = cv2.resize(img, None, fx=0.4, fy=0.4)
        height, width, channels = img.shape
        output = {'result':'sucess'}
        return output

api.add_resource(YOLO, '/')


if __name__ == '__main__':
    app.run(debug=True)
