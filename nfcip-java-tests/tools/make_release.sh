#!/bin/sh
VERSION=1.3.0
PROJECT=nfcip-java-tests
RELEASE_DIR=release-$VERSION

mkdir -p $RELEASE_DIR/$PROJECT-$VERSION
svn co http://nfcip-java.googlecode.com/svn/trunk/$PROJECT $PROJECT-$VERSION
cd $PROJECT-$VERSION
sh tools/download-libs.sh
ant deploy
cp dist/*-se* ../$RELEASE_DIR/$PROJECT-$VERSION
cp deployed/* ../$RELEASE_DIR/$PROJECT-$VERSION
cp README COPYING AUTHORS CHANGELOG ../$RELEASE_DIR/$PROJECT-$VERSION
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

