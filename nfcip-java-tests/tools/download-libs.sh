#!/bin/sh

# MicroEmulator
MICROEMU=2.0.3

# JSR-257 Nokia Extension API
JSR257EXT=1.0

# ProGuard
PROGUARD=4.3

# JAD Ant Tasks (JAT)
JAT=1.1

# NFCIP-JAVA
NFCIP_JAVA=1.2.0

cd lib/

# MicroEmulator
wget http://surfnet.dl.sf.net/sourceforge/microemulator/microemulator-${MICROEMU}.tar.gz
tar -xzf microemulator-${MICROEMU}.tar.gz
ln -s microemulator-${MICROEMU} microemulator

# JSR 257 Nokia Extensions Stub Libraries
wget http://nfcip-java.googlecode.com/files/jsr-257-nokia-${JSR257EXT}.jar
ln -s jsr-257-nokia-${JSR257EXT}.jar jsr-257-nokia.jar

# ProGuard
wget http://surfnet.dl.sf.net/sourceforge/proguard/proguard${PROGUARD}.tar.gz
tar -xzf proguard${PROGUARD}.tar.gz
ln -s proguard${PROGUARD} proguard

# JAT
wget http://nfcip-java.googlecode.com/files/jad-ant-tasks-1.1.jar
ln -s jad-ant-tasks-${JAT}.jar jad-ant-tasks.jar

# NFCIP-JAVA-SE
wget http://nfcip-java.googlecode.com/files/nfcip-java-${NFCIP_JAVA}.zip
unzip -q nfcip-java-${NFCIP_JAVA}.zip
ln -s nfcip-java-${NFCIP_JAVA}/nfcip-java-se-${NFCIP_JAVA}.jar nfcip-java-se.jar
ln -s nfcip-java-${NFCIP_JAVA}/nfcip-java-me-${NFCIP_JAVA}.jar nfcip-java-me.jar

