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

PATH="/usr/local/bin:/mingw/bin:/c/mingw/bin:/bin:/c/Windows/system32:/c/Windows:/c/Program Files (x86)/Vim/vim72:/c/Program Files/Subversion/bin:/C/Program Files (x86)/PuTTy:/C/Program Files/7-Zip"
Path=$PATH
export PATH

MAGIC_DIR=/C/Develop/magic
BUILD_DIR=${BUILD_DIR:-$MAGIC_DIR/build}
MA_WORKSPACE=$MAGIC_DIR/workspace
LOG=$BUILD_DIR/log
SF_USER=elaskavaia
SF_PRIVATE_KEY=$MAGIC_DIR/.ssh/id_rsa
SCP=${SCP:-scp.exe}

TIMESTAMP=`date +%Y%m%d%H%M`
UPLOAD=${UPLOAD:-0}
INSTALL=${INSTALL:-0}
UPDATE_SITE=${UPDATE_SITE:-0}
UPDATE_SITE_FULL=${UPDATE_SITE_FULL:-0}
UPDATE_DOCS=${UPDATE_DOCS:-0}
BUILD=${BUILD:-1}
VERSION=`grep Bundle-Version $MA_WORKSPACE/com.reflexit.magiccards-rcp/META-INF/MANIFEST.MF | sed -e 's?Bundle-Version: ??' -e 's?\.qualifier??'` 
RELEASE=$VERSION
if [ "$QUAL" != "" ]; then
	RELEASE=$VERSION.$QUAL
fi
OUTPUT=$BUILD_DIR/output
INSTALL_DIR=$MAGIC_DIR/install
EXPORT_DIR=$OUTPUT/export
UPDATE_ROOT="$EXPORT_DIR/update"
UPDATE_SITE_DIR="$UPDATE_ROOT/1.2"

echo Building $RELEASE
echo $BUILD_DIR

RESULT="$BUILD_DIR/result/com.reflexit.magicassistant.bucky_1.0.0-eclipse.feature"
if [ "$BUILD" -eq 1 ]; then
	echo Bucky build $TIMESTAMP
	
	$MAGIC_DIR/Bucky/buckminster/buckminster -data $BUILD_DIR/bucky_workspace/ \
	-S $MA_WORKSPACE/com.reflexit.magicassistant.bucky/build.script \
	-vmargs \
	-Dorig.workspace.root=$MA_WORKSPACE \
	-Dsource.root=$BUILD_DIR/sources \
	-Dmagic.build=$BUILD_DIR \
	-Dbuckyprops=$MA_WORKSPACE/com.reflexit.magicassistant.bucky/buckminster.properties \
	-Dbuild.id=${TIMESTAMP} \
	-Dbuckminster.build.timestamp=${TIMESTAMP} \
	-Dma.version=${VERSION} -Dma.release=${RELEASE} -Dmagic.build=${BUILD_DIR} 2>&1 | tee $LOG 

	test $? -eq 0 || { grep -i Error $LOG; die Build failed see $LOG; }

	if [ "$UPLOAD" -eq 1 ]; then
		$MAGIC_DIR/Bucky/buckminster/buckminster -data $BUILD_DIR/bucky_workspace/ \
		-S $MA_WORKSPACE/com.reflexit.magicassistant.bucky/build_zips.script \
		-vmargs \
		-Dorig.workspace.root=$MA_WORKSPACE \
		-Dsource.root=$BUILD_DIR/sources \
		-Dmagic.build=$BUILD_DIR \
		-Dbuckyprops=$MA_WORKSPACE/com.reflexit.magicassistant.bucky/buckminster.properties \
		-Dbuild.id=${TIMESTAMP} \
		-Dbuckminster.build.timestamp=${TIMESTAMP} \
		-Dma.version=${VERSION} -Dma.release=${RELEASE} -Dmagic.build=${BUILD_DIR} 2>&1 | tee $LOG 

		echo Creating self extracting archive
		(
		cd $RESULT
		mkdir -p repack
		cd repack
		rm -rf /c/tmp/w
		unzip -o ../magicassistant*win32*x86.zip -d /c/tmp/w
		cd /c/tmp/w/MagicAssistant
		cp -r "/C/Program Files (x86)/Java/jre7u9" jre 
		cd ..
		7z.exe a -t7z -mx5 -sfx7z.sfx $RESULT/magicassistant-intaller-$RELEASE-win32.exe MagicAssistant
		)

	fi
	
	echo Posting results
	rm -rf $OUTPUT; mkdir $OUTPUT
	mkdir -p "$UPDATE_ROOT"
	rm -rf "$UPDATE_SITE_DIR"
	cp -r $RESULT/site.p2 $UPDATE_SITE_DIR
	unzip $RESULT/magicassistant*win32*x86.zip -d $OUTPUT
