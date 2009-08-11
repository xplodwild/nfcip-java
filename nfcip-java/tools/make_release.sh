#!/bin/sh
VERSION=1.3.1
PROJECT=nfcip-java
RELEASE_DIR=release-$VERSION

mkdir -p $RELEASE_DIR/$PROJECT-$VERSION

svn co http://nfcip-java.googlecode.com/svn/trunk/$PROJECT $PROJECT-$VERSION
cd $PROJECT-$VERSION
sh tools/download-libs.sh
ant all
cp dist/* ../$RELEASE_DIR/$PROJECT-$VERSION
cp -r api ../$RELEASE_DIR/$PROJECT-$VERSION
cp README AUTHORS COPYING CHANGELOG ../$RELEASE_DIR/$PROJECT-$VERSION
ant clean
cp lib/lib.properties .
rm -rf lib/*
mv lib.properties lib/
rm -rf `find . -type d -name .svn`
cd ../$RELEASE_DIR
zip -qr $PROJECT-$VERSION.zip $PROJECT-$VERSION
cd ../
zip -qr $RELEASE_DIR/$PROJECT-$VERSION-sources.zip $PROJECT-$VERSION
rm -rf $RELEASE_DIR/$PROJECT-$VERSION
rm -rf $PROJECT-$VERSION

