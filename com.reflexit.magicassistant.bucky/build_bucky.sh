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
UPDATE_SITE=${UPDATE_SITE:-0}
UPDATE_SITE_FULL=${UPDATE_SITE_FULL:-0}
UPDATE_DOCS=${UPDATE_DOCS:-0}
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
cp $WORKSPACE/com.reflexit.magiccards-metadata/README.TXT $EXPORT_DIR/$RELEASE/
rm -rf $EXPORT_DIR/update
mkdir $EXPORT_DIR/update
cp -r $RESULT/site.p2 $EXPORT_DIR/update/1.2
(cd $OUTPUT; unzip $EXPORT_DIR/$RELEASE/magicassistant*win32*x86.zip;)
echo "Published results at $EXPORT_DIR/$RELEASE/"
fi



if [ "$INSTALL" -eq 1 ]; then
    echo Installing...
	cd $INSTALL_DIR
	rm -rf $INSTALL_DIR/$RELEASE
	mkdir $RELEASE
	cd $RELEASE
    unzip $EXPORT_DIR/$RELEASE/magicassistant*win32*x86.zip
    echo Installed in $INSTALL_DIR
fi

if [ "$UPLOAD" -eq 1 ]; then
  echo "Uploading builds for $RELEASE..."
  $SCP -r -v -i "$SF_PRIVATE_KEY" $EXPORT_DIR/$RELEASE $SF_USER,mtgbrowser@frs.sourceforge.net:/home/frs/project/m/mt/mtgbrowser/Magic_Assistant/
fi

if [ "$UPDATE_SITE" -eq 1 ]; then
  echo "Uploading update sute for $RELEASE..."
  REMOTE_PATH="htdocs/update/1.2"
  (
  cd $EXPORT_DIR/update/1.2/
  unzip content.jar
  unzip artifacts.jar
  #partial update
  $SCP -v -i "$SF_PRIVATE_KEY" binary/com.reflexit*  "$SF_USER,mtgbrowser@web.sourceforge.net:$REMOTE_PATH/binary/"
  $SCP -v -i "$SF_PRIVATE_KEY" features/com.reflexit*  "$SF_USER,mtgbrowser@web.sourceforge.net:$REMOTE_PATH/features/"
  $SCP -v -i "$SF_PRIVATE_KEY" plugins/com.reflexit*  "$SF_USER,mtgbrowser@web.sourceforge.net:$REMOTE_PATH/plugins/"
  $SCP -v -i "$SF_PRIVATE_KEY" *.xml *.jar "$SF_USER,mtgbrowser@web.sourceforge.net:$REMOTE_PATH/"
#  $SCP -v -i "$SF_PRIVATE_KEY" features/org.eclipse.rcp*  "$SF_USER,mtgbrowser@web.sourceforge.net:$REMOTE_PATH/features/"
  )
fi
if [ "$UPDATE_SITE_FULL" -eq 1 ]; then
  echo "Uploading update sute for $RELEASE..."
  REMOTE_PATH="htdocs/update/1.2"
  (
  cd $EXPORT_DIR/update/1.2/
  unzip content.jar
  unzip artifacts.jar
  $SCP -r -v -i "$SF_PRIVATE_KEY" "$EXPORT_DIR/update/1.2/"  "$SF_USER,mtgbrowser@web.sourceforge.net:htdocs/update/"
  )
fi

if [ "$UPDATE_DOCS" -eq 1 ]; then
	echo "Uploading docs for $RELEASE..."
	"$SCP" -v -r -i "$SF_PRIVATE_KEY" "$WORKSPACE/com.reflexit.magiccards.help/toc.html"  $SF_USER,mtgbrowser@web.sourceforge.net:htdocs/doc-plugins/com.reflexit.magiccards.help/
	"$SCP" -v -r -i "$SF_PRIVATE_KEY" "$WORKSPACE/com.reflexit.magiccards.help/html/"  $SF_USER,mtgbrowser@web.sourceforge.net:htdocs/doc-plugins/com.reflexit.magiccards.help/
	#$SCP -r -batch -i "$SF_PRIVATE_KEY" "$WORKSPACE/com.reflexit.magiccards.ui/icons/"  $SF_USER,mtgbrowser@web.sourceforge.net:htdocs/doc-plugins/com.reflexit.magiccards.ui/icons/
fi
