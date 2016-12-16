#!/bin/sh
sudo apt-get update -y

sudo apt-get install git \
oracle-java7-jdk \
librxtx-java \
ant \
hostapd \
dnsmasq

# récupération du code
git clone https://github.com/INTechSenpai/moon-rover.git

# compilation
cd moon-rover/pc
ant

# installation de la lib série
sudo cp lib/librxtxSerial.so $JAVA_HOME/jre/lib/i386/
sudo cp lib/RXTXcomm.jar $JAVA_HOME/jre/lib/ext/

# configuration hotspot wifi
# TODO
