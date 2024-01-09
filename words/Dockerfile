# sudo podman run --rm --privileged docker.io/multiarch/qemu-user-static --reset -p yes -c yes
# podman build --arch arm64 --tag words-build .

# Issue with dos2unix version in jammy
FROM ubuntu:mantic
RUN apt-get update && apt-get install -y gnat-12 unzip wget dos2unix

