from threading import Thread
import time
import numpy
import bluetooth as bt
import os
import cv2


class BluetoothCom:
    uuid = "fa87c0d0-afac-11de-8a39-0800200c9a66"

    # 블랙박스
    ACK_INIT = "acin"
    acin = ACK_INIT.encode()

    ACK_DATA_RECEIVED = "acdr"
    acdr = ACK_DATA_RECEIVED.encode()

    DATA_END = "dten"

    SEND_DATA = "sedt"
    sedt = SEND_DATA.encode()

    BLBK = "blbk"
    blbk = BLBK.encode()

    BLBK_END = "bled"
    bled = BLBK_END.encode()

    FILE_NAME_END = "fned"
    fned = FILE_NAME_END.encode()

    ACK_FILE_NAME = "acfn"
    acfn = ACK_FILE_NAME.encode()

    FILE_SIZE = "fisz"
    fisz = FILE_SIZE.encode()

    ACK_FILE_SIZE = "acfs"
    acfs = ACK_FILE_SIZE.encode()

    SEND_FILE_SIZE = "sfsz"
    sfsz = SEND_FILE_SIZE.encode()

    FILE_LIST = "fist"
    fist = FILE_LIST.encode()
    ACK_FILE_LIST = "acfl"
    LIST_START = "lsst"
    LIST_END = "lied"
    FLIE_LIST_END = "file"
    
    DATA_END = "dten"
    DATA_START = "dtst"

    CONTINUE_FLAG = "cont"
    continueFlag = CONTINUE_FLAG.encode()
    BREAK_FLAG = "brea"
    break_flag = BREAK_FLAG.encode()

    BLUETOOTH_END = "bted"
    bluetooth_end = BLUETOOTH_END.encode()

    START_STREAMING = "stst"
    stst = START_STREAMING.encode()

    def __init__(self):
        self.frame = None
        self.makeSocketException = False
        self.makingFileName = None
        self.makingDirName = None
        self.client_sock = None
        self.server_sock = None
        self.port = None
        print("BluetoothCom Thread init")

    def makeSocket(self):
        try:
            print("makeSocket")
            time.sleep(0.2)
            # Create a new server socket using RFCOMM protocol
            self.server_sock = bt.BluetoothSocket(bt.RFCOMM)

            # Bind to any port
            self.server_sock.bind(("", 4))

            # Start listening
            self.server_sock.listen(1)

            # Get the port the server socket is listening
            self.port = self.server_sock.getsockname()[1]

            # Start advertising the service
            bt.advertise_service(self.server_sock, "RaspiBtSrv",
                                 service_id=self.uuid,
                                 service_classes=[self.uuid, bt.SERIAL_PORT_CLASS],
                                 profiles=[bt.SERIAL_PORT_PROFILE])
            print("Waiting for connection : channel %d" % self.port)

            # 클라이언트 접속 대기
            self.client_sock, client_info = self.server_sock.accept()
            print("Accepted connection from " + str(client_info))
            self.makeSocketException = False

        except Exception as e:
            self.makeSocketException = True
            self.server_sock.close()
            self.client_sock.close()
            self.server_sock = None
            print(e)

    def getClient(self):
        return self.client_sock

    def start(self):
        Thread(target=self.run, args=()).start()
        return self

    def close(self):
        try:
            self.server_sock.close()
            self.client_sock.close()
            self.server_sock = None
            self.client_sock = None
            print("socket close")
        except Exception as e:
            print(e)

    def recvBluetoothENd(self, recv):
        if recv == self.bluetooth_end:
            print("android activity destroy")
            return True
        else:
            return False

    def clientRecv(self):
        rec = self.client_sock.recv(4)
        result = self.recvBluetoothENd(rec)
        if not result:
            return rec
        else:
            raise Exception("android activity close")

    def run(self):
        try:
            while True:
                self.makeSocket()
                if not self.makeSocketException:  # 정상적으로 만들어졌을때
                    pass
                else:  # 제대로 안만들어졌을때
                    continue  # 다시 만들기

                while True:
                    print("wait first recv...")
                    try:
                        rec = self.clientRecv()
                        print(rec)

                        if rec == ''.encode():  # 잘못 종료시 ''만 받아옴
                            self.close()
                            break
                        print("first recv out")

                        if rec == self.blbk:  # 블랙박스(파일 보내는거)
                            print("in blbk")
                            self.client_sock.send(self.acin)  # 블랙박스를 원한다고 받으면 폰으로 ack보냄

                            # 파일 이름 받아야 함
                            recData = self.client_sock.recv(1024)
                            fileName = recData[:-4].decode()
                            fileName = "d:video" + fileName
                            print(fileName)
                            fileEnd = recData[-4:]

                            # 파일 이름 다 받으면
                            if fileEnd == self.fned:
                                self.client_sock.send(self.acfn)  # 다 받았다고 알림
                            else:
                                print("file name error")
                                self.close()
                                break

                            # 파일 사이즈 보내달라는 sfsz기다림
                            rec = self.clientRecv()
                            print(rec)

                            try:
                                with open(fileName, "rb") as video:
                                    buffer = video.read()
                                    fileSize = len(buffer)
                                    print(fileSize)
                            except FileNotFoundError:
                                print("file not found")

                            # 파일 사이즈와 파일사이즈 끝났다고 보냄
                            self.client_sock.send(str(fileSize) + self.FILE_SIZE)

                            # 파일 사이즈 받았다는거 기다림
                            rec = self.clientRecv()

                            # 파일 데이터 보냄
                            self.client_sock.send(buffer)
                            self.client_sock.send(self.BLBK_END)

                            # 파일 받았다는거 기다림
                            rec = self.clientRecv()
                            if rec == self.acdr:
                                print("recv data {}".format(rec))
                            elif rec == '':
                                pass
                            print('send end')



                        elif rec == self.fist:  # 파일 리스트인 경우
                            self.client_sock.send(self.ACK_FILE_LIST.encode())
                            rec = self.clientRecv()
                            # 파일 리스트 전송 시작하라는 값 들어옴
                            if rec == self.LIST_START.encode():
                                print("{} {}".format(self.makingDirName, self.makingFileName))
                                fileList = []
                                for (root, dirs, files) in os.walk("D:/video"):
                                    fileDir = root[9:]

                                    if len(files) > 0:
                                        for file_name in files:
                                            if (self.makingFileName + ".avi" == file_name):
                                                if (self.makingDirName == fileDir):  # 현재 만들고있는 파일이 아닐때 값 추가
                                                    pass
                                                else:
                                                    fileDirectory = "{}!{}".format(fileDir, file_name)
                                                    fileList.append(fileDirectory)
                                            else:
                                                fileDirectory = "{}!{}".format(fileDir, file_name)
                                                fileList.append(fileDirectory)

                                # 왜 1:-2했는지 확인해볼것 => [] 제외 -> 0. -1 제외잖아...
                                self.client_sock.send(str(fileList)[1:-2] + self.LIST_END)  # 파일 리스트 보냄 + 리스트 끝 알림

                            # 파일 리스트 받았다는거 기다림
                            rec = self.clientRecv()
                            if rec == self.FLIE_LIST_END.encode():
                                print("send file list end")

                        elif rec == self.stst:  # 스트리밍일때
                            while True:
                                self.client_sock.send(self.DATA_START)
                                rec = self.clientRecv()
                                if (rec == self.acin):
                                    print('acin')

                                encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), 10]  # 기본값 95
                                result, imgencode = cv2.imencode('.jpg', self.frame, encode_param)

                                data = numpy.array(imgencode)

                                self.client_sock.send(data)
                                self.client_sock.send(self.DATA_END)

                                rec = self.clientRecv()
                                if (rec == self.acdr):
                                    print('acdr')

                        time.sleep(0.0001)

                    except Exception as e:
                        print(e)
                        self.close()
                        print("socket close")
                        break

                    time.sleep(0.0001)

        except Exception as e:
            print(e)
            self.run()  # 다시시작
