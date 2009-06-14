#!/bin/sh

# MicroEmulator
MICROEMU=2.0.3

# JSR-257 Nokia Extension API
JSR257EXT=1.0

cd lib/

# MicroEmulator
wget http://surfnet.dl.sf.net/sourceforge/microemulator/microemulator-${MICROEMU}.tar.gz
tar -xzf microemulator-${MICROEMU}.tar.gz
ln -s microemulator-${MICROEMU} microemulator

# JSR 257 Nokia Extensions Stub Libraries
wget http://nfcip-java.googlecode.com/files/jsr-257-nokia-${JSR257EXT}.jar
ln -s jsr-257-nokia-${JSR257EXT}.jar jsr-257-nokia.jar
