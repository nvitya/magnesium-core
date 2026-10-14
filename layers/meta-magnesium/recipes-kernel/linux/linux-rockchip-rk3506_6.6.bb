SUMMARY = "Rockchip BSP Linux kernel 6.6 for RK3506 / Luckfox Lyra Plus"
DESCRIPTION = "Rockchip develop-6.6 BSP kernel with magnesium-core board DTS and kernel configuration"
SECTION = "kernel"
LICENSE = "GPL-2.0-only"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit kernel
inherit kernel-yocto

DEPENDS += "lz4-native"

COMPATIBLE_MACHINE = "(luckfox-lyra-plus|luckfox-lyra-pi|luckfox-lyra-zero-w)"

LINUX_VERSION = "6.6.89"
PV = "${LINUX_VERSION}+git${SRCPV}"

KBRANCH = "develop-6.6"

FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}/${MACHINE}:${THISDIR}/${BPN}:"

# Select only the device-tree sources belonging to the current machine.
RK3506_DTS:luckfox-lyra-plus = "rk3506g-luckfox-lyra-plus-magnesium.dts"
RK3506_DTS:luckfox-lyra-pi = "rk3506b-luckfox-lyra-pi-sd.dts"
RK3506_DTS:luckfox-lyra-zero-w = "rk3506b-luckfox-lyra-zero-w.dts"

RK3506_DTSI:luckfox-lyra-plus = "rk3506-luckfox-lyra-magnesium.dtsi"
RK3506_DTSI:luckfox-lyra-pi = "rk3506-luckfox-lyra-ultra.dtsi"
RK3506_DTSI:luckfox-lyra-zero-w = "rk3506-luckfox-lyra-ultra.dtsi"

SRC_URI = " \
    git://github.com/rockchip-linux/kernel.git;protocol=https;branch=${KBRANCH} \
    file://defconfig \
    file://magnesium-core.cfg \
    file://rv3032.cfg \
    file://${RK3506_DTS} \
    file://${RK3506_DTSI} \
    file://i2c2_rtc_rv3032.dtso \
"

# The RV-3032 is an optional daughterboard. Keep it as an overlay so the base
# DTB remains usable when the module is not fitted. All machines using this
# kernel get the same overlay artifact in /boot.
KERNEL_DEVICETREE:append = " rockchip/i2c2_rtc_rv3032.dtbo"

# The overlay targets the RK3506 I2C2 node by path, so the base DTB does not
# need global -@ symbol export. This keeps /omit-if-no-ref/ pinctrl groups
# pruned and avoids retaining thousands of otherwise-unused nodes.

# For reproducible builds, replace this with a fixed commit hash.
SRCREV = "1ba51b059f25533c5529b7f68186190b47d6a7b3"


# The DTS source is kept in the magnesium layer and copied into the kernel tree.
# Your source URL points to arch/arm/boot/dts/rockchip, so install there.

do_patch:append() {
    install -d ${S}/arch/arm/boot/dts/rockchip

    install -m 0644 ${UNPACKDIR}/${RK3506_DTS} \
        ${S}/arch/arm/boot/dts/rockchip/${RK3506_DTS}

    install -m 0644 ${UNPACKDIR}/${RK3506_DTSI} \
        ${S}/arch/arm/boot/dts/rockchip/${RK3506_DTSI}

    install -m 0644 ${UNPACKDIR}/i2c2_rtc_rv3032.dtso \
        ${S}/arch/arm/boot/dts/rockchip/i2c2_rtc_rv3032.dtso

    if ! grep -Fq "i2c2_rtc_rv3032.dtbo" \
        ${S}/arch/arm/boot/dts/rockchip/Makefile; then
        printf 'dtb-$(CONFIG_ARCH_ROCKCHIP) += i2c2_rtc_rv3032.dtbo\n' \
            >> ${S}/arch/arm/boot/dts/rockchip/Makefile
    fi

    dtb="$(basename "${RK3506_DTS}" .dts).dtb"
    if ! grep -Fq "$dtb" \
        ${S}/arch/arm/boot/dts/rockchip/Makefile; then
        printf 'dtb-$(CONFIG_ARCH_ROCKCHIP) += %s\n' "$dtb" \
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

