SUMMARY = "Magnesium Main Linux Image"
LICENSE = "MIT"

# Remember: This is a regex
#COMPATIBLE_MACHINE = ""

inherit core-image

IMAGE_CLASSES ?= "image_types"

IMAGE_INSTALL:append = " \
    busybox \
    base-files \
    base-passwd \
    netbase \
    openssh \
    shadow \
    util-linux \
    procps \
    strace \
    iproute2 \
    ethtool \
    gdbserver \
    init-ifupdown \
    i2c-tools \
    mc \
    opkg \
    readline \
    wget \
    htop \
    libgpiod-tools \
    libinput \
    gdbserver \
    nano \
    evtest \
    ${VIRTUAL-RUNTIME_dev_manager} \
    ${CORE_IMAGE_EXTRA_INSTALL} \
"

# Bake the kernel into the rootfs
IMAGE_INSTALL:append = " kernel-image kernel-devicetree kernel-modules"

# Pregenerated hostkeys (for development - remove in prod)
#IMAGE_INSTALL:append = " ssh-pregen-hostkeys"

# Output formats
IMAGE_FSTYPES = "tar.gz ext4"

# Explicitly rootfs-only
IMAGE_NO_BOOTLOADER = "1"

# No root password
EXTRA_IMAGE_FEATURES += "allow-empty-password allow-root-login empty-root-password"

# to include installed package database into the target
EXTRA_IMAGE_FEATURES += "package-management"

require ../../version-magnesium.inc

