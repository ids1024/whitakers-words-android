Whitaker's Words for Android
============================

[![Build Status](https://travis-ci.org/ids1024/whitakers-words-android.svg?branch=master)](https://travis-ci.org/ids1024/whitakers-words-android)

[`words`](http://archives.nd.edu/whitaker/words.htm) is a dictionary and morphological analysis tool by Colonel William Whitaker for Latin that accepts words of any form and gives the case/tense/etc. along with a short definition. This app provides a native Android interface that wraps the original command line program.

License
-------

Whitaker's Ada code is under the license in the [`words.LICENSE`](words.LICENSE) file, while all the code here is under the [MIT license](LICENSE).

Compiling
---------

### Building the words executable and data files

Building the Ada code is problematic, since Android's NDK only supports C and C++. The `words` directory has scripts for building a copy of GCC with Ada support, targeting Android. Moreover, it has data files that need to be build, and potentially differ by architecture. So the build script here uses `qemu` to generate those.

This takes a long time to build (and some disk space). So a compiled version of words 1.97FC is included in assets/words. But if you really want to compile it, this procedure should work:

```bash
wget https://dl.google.com/android/repository/android-ndk-r17b-linux-x86_64.zip
unzip android-ndk-r17b-linux-x86_64.zip
cd words
./ada-android.sh
./build-words.sh
```

### Compiling the app

This uses the standard gradle build system for Android, so any documentation on that will apply, or you can use Android Studio.

For instance, `./gradlew installDebug` will build the app and install it on your device.
