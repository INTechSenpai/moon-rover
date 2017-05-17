./getLog.sh
echo Uploading files to slack...
python debug_tools/uploadToSlack.py tmp/log.txt
#if [ "$matching" = true ]; then
	python3.5 debug_tools/uploadToSlack.py tmp/vid.dat
#fi
echo Uploaded
