import cwiid, time

button_delay = 0.1

print('Please press buttons 1 + 2 on your Wiimote now ...')
time.sleep(1)

# This code attempts to connect to your Wiimote and if it fails the program quits
try:
    wii = cwiid.Wiimote()
except RuntimeError:
    wii = None
    print("Cannot connect to your Wiimote. Run again and make sure you are holding buttons 1 + 2!")
    quit()

print('Wiimote connection established!')
print('Go ahead and press some buttons')
print('Press PLUS and MINUS together to disconnect and quit.')

time.sleep(3)

wii.rpt_mode = cwiid.RPT_BTN

while True:
    buttons = wii.state['buttons']

    # Detects whether + and - are held down and if they are it quits the program
    if buttons - cwiid.BTN_PLUS - cwiid.BTN_MINUS == 0:
        print('Closing connection ...')
        # NOTE: This is how you RUMBLE the Wiimote
        wii.rumble = 1
        time.sleep(1)
        wii.rumble = 0
        exit(wii)

    # The following code detects whether any of the Wiimotes buttons have been pressed and then prints a statement to the screen!
    if buttons & cwiid.BTN_DOWN:
        print('Left pressed')
        time.sleep(button_delay)
    elif buttons & cwiid.BTN_UP:
        print('Right pressed')
        time.sleep(button_delay)
    elif buttons & cwiid.BTN_RIGHT:
        print('Up pressed')
        time.sleep(button_delay)
    elif buttons & cwiid.BTN_LEFT:
        print('Down pressed')
        time.sleep(button_delay)
    elif buttons & cwiid.BTN_1:
        print('Button 1 pressed')
        time.sleep(button_delay)
    elif buttons & cwiid.BTN_B:
        print('Button B pressed')
        time.sleep(button_delay)
    elif buttons - cwiid.BTN_A - cwiid.BTN_2 == 0:
        print('Button A + B pressed')
        time.sleep(button_delay)
    elif buttons & cwiid.BTN_A:
        print('Button A pressed')
        time.sleep(button_delay)
    elif buttons & cwiid.BTN_2:
        print('Button 2 pressed')
        time.sleep(button_delay)
    elif buttons & cwiid.BTN_MINUS:
        print('Minus Button pressed')
        time.sleep(button_delay)
    elif buttons & cwiid.BTN_PLUS:
        print('Plus Button pressed')
        time.sleep(button_delay)
    else:
        time.sleep(button_delay)

    if buttons & cwiid.BTN_B:
        wii.rpt_mode = cwiid.RPT_BTN | cwiid.RPT_ACC
        print(wii.state['acc'])
        time.sleep(button_delay)
