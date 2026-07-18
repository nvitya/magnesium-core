SUMMARY = "Rockchip BSP Linux kernel 6.6 for RK3506 / Luckfox Lyra Plus"
DESCRIPTION = "Rockchip develop-6.6 BSP kernel with magnesium-core board DTS and kernel configuration"
SECTION = "kernel"
LICENSE = "GPL-2.0-only"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit kernel
inherit kernel-yocto

DEPENDS += "lz4-native"

COMPATIBLE_MACHINE = "(luckfox-lyra-plus|luckfox-lyra-pi)"

LINUX_VERSION = "6.6.89"
PV = "${LINUX_VERSION}+git${SRCPV}"

KBRANCH = "develop-6.6"

#FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}/${MACHINE}:${THISDIR}/${PN}:"
FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}/${MACHINE}:${THISDIR}/${BPN}:"

SRC_URI = " \
    git://github.com/rockchip-linux/kernel.git;protocol=https;branch=${KBRANCH} \
    file://defconfig \
    file://magnesium-core.cfg \
    file://rk3506g-luckfox-lyra-plus-magnesium.dts \
    file://rk3506-luckfox-lyra-magnesium.dtsi \
    file://rk3506b-luckfox-lyra-pi-sd.dts \
    file://rk3506-luckfox-lyra-ultra.dtsi \
"

# For first bring-up AUTOREV is convenient.
# For reproducible builds, replace this with a fixed commit hash.
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

# The DTS source is kept in the magnesium layer and copied into the kernel tree.
# Your source URL points to arch/arm/boot/dts/rockchip, so install there.

do_patch:append() {
    install -d ${S}/arch/arm/boot/dts/rockchip

    install -m 0644 ${UNPACKDIR}/rk3506g-luckfox-lyra-plus-magnesium.dts \
        ${S}/arch/arm/boot/dts/rockchip/rk3506g-luckfox-lyra-plus-magnesium.dts

    install -m 0644 ${UNPACKDIR}/rk3506-luckfox-lyra-magnesium.dtsi \
        ${S}/arch/arm/boot/dts/rockchip/rk3506-luckfox-lyra-magnesium.dtsi

    install -m 0644 ${UNPACKDIR}/rk3506b-luckfox-lyra-pi-sd.dts \
        ${S}/arch/arm/boot/dts/rockchip/rk3506b-luckfox-lyra-pi-sd.dts

    install -m 0644 ${UNPACKDIR}/rk3506-luckfox-lyra-ultra.dtsi \
        ${S}/arch/arm/boot/dts/rockchip/rk3506-luckfox-lyra-ultra.dtsi

    if ! grep -q "rk3506g-luckfox-lyra-plus-magnesium.dtb" \
        ${S}/arch/arm/boot/dts/rockchip/Makefile; then
        echo 'dtb-$(CONFIG_ARCH_ROCKCHIP) += rk3506g-luckfox-lyra-plus-magnesium.dtb' \
            >> ${S}/arch/arm/boot/dts/rockchip/Makefile
    fi

    if ! grep -q "rk3506b-luckfox-lyra-pi-sd.dtb" \
        ${S}/arch/arm/boot/dts/rockchip/Makefile; then
        echo 'dtb-$(CONFIG_ARCH_ROCKCHIP) += rk3506b-luckfox-lyra-pi-sd.dtb' \
            >> ${S}/arch/arm/boot/dts/rockchip/Makefile
    fi
}

# Use this when defconfig is a savedefconfig-style minimal defconfig.
# Yocto applies defconfig first, then applies .cfg fragments from SRC_URI.
KCONFIG_MODE = "alldefconfig"

do_install:append() {
    # Create symlink for devtree.dtb pointing to the machine's dtb
    if [ -n "${KERNEL_DEVICETREE}" ]; then
        for dtb in ${KERNEL_DEVICETREE}; do
            dtb_ext=${dtb##*.}
            dtb_base_name=`basename $dtb .$dtb_ext`
            # find the actual dtb in /boot (might be prefixed with devicetree-)
            real_dtb=$(find ${D}/boot -name "*${dtb_base_name}.${dtb_ext}" -printf "%f\n" | head -n 1)
            if [ -n "$real_dtb" ]; then
                ln -sf $real_dtb ${D}/boot/devtree.dtb
                break
            fi
        done
    fi

    # Create symlink for zImage
    real_zimage=$(find ${D}/boot -name "zImage-*" -printf "%f\n" | head -n 1)
    if [ -n "$real_zimage" ]; then
        ln -sf $real_zimage ${D}/boot/zImage
    fi
}

