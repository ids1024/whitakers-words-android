# sudo podman run --rm --privileged docker.io/multiarch/qemu-user-static --reset -p yes -c yes
# podman build --arch arm64 --tag words-build .

# Issue with dos2unix version in jammy
FROM alpine:edge
RUN apk update && apk add gcc-gnat unzip wget dos2unix musl-dev

