import json
import os
import time

from django.conf import settings
from django.http import JsonResponse
from django.shortcuts import render
from . import forms
from .models import Document, SelectedFile

# Create your views here.
from django.views.decorators.csrf import csrf_exempt
import keras
from keras.models import Sequential
from keras.layers import Conv2D, ZeroPadding2D, Activation, Input, concatenate
from keras.models import Model
from keras.layers.normalization import BatchNormalization
from keras.layers.pooling import MaxPooling2D, AveragePooling2D
from keras.layers.merge import Concatenate
from keras.layers.core import Lambda, Flatten, Dense
from keras.initializers import glorot_uniform
from keras.engine.topology import Layer
from keras import backend as K
K.set_image_data_format('channels_first')
import cv2
import os
import numpy as np
from numpy import genfromtxt
import pandas as pd
import tensorflow as tf
from .fr_utils import *
from .inception_blocks_v2 import *

np.set_printoptions(threshold=np.nan)



def triplet_loss(y_true, y_pred, alpha=0.2):
    """
    Implementation of the triplet loss as defined by formula (3)

    Arguments:
    y_true -- true labels, required when you define a loss in Keras, you don't need it in this function.
    y_pred -- python list containing three objects:
            anchor -- the encodings for the anchor images, of shape (None, 128)
            positive -- the encodings for the positive images, of shape (None, 128)
            negative -- the encodings for the negative images, of shape (None, 128)

    Returns:
    loss -- real number, value of the loss
    """

    anchor, positive, negative = y_pred[0], y_pred[1], y_pred[2]

    ### START CODE HERE ### (≈ 4 lines)
    # Step 1: Compute the (encoding) distance between the anchor and the positive, you will need to sum over axis=-1
    pos_dist = tf.reduce_sum(tf.square(tf.subtract(anchor, positive)), axis=-1)
    # Step 2: Compute the (encoding) distance between the anchor and the negative, you will need to sum over axis=-1
    neg_dist = tf.reduce_sum(tf.square(tf.subtract(anchor, negative)), axis=-1)
    # Step 3: subtract the two previous distances and add alpha.
    basic_loss = tf.add(tf.subtract(pos_dist, neg_dist), alpha)
    # Step 4: Take the maximum of basic_loss and 0.0. Sum over the training examples.
    loss = tf.reduce_sum(tf.maximum(basic_loss, 0))
    ### END CODE HERE ###

    return loss


def load_model():
    print('Loading model')
    FRmodel = faceRecoModel(input_shape=(3, 96, 96))
    FRmodel.compile(optimizer='adam', loss=triplet_loss, metrics=['accuracy'])
    load_weights_from_FaceNet(FRmodel)
    FRmodel._make_predict_function()
    print('Model loaded at source')
    return FRmodel

FRmodel = load_model()
threshold = 0.008


def create_directory():
    """
    Create a directory to hold the input data based on the analysis type
    parameter type: Type of analysis chosen by the user

    """
    static_directory = os.path.join(settings.MEDIA_ROOT, 'skin_cancer' + settings.PATH_TYPE)
    print(static_directory)
    if not os.path.exists(static_directory):
        try:
            os.makedirs(static_directory)
        except OSError:
            if os.path.exists(os.path.join(settings.MEDIA_ROOT, 'skin_cancer' + settings.PATH_TYPE)):
                pass
            else:
                raise



@csrf_exempt
def index(request):
    form = forms.DocumentForm()
    return_object = {
        'form': form,
        'document': None
    }

    if request.method == 'POST':
        form = forms.DocumentForm(request.POST, request.FILES)
        if form.is_valid():
            newdoc = Document(docfile=request.FILES['docfile'])  # Create a new Document to store the file on database
            print(newdoc)
            print(newdoc.docfile)
            print(newdoc.docfile.name)
            filename_to_process = SelectedFile(
                filename=os.path.join('skin_cancer' + settings.PATH_TYPE + newdoc.docfile.name))
            newdoc.save()
            filename_to_process.save()
            relative_filepath = filename_to_process.filename  # store the filename for the session


            image_filepath_to_display = os.path.join(settings.MEDIA_URL, relative_filepath)
            image_filepath_to_analyze = os.path.join(settings.MEDIA_ROOT, relative_filepath)

            model = FRmodel
            encoding = img_to_encoding(image_filepath_to_analyze, model)
            average_enc = np.average(encoding)
            print(average_enc)

            cancer = True if average_enc > threshold else False
            return_object['document'] = image_filepath_to_display
            return_object['is_cancer'] = cancer
            return_object['encoding'] = average_enc
    else:
        # model = FRmodel
        print("Model loaded in view")
        create_directory()
    return render(request, 'skin/home.html', context=return_object)
