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

# installation de la lib série (TODO)
sudo cp lib/librxtxSerial.so $JAVA_HOME/jre/lib/i386/
sudo cp lib/RXTXcomm.jar $JAVA_HOME/jre/lib/ext/

# configuration hotspot wifi : https://frillip.com/using-your-raspberry-pi-3-as-a-wifi-access-point-with-hostapd/
sudo cp pi/dhcpcd.conf /etc/dhcpcd.conf
sudo cp pi/interfaces /etc/network/interfaces

sudo service dhcpcd restart
sudo ifdown wlan0
sudo ifup wlan0

sudo cp pi/hostapd.conf /etc/hostapd/hostapd.conf
sudo /usr/sbin/hostapd /etc/hostapd/hostapd.conf
sudo cp pi/hostapd /etc/default/hostapd
sudo cp pi/dnsmasq.conf /etc/dnsmasq.conf
sudo cp pi/sysctl.conf /etc/sysctl.conf

sudo iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE  
sudo iptables -A FORWARD -i eth0 -o wlan0 -m state --state RELATED,ESTABLISHED -j ACCEPT  
sudo iptables -A FORWARD -i wlan0 -o eth0 -j ACCEPT
sudo sh -c "iptables-save > /etc/iptables.ipv4.nat"

sudo cp pi/rc.local /etc/rc.local
sudo reboot
