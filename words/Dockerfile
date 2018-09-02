FROM alpine:3.8 AS build
LABEL maintainer="Ian Douglas Scott <ian@iandouglasscott.com>"

RUN wget https://dl.google.com/android/repository/android-ndk-r17b-linux-x86_64.zip \
    && unzip android-ndk-r17b-linux-x86_64.zip \
    && rm android-ndk-r17b-linux-x86_64.zip
ENV ANDROID_NDK_HOME="/android-ndk-r17b"

RUN apk add --no-cache \
    # needed by the ndk
    bash coreutils python \
    # needed to build gcc
    build-base gcc-gnat zlib-dev

WORKDIR /ada-android

RUN $ANDROID_NDK_HOME/build/tools/make-standalone-toolchain.sh \
    --arch=arm \
    --platform=android-14 \
    --stl=libc++ \
    --install-dir=./ndk-chain

ARG GCC_URL=https://ftp.gnu.org/gnu/gcc/gcc-6.4.0/gcc-6.4.0.tar.xz
ARG BINUTILS_URL=https://ftp.gnu.org/gnu/binutils/binutils-2.28.tar.bz2

# Download and extract binutils and gcc
RUN wget $GCC_URL $BINUTILS_URL \
    && tar xf gcc-* \
    && tar xf binutils-* \
    && rm *.tar.* \
    && mv binutils-* binutils \
    && mv gcc-* gcc \
    && cd gcc \
    && ./contrib/download_prerequisites

# https://developer.android.com/ndk/guides/abis#v7a
# Since Android 5.0, only PIE executables are supported.
# PIE doesn't work on 4.0 and earlier; static linking solves that.
ARG CONFIGURE_ARGS="\
    --prefix=/ada-android/toolchain \
    --target=arm-linux-androideabi \
    --with-sysroot=/ada-android/ndk-chain/sysroot \
    --with-float=soft \
    --with-fpu=vfpv3-d16 \
    --with-arch=armv7-a \
    --enable-languages=ada \
    --enable-threads=posix \
    --enable-shared \
    --enable-default-pie \
    --disable-tls \
    --enable-initfini-array \
    --disable-nls \
    --enable-plugins \
    --disable-werror \
    --with-system-zlib \
    --disable-gdb \
    CFLAGS_FOR_TARGET=-D__ANDROID_API__=14"

# Build binutils
RUN cd binutils \
    && mkdir build \
    && cd build \
    && ../configure $CONFIGURE_ARGS \
    && make -j$(nproc) \
    && make install

# Build gcc
COPY ada-musl.patch /ada-android/
RUN cd gcc \
    && patch -p1 -i ../ada-musl.patch \
    && mkdir build \
    && cd build \
    && ../configure $CONFIGURE_ARGS \
    && make -j$(nproc) \
    && make install

RUN strip $(find toolchain/bin toolchain/libexec -type f) || true

FROM alpine:3.8
COPY --from=build /ada-android/toolchain/ /usr/
COPY --from=build /ada-android/ndk-chain/sysroot/usr/lib/ \
                  /usr/arm-linux-androideabi/lib/armv7-a/
ENV LD_LIBRARY_PATH=/usr/x86_64-pc-linux-gnu/arm-linux-androideabi/lib
