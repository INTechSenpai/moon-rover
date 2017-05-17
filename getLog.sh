python=python3.5
type $python >/dev/null 2>&1 || { python=python;}
echo Searching log file...
logName=$(ssh pi@moonrover ls -t moon-rover/pc/logs | head -1)
echo Last log file is: $logName
echo Searching for matching video file...
vidName=$(ssh pi@moonrover ls -t moon-rover/pc/videos | head -1)

logName_short=$(basename "$logName")
logName_short="${logName_short%.*}"
vidName_short=$(basename "$vidName")
vidName_short="${vidName_short%.*}"

matching=false
if [ "$vidName_short" == "$logName_short" ]; then
	matching=true
fi

if [ "$matching" = true ]; then
	echo Found
else
	echo NOT FOUND
fi

echo Retrieving files...
if [ ! -d tmp/ ]; then
	mkdir tmp
fi
scp -q pi@moonrover:~/moon-rover/pc/logs/$logName tmp/log.txt
version=$($python debug_tools/readCommitNumber.py tmp/log.txt)
echo $version

if [ "$matching" = true ]; then
	scp -q pi@moonrover:~/moon-rover/pc/videos/$vidName tmp/vid.dat
fi
echo Done
