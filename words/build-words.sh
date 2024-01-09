#!/bin/sh

set -e

export PATH=$PWD/toolchain/bin:$PATH
# TARGET=arm-linux-androideabi-
# QEMU=qemu-arm
ANDROID_SYSROOT="$PWD/ndk-chain/sysroot"
BUILD_ARGS="-cargs -fPIE -largs -pie"
STATIC_ARGS="-bargs -static -largs -static"

echo "Removing directories..."
rm -rf words words-build

echo "Downloading words source..."
if [ ! -f wordsall.zip ]
then
	wget https://archives.nd.edu/whitaker/old/wordsall.zip
fi

mkdir words-build
cd words-build
	echo "Extracting words..."
	unzip ../wordsall.zip

	echo "Fix line endings..."
	dos2unix -O ADDONS.LAT > ADDONS.LAT.new
	mv ADDONS.LAT.new ADDONS.LAT

	echo "Building words..."
	${TARGET}gnatmake -O3 words $STATIC_ARGS
	${TARGET}gnatmake makedict $STATIC_ARGS
	${TARGET}gnatmake makestem $STATIC_ARGS
	${TARGET}gnatmake makeefil $STATIC_ARGS
	${TARGET}gnatmake makeinfl $STATIC_ARGS

	echo "Building data files in qemu..."
	echo G | $QEMU ./makedict
	echo G | $QEMU ./makestem
	echo G | $QEMU ./makeefil
	echo G | $QEMU ./makeinfl | sed '/\*\*\*\*/d'

	echo "Copying output to 'words'..."
	mkdir ../words
	cp ADDONS.LAT DICTFILE.GEN EWDSFILE.GEN INDXFILE.GEN INFLECTS.SEC STEMFILE.GEN UNIQUES.LAT words ../WORD.MDV ../words
cd ..

echo "Stripping words..."
${TARGET}strip words/words
