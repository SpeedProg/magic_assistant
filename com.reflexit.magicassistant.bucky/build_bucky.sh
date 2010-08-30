#!/bin/sh 
#
# to run:
# UPLOAD=1 - to upload release
# TIMESTAMP=N - to force release 4th number (build id) to be N 
# e.g 
# UPLOAD=1 TIMESTAMP=4 ./build.sh
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
echo copy sources
SOURCE_DIR=$BUILD_DIR/sources
rm -rf $SOURCE_DIR
mkdir $SOURCE_DIR
cp -r $WORKSPACE/com.reflexit.magic* $SOURCE_DIR
cp -r $WORKSPACE/com.reflexit.mtg* $SOURCE_DIR

echo fixing build id
#fixing build id in about.ini
sed -e "s/Build id: xxxxxxxx/Build id: $RELEASE ($TIMESTAMP)/" $WORKSPACE/com.reflexit.magiccards-rcp/plugin.xml > $SOURCE_DIR/com.reflexit.magiccards-rcp/plugin.xml || die Cannot run subst
sed -e "s/Build id: xxxxxxxx/Build id: $RELEASE ($TIMESTAMP)/" $WORKSPACE/com.reflexit.magiccards.ui/about.properties > $SOURCE_DIR/com.reflexit.magiccards.ui/about.properties || die Cannot run subst


echo bucky build
RESULT="$BUILD_DIR/result/com.reflexit.magicassistant.bucky_1.0.0-eclipse.feature"
mv $BUILD_DIR/tmp $BUILD_DIR/result /c/tmp/w
rm -rf /c/tmp/w/*

$MAGIC_DIR/Bucky/buckminster/buckminster -data $BUILD_DIR/bucky_workspace/ \
  -S $SOURCE_DIR/com.reflexit.magicassistant.bucky/build.script \
  -vmargs -Dsource.dir=$SOURCE_DIR \
  -Dbuckyprops=$SOURCE_DIR/com.reflexit.magicassistant.bucky/buckminster.properties \
  -Dbuild.id=${TIMESTAMP} \
  -Dbuckminster.build.timestamp=${TIMESTAMP} \
  -Dma.version=${VERSION} -Dma.release=${RELEASE} -Dmagic.build=${BUILD_DIR} > $LOG 2>&1 
  
test $? -eq 0 || die Build failed see $LOG

rm -rf "$EXPORT_DIR/$RELEASE"
mkdir "$EXPORT_DIR/$RELEASE"
cp $RESULT/magicassistant*.zip $EXPORT_DIR/$RELEASE/
cp -r $RESULT/site.p2 $EXPORT_DIR/$RELEASE/
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
fi
