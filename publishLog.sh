./getLog.sh
python=python3.5
type $python >/dev/null 2>&1 || { python=python;}
echo Uploading files to slack...
python debug_tools/uploadToSlack.py tmp/log.txt
if [ -f /tmp/vid.dat ]; then
	$python debug_tools/uploadToSlack.py tmp/vid.dat
fi
echo Uploaded
