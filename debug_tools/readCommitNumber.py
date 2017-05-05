import sys

if __name__ == "__main__":
    if len(sys.argv) == 2:
        path = str(sys.argv[1])
        file = open(path)
        for line in file:
            sLine = line.split(">")
            if 'Version' in sLine[1]:
                print(sLine[1], end='')
                sys.exit(0)
    else:
        print("Argument error")