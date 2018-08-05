A compiled version of words 1.97FC is included in assets/words. This is because android ndk does not support Ada, nor do most cross compiler toolchains.

We therefore have to build own own compiler. The `words` directory has scripts for helping with this.

Compilation procedure:

```bash
wget https://dl.google.com/android/repository/android-ndk-r17b-linux-x86_64.zip
unzip android-ndk-r17b-linux-x86_64.zip
cd words
./ada-android.sh
./build-words.sh
```
