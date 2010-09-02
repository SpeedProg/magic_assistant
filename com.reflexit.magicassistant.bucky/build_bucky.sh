#!/bin/sh 
#
# to run:
# UPLOAD=1 - to upload release
# QUAL=N - to force release 4th number (build id) to be N 
# e.g 
# UPLOAD=1 QUAL=4 ./build.sh
#

die() {
  echo $*
  exit 1
}

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

TIMESTAMP=`date +%Y%m%d%H%M`
UPLOAD=${UPLOAD:-0}
INSTALL=${INSTALL:-0}
BUILD=${BUILD:-1}
VERSION=`grep Bundle-Version $WORKSPACE/com.reflexit.magiccards-rcp/META-INF/MANIFEST.MF | sed -e 's?Bundle-Version: ??' -e 's?\.qualifier??'` 
RELEASE=$VERSION
if [ "$QUAL" != "" ]; then
  RELEASE=$VERSION.$QUAL
fi
 
mkdir -p /c/tmp/w
echo Building $RELEASE

if [ "$BUILD" -eq 1 ]; then

RESULT="$BUILD_DIR/result/com.reflexit.magicassistant.bucky_1.0.0-eclipse.feature"


echo Bucky build $TIMESTAMP
$MAGIC_DIR/Bucky/buckminster/buckminster -data $BUILD_DIR/bucky_workspace/ \
  -S $WORKSPACE/com.reflexit.magicassistant.bucky/build.script \
  -vmargs \
  -Dorig.workspace.root=$WORKSPACE \
  -Dsource.root=$BUILD_DIR/sources \
  -Dbuckyprops=$WORKSPACE/com.reflexit.magicassistant.bucky/buckminster.properties \
  -Dbuild.id=${TIMESTAMP} \
  -Dbuckminster.build.timestamp=${TIMESTAMP} \
  -Dma.version=${VERSION} -Dma.release=${RELEASE} -Dmagic.build=${BUILD_DIR} 2>&1 | tee $LOG 
  
test $? -eq 0 || { grep -i Error $LOG; die Build failed see $LOG; }

echo Posting results
OUTPUT=$BUILD_DIR/output
rm -rf $OUTPUT
mkdir $OUTPUT
cp -r $RESULT/site.p2 $OUTPUT
rm -rf "$EXPORT_DIR/$RELEASE"
mkdir "$EXPORT_DIR/$RELEASE"
cp $RESULT/magicassistant*.zip $EXPORT_DIR/$RELEASE/
cp -r $RESULT/site.p2 $EXPORT_DIR/$RELEASE/1.1
(cd $OUTPUT; unzip $EXPORT_DIR/$RELEASE/magicassistant*win32*.zip;)
echo "Published results at $EXPORT_DIR/$RELEASE/"
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
  #$SCP -r -v -i "$SF_PRIVATE_KEY" "$EXPORT_DIR/$RELEASE/1.1/"  "$SF_USER,mtgbrowser@web.sourceforge.net:htdocs/update/"
fi
