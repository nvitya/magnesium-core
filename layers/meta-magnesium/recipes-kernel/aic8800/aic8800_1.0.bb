SUMMARY = "Aicsemi AIC8800 Wi-Fi and Bluetooth Linux Driver"
DESCRIPTION = "Out-of-tree Linux kernel driver and firmware for Aicsemi AIC8800DC USB Wi-Fi 6 and Bluetooth 5.2"
SECTION = "kernel/modules"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://aic_btusb/aic_btusb.c;beginline=8;endline=11;md5=890ed7f154d50dbcd742efa66a077629"

inherit module

SRC_URI = " \
    file://Makefile \
    file://aic8800 \
    file://aic_btusb \
    file://firmware \
"

S = "${UNPACKDIR}"

COMPATIBLE_MACHINE = "rk3506"

# Autoload drivers on boot in the proper order
KERNEL_MODULE_AUTOLOAD += "aic_load_fw aic8800_fdrv aic_btusb"

do_install:append() {
    install -d ${D}${nonarch_base_libdir}/firmware/aic8800DC
    install -m 0644 ${S}/firmware/* ${D}${nonarch_base_libdir}/firmware/aic8800DC/

    # Also symlink into /lib/firmware for drivers that look in the root firmware directory
    for f in ${D}${nonarch_base_libdir}/firmware/aic8800DC/*; do
        ln -sf aic8800DC/$(basename $f) ${D}${nonarch_base_libdir}/firmware/$(basename $f)
    done
}

FILES:${PN} += "${nonarch_base_libdir}/firmware"

RDEPENDS:${PN} += "kernel-module-cfg80211"
RPROVIDES:${PN} += "kernel-module-aic8800"
