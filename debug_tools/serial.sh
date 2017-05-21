# Permet de communiquer avec l'interface débug du bas niveau depuis la raspberry pi
command=sudo python -m serial.tools.miniterm -e --eol LF /dev/ttyACM0

while [ true ]; do
	$command
	sleep 2
done
