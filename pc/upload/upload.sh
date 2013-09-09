#!/bin/bash
LOGIN=intech
DOSSIER_DESTINATION=/home/intech/intech-2013/software/pc/

rsync -e ssh --delete-after --exclude-from exclusion.txt -az ../ "$LOGIN"@"$1":"$DOSSIER_DESTINATION"
