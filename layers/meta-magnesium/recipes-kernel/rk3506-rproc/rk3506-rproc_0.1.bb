SUMMARY = "Rockchip RK3506 Cortex-M0 Remote Processor Driver"
DESCRIPTION = "Out-of-tree Linux kernel remote processor driver for Rockchip RK3506 Cortex-M0 MCU"
HOMEPAGE = "https://github.com/nvitya/rk3506-mcu"
SECTION = "kernel/modules"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://rk3506_rproc.c;beginline=1;endline=1;md5=50d2ba0afecd20f74c12a4bdbcfcfe61"

inherit module

SRC_URI = " \
    file://Makefile \
    file://rk3506_rproc.c \
"

S = "${UNPACKDIR}"

COMPATIBLE_MACHINE = "(luckfox-lyra-plus|luckfox-lyra-pi)"

# Automatically load the kernel module on boot
KERNEL_MODULE_AUTOLOAD += "rk3506_rproc"

RPROVIDES:${PN} += "kernel-module-rk3506-rproc"
