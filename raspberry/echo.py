# -*- coding : utf-8 -*-
import time
import serial
from threading import Thread
from gpiozero import Robot
import ultrasonic
import atexit
ser1 = serial.Serial("/dev/ttyAMA1", 115200)
ser2 = serial.Serial("/dev/ttyAMA2", 115200)
motor = Robot(left=(17, 27), right=(19, 26))
distance1 = 0
distance2 = 0
flag =True
# we define a new function that will get the data from LiDAR and publish it

def read_data1():
    global distance1
    global flag
    while flag:
        counter = ser1.in_waiting # count the number of bytes of the serial port
        if counter > 8:
            bytes_serial = ser1.read(9)
            ser1.reset_input_buffer()
            if bytes_serial[0] == 0x59 and bytes_serial[1] == 0x59: # this portion is for python3 
                distance1 = bytes_serial[2] + bytes_serial[3]*256
                ser1.reset_input_buffer()


	    # this portion for python2
            if bytes_serial[0] == "Y" and bytes_serial[1] == "Y":
                distL = int(bytes_serial[2].encode("hex"), 16)
                distH = int(bytes_serial[3].encode("hex"), 16)
                distance1 = distL + distH*256
                print("Distance1:"+ str(distance1) + "\n")
                ser1.reset_input_buffer()

def read_data2():
    global distance2
    global flag
    while flag:
        counter = ser2.in_waiting # count the number of bytes of the serial port
        if counter > 8:
            bytes_serial = ser2.read(9)
            ser2.reset_input_buffer()
            if bytes_serial[0] == 0x59 and bytes_serial[1] == 0x59: # this portion is for python3 
                distance2 = bytes_serial[2] + bytes_serial[3]*256 
                ser2.reset_input_buffer()


	    # this portion for python2
            if bytes_serial[0] == "Y" and bytes_serial[1] == "Y":
                distL = int(bytes_serial[2].encode("hex"), 16)
                distH = int(bytes_serial[3].encode("hex"), 16)
                distance2 = distL + distH*256
                print("Distance2:"+ str(distance2) + "\n")
                ser2.reset_input_buffer()


def vib1(cm=500):
    global distance1
    global flag
    while flag:
        
        if distance1 < cm:
            temp = cm - distance1*1.2
            if temp == 0:
                temp = 1 
            speed = temp/cm #가까이있는 물체일수록 진동세기 커짐
            if speed <0 : #음수일경우
                speed = 0 #진동 안울림(짜피 멀리있으니깐)
            print("back left : distance {} speed{} ".format(str(distance1),speed))
            motor.left_motor.forward(speed)
            time.sleep(speed) #가까이 있을수록 진동 오래울림
            motor.left_motor.stop()
            time.sleep(0.5)



def vib2(cm=500):
    global distance2
    global flag
    while flag:
        if distance2 < cm:
            temp = cm - distance2*1.2
            if temp == 0:
                temp = 1 
            speed = temp/cm #가까이있는 물체일수록 진동세기 커짐
            if speed <0 : #음수일경우
                speed = 0 #진동 안울림(짜피 멀리있으니깐)
            print("back right : distance {} speed{} ".format(str(distance2),speed))
            motor.right_motor.forward(speed)
            time.sleep(speed) #가까이 있을수록 진동 오래울림
            motor.right_motor.stop()
            time.sleep(0.5)
def programExit():
    global flag
    flag =False
    motor.stop()
    ultrasonic.stop()
    print('program exit')

if __name__ == "__main__":
    cm=700
    atexit.register(programExit)
    try:
        if ser1.isOpen() == False:
            ser1.open()
        if ser2.isOpen() == False:
            ser2.open()    
        thread1 = Thread(target=read_data1, args=())
        thread2 = Thread(target=read_data2, args=())
        thread3 = Thread(target=vib1, args=(cm,))
        thread4 = Thread(target=vib2, args=(cm,))
        thread5 = Thread(target=ultrasonic.ultra, args=())
        thread1.start()
        thread2.start()
        thread3.start()
        thread4.start()
        thread5.start()
        thread1.join()
        thread2.join()
        thread3.join()
        thread4.join()
        thread5.join()
    except KeyboardInterrupt: # ctrl + c in terminal.
        programExit()
        if ser1 != None:
            ser1.close()
            print("program interrupted by the user")
        if ser2 != None:
            ser2.close()
            print("program interrupted by the user")