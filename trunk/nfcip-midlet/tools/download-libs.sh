#!/bin/sh

# BlueCove, BlueCove-GPL, BlueCove-EMU
#BLUECOVE=2.1.0

# MicroEmulator
MICROEMU=2.0.3

# ProGuard
PROGUARD=4.3

# Bouncy Castle for Java SE
#BOUNCY_SE=142

# Bouncy Castle for Java ME
# (142 does not work as it uses Object cloning in one of the classes
# which is not supported by Java ME and ProGuard complains about it)
#BOUNCY_ME=141

cd lib/

# BlueCove
#wget http://surfnet.dl.sf.net/sourceforge/bluecove/bluecove-${BLUECOVE}.jar
#wget http://surfnet.dl.sf.net/sourceforge/bluecove/bluecove-gpl-${BLUECOVE}.jar
#wget http://surfnet.dl.sf.net/sourceforge/bluecove/bluecove-emu-${BLUECOVE}.jar
#ln -s bluecove-${BLUECOVE}.jar bluecove.jar
#ln -s bluecove-gpl-${BLUECOVE}.jar bluecove-gpl.jar
#ln -s bluecove-emu-${BLUECOVE}.jar bluecove-emu.jar

# MicroEmulator
wget http://surfnet.dl.sf.net/sourceforge/microemulator/microemulator-${MICROEMU}.tar.gz
tar -xzf microemulator-${MICROEMU}.tar.gz
ln -s microemulator-${MICROEMU} microemulator

# ProGuard
wget http://surfnet.dl.sf.net/sourceforge/proguard/proguard${PROGUARD}.tar.gz
tar -xzf proguard${PROGUARD}.tar.gz
ln -s proguard${PROGUARD} proguard

# Bouncy Castle
#wget http://www.bouncycastle.org/download/bcprov-jdk16-${BOUNCY_SE}.jar
#wget http://www.bouncycastle.org/download/lcrypto-j2me-${BOUNCY_ME}.tar.gz

#ln -s bcprov-jdk16-${BOUNCY_SE}.jar bcprov.jar
#tar -zxf lcrypto-j2me-${BOUNCY_ME}.tar.gz
#ln -s lcrypto-j2me-${BOUNCY_ME} lcrypto-j2me

# JAD Ant Task
wget http://nfcip-java.googlecode.com/files/jad-ant-task-1.0.jar

# JSR 257 Nokia Extensions Stub Libraries
wget http://nfcip-java.googlecode.com/files/jsr-257-nokia-1.0.jar
