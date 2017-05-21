python=python3.5
type $python >/dev/null 2>&1 || { python=python;}

echo Retrieving files...
if [ ! -d tmp/ ]; then
	mkdir tmp
fi

scp pi@moonrover:~/moon-rover/pc/logs/last.txt tmp/log.txt
scp pi@moonrover:~/moon-rover/pc/videos/last.dat tmp/vid.dat

version=$($python debug_tools/readCommitNumber.py tmp/log.txt)
echo $version

echo Done
