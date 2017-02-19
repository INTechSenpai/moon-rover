#!/bin/sh
java -cp bin/:lib/jcommon-1.0.13.jar:lib/jfreechart-1.0.13.jar:lib/RXTXcomm.jar -Djava.library.path=/usr/lib/jni $@
