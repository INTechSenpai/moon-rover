_last_speed = 0
_last_direction = 0

def set_speed(speed):
    global _last_speed
    if speed != _last_speed:
        print "Speed set to: " + str(speed)
        _last_speed = speed
        # @PF: au travail !

# direction entre -20 et 20
def set_direction(direction):
    global _last_direction
    if direction != _last_direction:
        print "Direction set to: " + str(direction)
        _last_direction = direction
        # @PF: au travail !

def robot_stop():
    print "Stop"
    # @PF: au travail !

def robot_run():
    print "Running"
    #todo verifier que l'ordre est fini avant d'en envoyer un nouveau
    # @PF: au travail !

def pull_up_net():
    print "pull_up_net"
    # @PF: au travail !

def pull_down_net():
    print "pull_down_net"
    # @PF: au travail !

def open_net():
    print "open_net"
    # @PF: au travail !

def close_net():
    print "close_net"
    # @PF: au travail !

def eject_left_side():
    print "eject_left_side"
    # @PF: au travail !

def rearm_left_side():
    print "rearm_left_side"
    # @PF: au travail !

def eject_right_side():
    print "eject_right_side"
    # @PF: au travail !

def rearm_right_side():
    print "rearm_right_side"
    # @PF: au travail !
