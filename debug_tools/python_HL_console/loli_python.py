import serial
import time
import stats

ser = serial.Serial('COM3', baudrate=9600)  # open serial port
print(ser.name)         # check which port was really used

tArray = []
tArray2 = []
for i in range(60):
    t1 = time.clock()
    ser.write([0xFD, 0x05, 0x01, 0x5A, 0x5D])
    t3 = time.clock()
    r = ser.read(4)
    t2 = time.clock()
    if r != b'\xf9\x04\x01\xfe':
        print(r)
    tArray.append(t2-t1)
    tArray2.append(t2-t3)
    time.sleep(1)

print(max(tArray), stats.mean(tArray))
print(max(tArray2), stats.mean(tArray2))

ser.close()             # close port
