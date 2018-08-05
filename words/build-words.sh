#!/bin/bash

set -e

export PATH=$PWD/toolchain/bin:$PATH
TARGET=arm-linux-androideabi
ANDROID_SYSROOT="$PWD/ndk-chain/sysroot"
BUILD_ARGS="-cargs -fPIE -largs -pie"
STATIC_ARGS="-bargs -static -largs -static"

echo "Removing directories..."
rm -rf words words-build

echo "Downloading words source..."
wget -nc http://archives.nd.edu/whitaker/wordsall.zip

mkdir words-build
pushd words-build
	echo "Extracting words..."
	unzip ../wordsall.zip

	echo "Fix line endings..."
	dos2unix ADDONS.LAT

	echo "Building words..."
	$TARGET-gnatmake -O3 words $BUILD_ARGS
	$TARGET-gnatmake makedict $STATIC_ARGS
	$TARGET-gnatmake makestem $STATIC_ARGS
	$TARGET-gnatmake makeefil $STATIC_ARGS
	$TARGET-gnatmake makeinfl $STATIC_ARGS

	echo "Stripping words..."
	$TARGET-strip words

	echo "Building data files in qemu..."
	echo G | qemu-arm ./makedict
	echo G | qemu-arm ./makestem
	echo G | qemu-arm ./makeefil
	echo G | qemu-arm ./makeinfl
popd

echo "Copying output to 'words'..."
mkdir words
cp words-build/{ADDONS.LAT,DICTFILE.GEN,EWDSFILE.GEN,INDXFILE.GEN,INFLECTS.SEC,STEMFILE.GEN,UNIQUES.LAT,words} words
