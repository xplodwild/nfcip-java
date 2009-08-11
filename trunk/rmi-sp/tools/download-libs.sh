#!/bin/sh

# MicroEmulator
MICROEMU=2.0.3

# ProGuard
PROGUARD=4.4

# JAD Ant Tasks (JAT)
JAT=1.1

# nfcip-java
NFCIP_JAVA=1.3.1

# JSR-257 Nokia Extension API
JSR257EXT=1.0

# BouncyCastle Java ME
BOUNCY_ME=143

# BouncyCastle Java SE
BOUNCY_SE=143

cd lib/

# MicroEmulator
wget -nv http://surfnet.dl.sf.net/sourceforge/microemulator/microemulator-${MICROEMU}.tar.gz
tar -xzf microemulator-${MICROEMU}.tar.gz
ln -s microemulator-${MICROEMU} microemulator

# ProGuard
wget -nv http://surfnet.dl.sf.net/sourceforge/proguard/proguard${PROGUARD}.tar.gz
tar -xzf proguard${PROGUARD}.tar.gz
ln -s proguard${PROGUARD} proguard

# JAT
wget -nv http://nfcip-java.googlecode.com/files/jad-ant-tasks-${JAT}.jar
ln -s jad-ant-tasks-${JAT}.jar jad-ant-tasks.jar

# nfcip-java
wget -nv http://nfcip-java.googlecode.com/files/nfcip-java-${NFCIP_JAVA}.zip
unzip -q nfcip-java-${NFCIP_JAVA}.zip
ln -s nfcip-java-${NFCIP_JAVA}/nfcip-java-se-${NFCIP_JAVA}.jar nfcip-java-se.jar
ln -s nfcip-java-${NFCIP_JAVA}/nfcip-java-me-${NFCIP_JAVA}.jar nfcip-java-me.jar

# JSR 257 Nokia Extensions Stub Libraries
wget -nv http://nfcip-java.googlecode.com/files/jsr-257-nokia-${JSR257EXT}.jar
ln -s jsr-257-nokia-${JSR257EXT}.jar jsr-257-nokia.jar

# BouncyCastle Java ME
wget -nv http://www.bouncycastle.org/download/lcrypto-j2me-${BOUNCY_ME}.zip
unzip -q lcrypto-j2me-${BOUNCY_ME}.zip
ln -s lcrypto-j2me-${BOUNCY_ME} lcrypto-j2me

# BouncyCastle Java SE
wget -nv http://www.bouncycastle.org/download/bcprov-jdk16-${BOUNCY_SE}.jar
ln -s bcprov-jdk16-${BOUNCY_SE}.jar bcprov-jdk16.jar

