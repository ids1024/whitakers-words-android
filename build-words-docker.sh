#!/bin/sh

IMAGE=ids1024/ada-android:6.4.0

sudo docker pull $IMAGE
# Disable seccomp for 'personality' syscall, which is used by bionic (in qemu)
sudo docker run \
	-v "$PWD/words:/words" \
	-w /words \
	-u $(id -u):$(id -g) \
	--security-opt seccomp=unconfined \
	$IMAGE \
	./build-words.sh
echo "Words built in words/words."
