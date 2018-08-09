#!/bin/bash

set -e

BINUTILS_VERSION=2.30
GCC_VERSION=8.1.0
NDK_VERSION=r17b
NDK="$PWD/android-ndk-$NDK_VERSION"
ANDROID_SYSROOT="$PWD/ndk-chain/sysroot"
DEST=$PWD/toolchain
TARGET=arm-linux-androideabi
PLATFORM=14
CFLAGS="-D__ANDROID_API__=$PLATFORM"

combined_dir=gcc-$GCC_VERSION-binutils-$BINUTILS_VERSION

echo "Removing directories..."
rm -rf binutils-$BINUTILS_VERSION gcc-$GCC_VERSION $combined_dir ndk-chain $DEST

echo "Downloading Android NDK..."
wget -nc https://dl.google.com/android/repository/android-ndk-$NDK_VERSION-linux-x86_64.zip

echo "Extracting Android NDK..."
if [ ! -d "$NDK" ]; then
	unzip android-ndk-$NDK_VERSION-linux-x86_64.zip
fi

echo "Creating standalone ndk toolchain..."
$NDK/build/tools/make-standalone-toolchain.sh --arch=arm --platform=android-$PLATFORM --stl=libc++ --install-dir=$PWD/ndk-chain

echo "Downloading gcc..."
wget -nc https://ftp.gnu.org/gnu/gcc/gcc-$GCC_VERSION/gcc-$GCC_VERSION.tar.xz
echo "Downloading binutils..."
wget -nc https://ftp.gnu.org/gnu/binutils/binutils-$BINUTILS_VERSION.tar.xz

echo "Extracting binutils..."
tar -xf binutils-$BINUTILS_VERSION.tar.xz
echo "Extracting gcc..."
tar -xf gcc-$GCC_VERSION.tar.xz

echo "Merging binutils and gcc source trees..."
mkdir $combined_dir
cp -r binutils-$BINUTILS_VERSION/* $combined_dir/
rm -rf binutils-$BINUTILS_VERSION
cp -r gcc-$GCC_VERSION/* $combined_dir/
rm -rf gcc-$GCC_VERSION

cd $combined_dir
echo "Downloading prerequisites..."
./contrib/download_prerequisites

mkdir build
cd build

../configure --prefix=$DEST --target=$TARGET --with-gnu-as --with-gnu-ld --enable-languages=c,c++,ada --disable-libssp --enable-threads --disable-libmudflap --disable-libstdc__-v3 --disable-sjlj-exceptions --enable-shared --disable-tls --disable-libitm --with-float=soft --with-fpu=vfpv3-d16 --with-arch=armv7-a --enable-target-optspace --enable-initfini-array --disable-nls --with-sysroot=$ANDROID_SYSROOT --disable-bootstrap --enable-plugins --enable-libgomp --enable-gnu-indirect-function --disable-libsanitizer --enable-eh-frame-hdr-for-static --enable-graphite=yes --enable-vtable-verify --disable-werror --with-isl --with-system-zlib CFLAGS_FOR_TARGET="$CFLAGS"

make -j$(nproc)
make install
