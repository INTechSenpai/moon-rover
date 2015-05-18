#!/bin/sh
if [ "$#" -ne 2 ]; then
    echo "Usage: ./ttl_upload.sh archive.jar /dev/ttyWHATEVER"
else
    echo "Transfert..."
    echo "stty -echo" > $2
    echo -n "echo \"" >> $2
    base64 --wrap=0 $1 >> $2
    echo "\"| base64 -d > $(date +%F_%T)_$1" >> $2
    echo -n "if [ $(crc32 $1) != \"$" >> $2
    echo "(crc32 $(date +%F_%T)_$1)\" ]; then echo "Erreur de checksum"; fi" >> $2
    echo "stty echo" >> $2
    echo "Done."
fi
