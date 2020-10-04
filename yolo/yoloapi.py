from flask import Flask,request, Response
from flask_restful import reqparse, abort, Api, Resource
import pickle
import numpy as np
import cv2
import base64
import io
from PIL import Image
import jsonpickle

app = Flask(__name__)

net = cv2.dnn.readNet("yolov3.weights", "yolov3.cfg")
classes = []
with open("coco.names", "r") as f:
    classes = [line.strip() for line in f.readlines()]
layer_names = net.getLayerNames()
output_layers = [layer_names[i[0] - 1] for i in net.getUnconnectedOutLayers()]
colors = np.random.uniform(0, 255, size=(len(classes), 3))


#parser = reqparse.RequestParser()
#parser.add_argument('image')

@app.route('/api/test', methods=['POST'])
def test():
        #args = parser.parse_args()
        #image = args['image']
        #print(type(image))
        #img = io.BytesIO(base64.b64decode(image))
        #img = Image.open(img)
        #img = np.array(img)
    r = request
    nparr = np.fromstring(r.data, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    print('heyy!!')
    img = cv2.resize(img, None, fx=0.4, fy=0.4)
    height, width, channels = img.shape

    blob = cv2.dnn.blobFromImage(img, 0.00392, (416, 416), (0, 0, 0), True, crop=False)
    net.setInput(blob)
    outs = net.forward(output_layers)

    class_ids=[]
    confidences = []
    boxes = []
    for out in outs:
        for detection in out:
            scores = detection[5:]
            class_id = np.argmax(scores)
            confidence = scores[class_id]
            if confidence > 0.5:
                center_x = int(detection[0] * width)
                center_y = int(detection[1] * height)
                w = int(detection[2] * width)
                h = int(detection[3] * height)
                x = int(center_x - w / 2)
                y = int(center_y - h / 2)
                boxes.append([x, y, w, h])
                confidences.append(float(confidence))
                class_ids.append(class_id)
    indexes = cv2.dnn.NMSBoxes(boxes, confidences, 0.5, 0.4)

    font = cv2.FONT_HERSHEY_PLAIN
    for i in range(len(boxes)):
        if i in indexes:
            x, y, w, h = boxes[i]
            label = str(classes[class_ids[i]])
            color = colors[i]
            cv2.rectangle(img, (x, y), (x + w, y + h), color, 1)
            cv2.putText(img, label, (x, y + 30), font, 1, color, 1)
    print('heyy!!')
    print(type(img))
    _, img_encoded = cv2.imencode('.jpg', img)
    # a = img_encoded.tostring()
    # print(type(a))
    # output = {'result': a}
    response = {'message': img_encoded.tostring()}
    response_pickled = jsonpickle.encode(response)
    return Response(response=response_pickled, status=200, mimetype="application/json")

#api.add_resource(PredictSentiment, '/')


if __name__ == '__main__':
    app.run(debug=True)