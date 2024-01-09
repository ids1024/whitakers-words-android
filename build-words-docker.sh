#!/bin/bash

set -e

IMAGE=docker.io/ids1024/ada-android:6.4.0

podman pull $IMAGE

# Disable seccomp for 'personality' syscall, which is used by bionic (in qemu)
podman run \
	--rm \
	-v "$PWD/words:/words" \
	-w /words \
	-u $(id -u):$(id -g) \
	--security-opt seccomp=unconfined \
	$IMAGE \
	./build-words.sh

rm -rf src/main/assets/words
mkdir -p src/main/assets
mv words/words src/main/assets/words
echo "Words built in src/main/assets/words."

rm -rf libs
mkdir -p libs/{armeabi-v7a,arm64-v8a}
cp src/main/assets/words/words libs/armeabi-v7a/libwords.so
cp src/main/assets/words/words libs/arm64-v8a/libwords.so
echo "Copied to libs/"
