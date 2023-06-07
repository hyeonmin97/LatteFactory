import RPi.GPIO as GPIO
import time
from gpiozero import Buzzer

trig = 20
echo = 21
 
print('start')
 
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(trig,GPIO.OUT)
GPIO.setup(echo,GPIO.IN)
bz = Buzzer(6)
distance = 0
def ultra(): 
    try:
        global distance
        while(True):
            GPIO.output(trig, False)
            #time.sleep(0.2)
            
            GPIO.output(trig, True)
            time.sleep(0.00001)
            GPIO.output(trig, False)
            
            while GPIO.input(echo) == 0:
                pulse_start = time.time()
                
            while GPIO.input(echo) == 1:
                pulse_end = time.time()
            
            
            pulse_duration = pulse_end - pulse_start
            distance = pulse_duration * 17000
            distance = round(distance, 2)
            
            print('front : ', distance,'cm')
            play(300)

            
    except:
        GPIO.cleanup()

def play(cm):
    global distance
    if(distance <cm):
        if distance < 50:
            distance = 50
        temp = (cm - distance)
        if temp <= 0 :
            temp = 1
        bz.on()
        time.sleep(0.01)
        bz.off()
        time.sleep((1-temp/cm)/1.5)
def stop():
    global bz
    bz = None
if __name__ == '__main__':
    ultra()