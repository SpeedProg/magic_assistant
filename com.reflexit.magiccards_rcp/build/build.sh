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
BUILD=${BUILD:-0}
RUN=${RUN:-0}
VERSION=`grep Bundle-Version $MA_WORKSPACE/com.reflexit.magiccards_rcp/META-INF/MANIFEST.MF | sed -e 's?Bundle-Version: ??' -e 's?\.qualifier??'`
QUAL=${QUAL:-`grep qual= build.properties | sed -e 's/qual=//'`} 
RELEASE=$VERSION
if [ "$QUAL" != "" ]; then
	RELEASE=$VERSION.$QUAL
fi
OUTPUT=$BUILD_DIR/output
INSTALL_DIR=$MAGIC_DIR/install
EXPORT_DIR=$BUILD_DIR/export
UPDATE_ROOT="$EXPORT_DIR/update"
UPDATE_SITE_DIR="$UPDATE_ROOT/1.4"

echo Release $RELEASE
echo Build Dir $BUILD_DIR

RESULT=$BUILD_DIR/I.$RELEASE
ECLIPSE_BASE=C:/Develop/Eclipse/Eclipse4.2.1/eclipse
PDE_BUILD_HOME=$ECLIPSE_BASE/plugins/org.eclipse.pde.build_3.8.1.v20120725-202643/
ANT="$ECLIPSE_BASE/plugins/org.apache.ant_1.8.3.v20120321-1730/bin/ant -Declipse.pdebuild.home=$PDE_BUILD_HOME -Declipse.pdebuild.scripts=$PDE_BUILD_HOME/scripts  -Declipse.pdebuild.templates=$PDE_BUILD_HOME/templates -f pdebuild.ant"
if [ "$BUILD" -eq 1 ]; then
	echo Building $RELEASE
	$ANT -Dqual=$QUAL build.product export.update.site pack.jre 2>&1 | tee $LOG 

	test $? -eq 0 || { grep -i Error $LOG; die Build failed see $LOG; }
fi

if [ "$INSTALL" -eq 1 ]; then
	echo Installing...
	$ANT install
fi

if [ "$RUN" -eq 1 ]; then
		echo Running
		$ANT run
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

if [ "$UPDATE_SITE" -gt 0 ]; then
	echo "Uploading update sute for $RELEASE..."
	(
	cd $UPDATE_SITE_DIR || die "No update site ready"
	REMOTE_PATH="htdocs/update/1.4"
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

if [ "$UPLOAD" -eq 1 -o "$UPDATE_SITE" -gt 0 ]; then
	$ANT fix.qual 2>&1 | tee $LOG 
fi

if [ "$UPDATE_DOCS" -eq 1 ]; then
	echo "Uploading docs for $RELEASE..."
	"$SCP" -v -r -i "$SF_PRIVATE_KEY" "$MA_WORKSPACE/com.reflexit.magiccards.help/toc.html"  $SF_USER,mtgbrowser@web.sourceforge.net:htdocs/doc-plugins/com.reflexit.magiccards.help/
	"$SCP" -v -r -i "$SF_PRIVATE_KEY" "$MA_WORKSPACE/com.reflexit.magiccards.help/html/"  $SF_USER,mtgbrowser@web.sourceforge.net:htdocs/doc-plugins/com.reflexit.magiccards.help/
	#$SCP -r -batch -i "$SF_PRIVATE_KEY" "$MA_WORKSPACE/com.reflexit.magiccards.ui/icons/"  $SF_USER,mtgbrowser@web.sourceforge.net:htdocs/doc-plugins/com.reflexit.magiccards.ui/icons/
fi
