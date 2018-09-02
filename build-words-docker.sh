#!/bin/sh

IMAGE=ids1024/ada-android:6.4.0

docker pull $IMAGE
# Disable seccomp for 'personality' syscall, which is used by bionic (in qemu)
docker run \
	-v "$PWD/words:/words" \
	-w /words \
	--security-opt seccomp=unconfined \
	$IMAGE \
	./build-words.sh
echo "Words built in words/words."