fi



if [ "$INSTALL" -eq 1 ]; then
	echo Installing...
	cd $INSTALL_DIR
	rm -rf $INSTALL_DIR/$RELEASE
	mkdir $RELEASE
	unzip $RESULT/magicassistant*win32*x86.zip -d $RELEASE
	echo Installed in $INSTALL_DIR
fi

if [ "$UPLOAD" -eq 1 ]; then
	RELEASE_DIR="$EXPORT_DIR/$RELEASE"
	rm -rf "$RELEASE_DIR"; mkdir -p "$RELEASE_DIR"
	cp $RESULT/magicassistant*.zip $RELEASE_DIR/
	cp $RESULT/magicassistant*.exe $RELEASE_DIR/
	cp $MA_WORKSPACE/com.reflexit.magiccards-metadata/README.TXT $RELEASE_DIR/
	echo "Published results at $RELEASE_DIR/"
	echo "Uploading builds for $RELEASE..."
	$SCP -r -v -i "$SF_PRIVATE_KEY" $RELEASE_DIR $SF_USER,mtgbrowser@frs.sourceforge.net:/home/frs/project/m/mt/mtgbrowser/Magic_Assistant/
fi

if [ "$UPDATE_SITE" -eq 1 -o "$UPDATE_SITE_FULL" -eq 1 ]; then
	echo "Uploading update sute for $RELEASE..."
	(
	cd $UPDATE_SITE_DIR || die "No update site ready"
	unzip content.jar
	unzip artifacts.jar
	if [ "$UPDATE_SITE" -eq 1 ]; then
		#partial update
		$SCP -v -i "$SF_PRIVATE_KEY" binary/com.reflexit*  "$SF_USER,mtgbrowser@web.sourceforge.net:$REMOTE_PATH/binary/"
		$SCP -v -i "$SF_PRIVATE_KEY" features/com.reflexit*  "$SF_USER,mtgbrowser@web.sourceforge.net:$REMOTE_PATH/features/"
		$SCP -v -i "$SF_PRIVATE_KEY" plugins/com.reflexit*  "$SF_USER,mtgbrowser@web.sourceforge.net:$REMOTE_PATH/plugins/"
		$SCP -v -i "$SF_PRIVATE_KEY" *.xml *.jar "$SF_USER,mtgbrowser@web.sourceforge.net:$REMOTE_PATH/"
		#  $SCP -v -i "$SF_PRIVATE_KEY" features/org.eclipse.rcp*  "$SF_USER,mtgbrowser@web.sourceforge.net:$REMOTE_PATH/features/"
	else
		#full update
		$SCP -r -v -i "$SF_PRIVATE_KEY" "$UPDATE_SITE_DIR"  "$SF_USER,mtgbrowser@web.sourceforge.net:htdocs/update/"
	fi
	)
fi

if [ "$UPDATE_DOCS" -eq 1 ]; then
	echo "Uploading docs for $RELEASE..."
	"$SCP" -v -r -i "$SF_PRIVATE_KEY" "$MA_WORKSPACE/com.reflexit.magiccards.help/toc.html"  $SF_USER,mtgbrowser@web.sourceforge.net:htdocs/doc-plugins/com.reflexit.magiccards.help/
	"$SCP" -v -r -i "$SF_PRIVATE_KEY" "$MA_WORKSPACE/com.reflexit.magiccards.help/html/"  $SF_USER,mtgbrowser@web.sourceforge.net:htdocs/doc-plugins/com.reflexit.magiccards.help/
	#$SCP -r -batch -i "$SF_PRIVATE_KEY" "$MA_WORKSPACE/com.reflexit.magiccards.ui/icons/"  $SF_USER,mtgbrowser@web.sourceforge.net:htdocs/doc-plugins/com.reflexit.magiccards.ui/icons/
fi
