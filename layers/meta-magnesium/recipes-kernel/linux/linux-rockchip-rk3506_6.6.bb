SUMMARY = "Rockchip BSP Linux kernel 6.6 for RK3506 / Luckfox Lyra Plus"
DESCRIPTION = "Rockchip develop-6.6 BSP kernel with magnesium-core board DTS and kernel configuration"
SECTION = "kernel"
LICENSE = "GPL-2.0-only"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit kernel
inherit kernel-yocto

COMPATIBLE_MACHINE = "luckfox-lyra-plus"

LINUX_VERSION = "6.6"
PV = "${LINUX_VERSION}+git${SRCPV}"

KBRANCH = "develop-6.6"

SRC_URI = " \
    git://github.com/rockchip-linux/kernel.git;protocol=https;branch=${KBRANCH} \
    file://defconfig \
    file://magnesium-core.cfg \
    file://rk3506g-luckfox-lyra-plus-magnesium.dts \
    file://rk3506-luckfox-lyra-magnesium.dtsi \
"

# For first bring-up AUTOREV is convenient.
# For reproducible builds, replace this with a fixed commit hash.
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

# The DTS source is kept in the magnesium layer and copied into the kernel tree.
# Your source URL points to arch/arm/boot/dts/rockchip, so install there.
do_patch:append() {
    install -d ${S}/arch/arm/boot/dts/rockchip

    install -m 0644 ${WORKDIR}/rk3506g-luckfox-lyra-plus-magnesium.dts \
        ${S}/arch/arm/boot/dts/rockchip/rk3506g-luckfox-lyra-plus-magnesium.dts

    install -m 0644 ${WORKDIR}/rk3506-luckfox-lyra-magnesium.dtsi \
        ${S}/arch/arm/boot/dts/rockchip/rk3506-luckfox-lyra-magnesium.dtsi

    # Make the DTB visible to normal "make dtbs".
    # Direct DTB targets often work without this, but adding it is safer.
    if ! grep -q "rk3506g-luckfox-lyra-plus-magnesium.dtb" \
        ${S}/arch/arm/boot/dts/rockchip/Makefile; then
        echo 'dtb-$(CONFIG_ARCH_ROCKCHIP) += rk3506g-luckfox-lyra-plus-magnesium.dtb' \
            >> ${S}/arch/arm/boot/dts/rockchip/Makefile
    fi
}

# Use this when defconfig is a savedefconfig-style minimal defconfig.
# Yocto applies defconfig first, then applies .cfg fragments from SRC_URI.
KCONFIG_MODE = "alldefconfig"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}/${MACHINE}:${THISDIR}/${PN}:"
