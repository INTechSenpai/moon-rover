# Permet de communiquer avec l'interface débug du bas niveau depuis la raspberry pi
sudo python -m serial.tools.miniterm -e --eol LF /dev/ttyACM0
