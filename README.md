Whitaker's Words for Android
============================

[![Build Status](https://travis-ci.org/ids1024/whitakers-words-android.svg?branch=master)](https://travis-ci.org/ids1024/whitakers-words-android)

[`words`](http://archives.nd.edu/whitaker/words.htm) is a dictionary and morphological analysis tool by Colonel William Whitaker for Latin that accepts words of any form and gives the case/tense/etc. along with a short definition. This app provides a native Android interface that wraps the original command line program.

This app is on [Google Play](https://play.google.com/store/apps/details?id=com.ids1024.whitakerswords).

License
-------

Whitaker's Ada code is under the license in the [`words.LICENSE`](words.LICENSE) file, while all the code here is under the [MIT license](LICENSE).

Compiling
---------

### Downloading or building the words executable and data files

Building the Ada code is problematic, since Android's NDK only supports C and C++. The `words` directory has scripts for building a copy of GCC with Ada support, targeting Android. Moreover, it has data files that need to be build, and potentially differ by architecture. So the build script here uses `qemu` to generate those.

To provide such I toolchain, I've built a [docker image](https://hub.docker.com/r/ids1024/ada-android/), which you can download and use, or [build yourself](https://github.com/ids1024/ada-android-docker) (given enough CPU time and disk space). Prebuilt copies of words, automatically built by Travis CI, are available on [Bintray](https://bintray.com/ids1024/whitakers-words-android/master).

To compile this app, you'll need to obtain a copy of words in one of two ways:

Download prebuilt words from Bintray:

```bash
./fetch-words-bintray.sh
```

Or, download the docker image and build words:

```bash
./build-words-docker.sh
```

### Compiling the app

This uses the standard gradle build system for Android, so any documentation on that will apply, or you can use Android Studio.

For instance, `./gradlew installDebug` will build the app and install it on your device.
