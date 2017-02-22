#!/bin/sh
#!/bin/sh
if [ "$#" -lt 1 ]; then
    echo "Usage: ./run.sh Classe"
else
    #sudo nice -n -2
    java -Xmx1G -Xms1G -cp bin/:lib/jcommon-1.0.13.jar:lib/jfreechart-1.0.13.jar:lib/RXTXcomm.jar -Djava.library.path=/usr/lib/jni $@
fi
