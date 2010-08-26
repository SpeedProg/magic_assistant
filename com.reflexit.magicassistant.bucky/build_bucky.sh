#!/bin/sh 
#
# to run:
# UPLOAD=1 - to upload release
# TIMESTAMP=N - to force release 4th number (build id) to be N 
# e.g 
# UPLOAD=1 TIMESTAMP=4 ./build.sh
#

PATH="/usr/local/bin:/mingw/bin:/c/mingw/bin:/bin:/c/Windows/system32:/c/Windows:/c/Program Files (x86)/Vim/vim72:/c/Program Files/Subversion/bin:/C/Program Files (x86)/PuTTy"
Path=$PATH
export PATH

MAGIC_DIR=/C/Develop/magic
BUILD_DIR=$MAGIC_DIR/build
INSTALL_DIR=$MAGIC_DIR/install
EXPORT_DIR=$MAGIC_DIR/export
WORKSPACE=$MAGIC_DIR/workspace
LOG=$BUILD_DIR/log
SF_USER=elaskavaia
SF_PRIVATE_KEY=$MAGIC_DIR/.ssh/id_rsa
SCP=scp.exe

TIMESTAMP=${TIMESTAMP:-`date +%Y%m%d%H%M`}
UPLOAD=${UPLOAD:-0}
INSTALL=${INSTALL:-0}
BUILD=${BUILD:-1}
VERSION=`grep Bundle-Version $WORKSPACE/com.reflexit.magiccards-rcp/META-INF/MANIFEST.MF | sed -e 's?Bundle-Version: ??' -e 's?\.qualifier??'` 
RELEASE=$VERSION.$TIMESTAMP
echo Building $RELEASE

if [ "$BUILD" -eq 1 ]; then
(
echo copy sources
SOURCE_DIR=$BUILD_DIR/sources
rm -rf $SOURCE_DIR
mkdir $SOURCE_DIR
#cp -r $WORKSPACE/com.reflexit.magic* $SOURCE_DIR
#cp -r $WORKSPACE/com.reflexit.mtg* $SOURCE_DIR
echo bucky build
RESULT="$BUILD_DIR/result/com.reflexit.magicassistant.bucky_1.0.0-eclipse.feature"
(cd  $RESULT; rm -rf magic* site.p2 site tmp;)
rm -rf $BUILD_DIR/tmp $BUILD_DIR/result
#$MAGIC_DIR/Bucky/buckminster/buckminster -data $BUILD_DIR/bucky_workspace/ -S $SOURCE_DIR/com.reflexit.magicassistant.bucky/build.script -vmargs -Dsource.dir=$SOURCE_DIR -Dbuckyprops=$SOURCE_DIR/com.reflexit.magicassistant.bucky/buckminister.properties 	-Dbuild.id=${TIMESTAMP}  -Dbuckminster.build.timestamp=${TIMESTAMP} -Dma.version=${VERSION} -Dma.release=${RELEASE} -Dmagic.build=${BUILD_DIR}
if [ $? -ne 0 ];then echo "Build failed"; exit 1; fi

rm -rf "$EXPORT_DIR/$RELEASE"
mkdir "$EXPORT_DIR/$RELEASE"
cp $RESULT/magicassistant*.zip $EXPORT_DIR/$RELEASE/
cp -r $RESULT/site.p2 $EXPORT_DIR/$RELEASE/
echo "Published results at  $EXPORT_DIR/$RELEASE/"
) 2>&1 | tee $LOG
fi



if [ "$INSTALL" -eq 1 ]; then
    echo Installing...
	cd $INSTALL_DIR
	rm -rf $INSTALL_DIR/*
        unzip $EXPORT_DIR/$RELEASE/magicassistant*win32*.zip
    echo Installed in $INSTALL_DIR
fi
if [ "$UPLOAD" -eq 1 ]; then
  echo "Uploading builds for $RELEASE..."
  $SCP -r -v -i "$SF_PRIVATE_KEY" $EXPORT_DIR/$RELEASE $SF_USER,mtgbrowser@frs.sourceforge.net:/home/frs/project/m/mt/mtgbrowser/Magic_Assistant/
fi
