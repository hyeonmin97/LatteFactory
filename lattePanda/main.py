import time
from BluetoothCom import BluetoothCom
from VideoSave import VideoSave

if __name__ == "__main__":
    videoSave = VideoSave().start()
    bluetooth = BluetoothCom().start()

    while True:
        bluetooth.frame = videoSave.frame
        bluetooth.makingDirName = videoSave.makingDirName
        bluetooth.makingFileName = videoSave.makingFileName
        time.sleep(0.0001)