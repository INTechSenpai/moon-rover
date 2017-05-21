# Permet de communiquer avec l'interface débug du bas niveau depuis la raspberry pi
function mytest {
    "$@"
    local status=$?
    if [ $status -ne 0 ]; then
        echo "error with $1" >&2
    fi
    return $status
}

mytest sudo python -m serial.tools.miniterm -e --eol LF /dev/ttyACM0
