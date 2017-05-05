echo Searching log file
logName=$(ssh pi@moonrover ls -t moon-rover/pc/logs | head -1)
echo Last log file is: $logName
echo Retrieving log file...
scp -q pi@moonrover:~/moon-rover/pc/logs/$logName tmp/log.txt
version=$(python debug_tools/readCommitNumber.py tmp/log.txt)
echo $version
echo Uploading log file to slack...
python debug_tools/uploadToSlack.py tmp/log.txt
rm tmp/log.txt
echo Done