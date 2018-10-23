from django.test import TestCase
import requests
import base64
from imageio import imread
import io
import cv2
import json
# Create your tests here.

with open('test_image.jpg', 'rb') as f:
	encoding = base64.b64encode(f.read())
	encoded_string = encoding.decode()

# print(encoded_string)
# img = imread(io.BytesIO(base64.b64decode(encoded_string)))
# cv2_img = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)
# cv2.imwrite('test_recon.jpg', cv2_img)
url = 'http://localhost:8000/skin_cancer/detect'
post_data = {'image': encoded_string}
headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
response = requests.post(url, json=post_data)
print(response.status_code)
print(response.json())