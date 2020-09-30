from picamera import PiCamera
import time
import sys
import os.path

save_path = "src/detector/pi_photos/"

def main():
    count = 0; 
    try:
        print(os.getcwd())
        filename = count           
        camera = PiCamera()
        camera.resolution = (450, 400)
        camera.hflip = True
        complete = os.path.join(save_path, str(filename)+".jpg")
        #camera.shutter_speed = 1000000/60
        camera.awb_mode = 'tungsten'
        camera.capture(complete)
        print("done")
    except Exception as e:
        print("fail")
        print(e)


if __name__ == "__main__":
    main()
