# -*- coding: utf-8 -*-

import socket

_last_direction = 0
_direction_buffer = []
_direction_buffer_size = 5
_direction_max_delta = 8
_first_direction = True

def init():
    global _socket
    _socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    _socket.connect(("localhost", 13371))
    

def close():
    global _socket
    # envoi d'un shutdown
    message = bytearray()
    message.append(8)
    message.append(0)
    _socket.send(message)
    _socket.close()

def set_speed(speed):
    global _socket
    speed /= 10
    print "Speed set to: " + str(speed)
    if speed < 0:
        speed = speed + 256
    message = bytearray()
    message.append(2)
    message.append(speed)
    _socket.send(message)

def speed_up():
    global _socket
    message = bytearray()
    message.append(0)
    message.append(0)
    _socket.send(message)

def speed_down():
    global _socket
    message = bytearray()
    message.append(1)
    message.append(0)
    _socket.send(message)

def turn_right():
    global _socket
    message = bytearray()
    message.append(3)
    message.append(0)
    _socket.send(message)

def turn_left():
    global _socket
    message = bytearray()
    message.append(4)
    message.append(0)
    _socket.send(message)

def reset_wheels():
    global _socket
    message = bytearray()
    message.append(7)
    message.append(0)
    _socket.send(message)

# direction entre -20 et 20
def set_direction(direction):
    global _socket, _last_direction, _first_direction, _direction_buffer, _direction_buffer_size, _direction_max_delta
    if _first_direction:
        _first_direction = False
    else:
        direction *= 3

        _direction_buffer.append(direction)
        if len(_direction_buffer) > _direction_buffer_size:
            _direction_buffer = _direction_buffer[1:]
        direction = sum(_direction_buffer) / len(_direction_buffer)

        if direction - _last_direction > _direction_max_delta:
            direction = _last_direction + _direction_max_delta
        elif direction - _last_direction < -_direction_max_delta:
            direction = _last_direction - _direction_max_delta

        direction = int(round(direction))

        print "Direction set to: " + str(direction)
        _last_direction = direction
        if direction < 0:
            direction = direction + 256
        message = bytearray()
        message.append(5)
        message.append(direction)
        _socket.send(message)

def ping():
    global _socket
    message = bytearray()
    message.append(9)
    message.append(0)
    _socket.send(message)

def robot_stop():
    global _socket
    print "Stop"
    message = bytearray()
    message.append(6)
    message.append(0)
    _socket.send(message)

def pull_up_net():
    global _socket
    print "pull_up_net"
    message = bytearray()
    message.append(10)
    message.append(0)
    _socket.send(message)

def pull_down_net():
    global _socket
    print "pull_down_net"
    message = bytearray()
    message.append(11)
    message.append(0)
    _socket.send(message)

def open_net():
    global _socket
    print "open_net"
    message = bytearray()
    message.append(13)
    message.append(0)
    _socket.send(message)

def close_net():
    global _socket
    print "close_net"
    message = bytearray()
    message.append(12)
    message.append(0)
    _socket.send(message)

def eject_left_side():
    global _socket
    print "eject_left_side"
    message = bytearray()
    message.append(14)
    message.append(0)
    _socket.send(message)

def rearm_left_side():
    global _socket
    print "rearm_left_side"
    message = bytearray()
    message.append(16)
    message.append(0)
    _socket.send(message)

def eject_right_side():
    global _socket
    print "eject_right_side"
    message = bytearray()
    message.append(15)
    message.append(0)
    _socket.send(message)

def rearm_right_side():
    global _socket
    print "rearm_right_side"
    message = bytearray()
    message.append(17)
    message.append(0)
    _socket.send(message)
