from threading import Thread
import cv2
import datetime as dt
import time
import pytz
import os
import shutil

class VideoSave:

    def __init__(self, src=0):
        print("VideoSave Thread init")
        self.stream = cv2.VideoCapture(src)
        (self.grabbed, self.frame) = self.stream.read()
        self.vid_cod = cv2.VideoWriter_fourcc(* 'XVID')
        self.makingFileName = None
        self.makingDirName = None

    def start(self):
        Thread(target=self.save, args=()).start()
        return self

    def get(self):
        while not self.stopped:
            if not self.grabbed:
                self.stop()
            else:
                (self.grabbed, self.frame) = self.stream.read()
                time.sleep(0.0001)

    def getVideoCapture(self):
        return self.stream

    def stop(self):
        self.stopped = True

    def getFrame(self):
        (self.grabbed, self.frame) = self.stream.read()
        return self.frame

    def checkVolume(self):
        path = "D:/video"
        try:
            total, used, free = shutil.disk_usage(path)
            used_percent = used/total
            print("used : {:.0%}".format(used_percent))
            if used_percent > 0.95:
                directory = os.listdir(path)
                delete_directory = path + "/" + directory[0]
                shutil.rmtree(delete_directory)
                print("directory {}".format(delete_directory))
        except Exception as e:
            print(e)

    def save(self):
        video_dir = os.listdir("D:/")
        if "video" not in video_dir:
            print("make video")
            os.mkdir("D:/video")

        while True:
            self.checkVolume()


            #비디오 저장을 위한 폴더 생성
            #해당 날짜 디렉터리 없으면 생성, 있으면 넘어가기
            KST = pytz.timezone("Asia/Seoul")
            now = dt.datetime.now(KST)
            ymd = now.strftime("%y%m%d")
            HMS = now.strftime("%H%M%S")

            #print(now.strftime("%y-%m-%d-%H-%M-%S")) 년 월 일 시 분 초
            directory = os.listdir("D:/video")
            if ymd not in directory:
                os.mkdir("D:/video/" + ymd)

            #비디오 저장을 위한 객체 생성
            output = cv2.VideoWriter("D:/video/{}/{}.avi".format(ymd, HMS), self.vid_cod, 30, (640,480))
            print(HMS)
            self.makingFileName = str(HMS)
            self.makingDirName = str(ymd)
            start_time = time.time()
            while True:
                (self.grabbed, self.frame) = self.stream.read()
                output.write(self.frame)

                video_time = time.time()
                if video_time-start_time > 60:#초단위로 저장
                    break

            print("output")
            output.release()

        self.stream.release()

if __name__ == "__main__":
    videoSave = VideoSave()
    videoSave.start()