from picamera import PiCamera
import time
import sys
import os.path

class SymbolDetector():
    def __init__(self):
        self.camera = PiCamera()
        self.camera.resolution = (450, 400)
        self.camera.hflip = False
        self.save_path = "src/detector/pi_photos/"
        print(os.getcwd())
        print('initializing camera script')

    def main(self, count):
        try:
            complete = os.path.join(self.save_path, str(count)+".jpg")
            #camera.shutter_speed = 1000000/60
            self.camera.awb_mode = 'tungsten'
            self.camera.capture(complete)
            print("done")
        except Exception as e:
            print("fail")
            print("error: ", e)