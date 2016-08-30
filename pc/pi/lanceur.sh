#!/bin/sh
if [ "$#" -ne 1 ]; then
    echo "Usage: ./lanceur.sh archive.jar"
else
    sudo nice -n -2 java -jar -Xmx1G -Xms1G $1
fi
