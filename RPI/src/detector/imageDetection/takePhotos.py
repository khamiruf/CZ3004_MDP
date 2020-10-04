# -*- coding: utf-8 -*-
"""
Created on Tue Sep 15 19:45:31 2020

@author: clari
"""


# import the necessary packages
import numpy as np
import argparse
# import imutils
import time
import cv2
import os
# from imutils.video import VideoStream

# construct the argument parse and parse the arguments
ap = argparse.ArgumentParser()
# ap.add_argument("-o", "--output", required=True,
# 	help="path to output video file")
ap.add_argument("-p", "--picamera", type=int, default=-1,
 	help="whether or not the Raspberry Pi camera should be used")
# ap.add_argument("-f", "--fps", type=int, default=20,
# 	help="FPS of output video")
# ap.add_argument("-c", "--codec", type=str, default="MJPG",
# 	help="codec of output video")
args = vars(ap.parse_args())
cap = cv2.VideoCapture(0)

# vs = VideoStream(usePiCamera=args["picamera"] > 0).start()
time.sleep(2.0)

count = 90

# initialize the FourCC, video writer, dimensions of the frame, a
# zeros array
# fourcc = cv2.VideoWriters_fourcc(*args["codec"])
# writer = None
# (h, w) = (None, None)
# zeros = None
# loop over frames from the video stream
dsize = (416, 416)

while True:
	# grab the frame from the video stream and resize it to have a
	# maximum width of 300 pixels

    hasframe, frame = cap.read()
    frame = cv2.resize(frame, dsize)
    key = cv2.waitKey(1) & 0xFF
	# if the `q` key was pressed, break from the loop
    # cv2.imshow("Frame", frame)


    # if key == ord("t"):
    cv2.imwrite("Dataset/images/Left/l."+str(count)+".jpg", frame)
    count+=1
    print("took images/9."+str(count)+".jpg")

    # if key == ord("q"):
        # break
    
    
# 	# check if the writer is None
# 	if writer is None:
# 		# store the image dimensions, initialize the video writer,
# 		# and construct the zeros array
# 		(h, w) = frame.shape[:2]
# 		writer = cv2.VideoWriter(args["output"], fourcc, args["fps"],
# 			(w * 2, h * 2), True)
# 		zeros = np.zeros((h, w), dtype="uint8")