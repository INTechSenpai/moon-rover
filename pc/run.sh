#!/bin/sh
export DISPLAY=:1
if [ "$#" -lt 1 ]; then
    echo "Usage: ./run.sh Classe"
else
    #sudo nice -n -2
    java -Xmx1G -Xms1G -cp bin/:lib/RXTXcomm.jar -Djava.library.path=/usr/lib/jni $@
fi
