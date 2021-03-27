#!/bin/sh

set -e

version=$(curl https://bintray.com/api/v1/packages/ids1024/whitakers-words-android/master | \
	python -c 'import json, sys; print(json.load(sys.stdin)["latest_version"])')

rm -f words.tar.xz
curl -L https://dl.bintray.com/ids1024/whitakers-words-android/words-$version.tar.xz -o words.tar.xz

rm -rf src/main/assets/words
mkdir -p src/main/assets
tar xf words.tar.xz -C src/main/assets
echo "Words extracted to src/main/assets/words from $version on bintray."

rm -rf libs
mkdir -p libs/{armeabi-v7a,arm64-v8a}
cp src/main/assets/words/words libs/armeabi-v7a/libwords.so
cp src/main/assets/words/words libs/arm64-v8a/libwords.so
echo "Copied to libs/"
