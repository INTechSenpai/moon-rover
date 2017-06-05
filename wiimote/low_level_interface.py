import socket

_last_speed = 0
_last_direction = 0

_socket

def init():
    global _socket
    _socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    _socket.connect(("localhost", 13371))
    

def close():
    # envoi d'un shutdown
    message = bytearray()
    message.append(8)
    message.append(0)
    _socket.send(message)
    _socket.close()

def set_speed(speed):
    global _last_speed
    if speed != _last_speed:
        print "Speed set to: " + str(speed)
        _last_speed = speed
        message = bytearray()
        message.append(2)
        message.append(speed)
        _socket.send(message)

# direction entre -20 et 20
def set_direction(direction):
    global _last_direction
    if direction != _last_direction:
        print "Direction set to: " + str(direction)
        _last_direction = direction
        message = bytearray()
        message.append(5)
        message.append(direction)
        _socket.send(message)

def robot_stop():
    print "Stop"
    message = bytearray()
    message.append(6)
    message.append(0)
    _socket.send(message)

def robot_run():
    print "Running"
    # TODO : à gérer plus haut niveau
    #todo verifier que l'ordre est fini avant d'en envoyer un nouveau

def pull_up_net():
    print "pull_up_net"
    message = bytearray()
    message.append(10)
    message.append(0)
    _socket.send(message)

def pull_down_net():
    print "pull_down_net"
    message = bytearray()
    message.append(11)
    message.append(0)
    _socket.send(message)

def open_net():
    print "open_net"
    message = bytearray()
    message.append(13)
    message.append(0)
    _socket.send(message)

def close_net():
    print "close_net"
    message = bytearray()
    message.append(12)
    message.append(0)
    _socket.send(message)

def eject_left_side():
    print "eject_left_side"
    message = bytearray()
    message.append(14)
    message.append(0)
    _socket.send(message)

def rearm_left_side():
    print "rearm_left_side"
    message = bytearray()
    message.append(16)
    message.append(0)
    _socket.send(message)

def eject_right_side():
    print "eject_right_side"
    message = bytearray()
    message.append(15)
    message.append(0)
    _socket.send(message)

def rearm_right_side():
    print "rearm_right_side"
    message = bytearray()
    message.append(17)
    message.append(0)
    _socket.send(message)
