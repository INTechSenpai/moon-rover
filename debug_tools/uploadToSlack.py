import requests
import json
import sys
from private_token import sg_token

if __name__ == "__main__":
    if len(sys.argv) == 2:
        path = str(sys.argv[1])
        url = 'https://slack.com/api/files.upload?channels=moon-rover&pretty=1&token=' + sg_token
        data = {'file': open(path, 'rb')}
        r = requests.post(url, files=data)
        answer = json.loads(r.text)

        if 'file' in answer:
            if 'url_private' in answer['file']:
                url_private = answer['file']['url_private']
                url = 'https://slack.com/api/chat.postMessage?token=' + sg_token + '&channel=moon-rover&as_user=true&pretty=1&text=' + url_private
                requests.post(url)
            else:
                print("error: " + r.text)
        else:
            print("error: " + r.text)
    else:
        print("Argument error")
