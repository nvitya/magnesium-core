SUMMARY = "WireGuard integration for Magnesium images"
DESCRIPTION = "Starts configured WireGuard interfaces after a network interface is brought up."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://50wireguard"

# The hook is unpacked directly into UNPACKDIR.
S = "${UNPACKDIR}"

inherit allarch

# Keep these hard dependencies: wg-quick needs both the userspace tools and
# the in-kernel WireGuard implementation available when ifup invokes its hook.
RDEPENDS:${PN} = "init-ifupdown kernel-module-wireguard wireguard-tools"

do_install() {
    install -d ${D}${sysconfdir}/network/if-up.d
    install -m 0755 ${UNPACKDIR}/50wireguard ${D}${sysconfdir}/network/if-up.d/50wireguard
}

FILES:${PN} = "${sysconfdir}/network/if-up.d/50wireguard"
