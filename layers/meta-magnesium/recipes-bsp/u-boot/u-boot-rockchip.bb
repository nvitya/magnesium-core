# Copyright (C) 2019, Fuzhou Rockchip Electronics Co., Ltd
# Released under the MIT license (see COPYING.MIT for the terms)

PATCHPATH = "${CURDIR}/u-boot-rockchip"
inherit auto-patch

inherit local-git python3-dir

require recipes-bsp/u-boot/u-boot.inc
require recipes-bsp/u-boot/u-boot-common.inc

PROVIDES = "virtual/bootloader"

DEPENDS += "bc-native dtc-native"

PV = "2017.09"

LIC_FILES_CHKSUM = "file://Licenses/README;md5=a2c678cfd4a4d97135585cad908541c6"

SRCREV = "1c535d65b8509f388d09e49fb6961f49fda35a1d"
# Keep the RK3506B loader config and matching DDR firmware. The later master
# commit replaces these with non-B RK3506 assets and removes this INI.
SRCREV_rkbin = "affd78ce1a445b04ef143761b3164b7eeb740fc6"
SRC_URI = " \
	git://github.com/rockchip-linux/u-boot.git;protocol=https;branch=next-dev; \
	git://github.com/rockchip-linux/rkbin.git;protocol=https;branch=master;name=rkbin;destsuffix=rkbin; \
	file://luckfox-boot.cfg \
"

SRCREV_FORMAT = "default_rkbin"


do_configure:prepend() {
    sed -i 's/-Werror//g' ${S}/Makefile
}

DEPENDS:append = " ${PYTHON_PN}-native"

# Needed for packing BSP u-boot
DEPENDS:append = " coreutils-native ${PYTHON_PN}-pyelftools-native"

do_configure:prepend() {
	# Make sure we use /usr/bin/env ${PYTHON_PN} for scripts
	for s in `grep -rIl python ${S}`; do
		sed -i -e '1s|^#!.*python[23]*|#!/usr/bin/env ${PYTHON_PN}|' $s
	done

	# Support python3
	sed -i -e 's/\(open([^,]*\))/\1, "rb")/' \
		-e 's/print >> \([^,]*\), *\(.*\),*$/print(\2, file=\1)/' \
		-e 's/print \(.*\)$/print(\1)/' \
		${S}/arch/arm/mach-rockchip/make_fit_atf.py

	# Remove unneeded stages from make.sh
	sed -i -e '/^select_tool/d' -e '/^clean/d' -e '/^\t*make/d' -e '/which python2/{n;n;s/exit 1/true/}' ${S}/make.sh

	if [ "x${RK_ALLOW_PREBUILT_UBOOT}" = "x1" ]; then
		# Copy prebuilt images
		if [ -e "${S}/${UBOOT_BINARY}" ]; then
			bbnote "${PN}: Found prebuilt images."
			mkdir -p ${B}/prebuilt/
			mv ${S}/*.bin ${S}/*.img ${B}/prebuilt/
		fi
	fi

	[ ! -e "${S}/.config" ] || make -C ${S} mrproper

	sed -i 's/ found;/ found = NULL;/' ${S}/lib/avb/libavb/avb_slot_verify.c

	# Append custom bootcmd to evb_rk3506.h (safe as it overrides previous defines).
	#
	# fdt_overlays is a space-separated list of overlay filenames relative to
	# /boot.  Each overlay is loaded into fdtoverlay_addr_r and applied to the
	# working DTB.  Keep a default scratch address for the stock RK3506 memory
	# layout; users can override fdtoverlay_addr_r in the saved environment.
	echo '#undef CONFIG_BOOTCOMMAND' >> ${S}/include/configs/evb_rk3506.h
	echo '#define CONFIG_BOOTCOMMAND "ext4load mmc 0:2 ${kernel_addr_r} /boot/zImage; ext4load mmc 0:2 ${fdt_addr_r} /boot/devtree.dtb; fdt addr ${fdt_addr_r}; if test -n \"${fdt_overlays}\"; then if test -z \"${fdtoverlay_addr_r}\"; then setenv fdtoverlay_addr_r 0x01e00000; fi; fdt resize 0x10000; for overlay in ${fdt_overlays}; do ext4load mmc 0:2 ${fdtoverlay_addr_r} /boot/${overlay} && fdt apply ${fdtoverlay_addr_r}; done; fi; bootz ${kernel_addr_r} - ${fdt_addr_r}"' >> ${S}/include/configs/evb_rk3506.h
}

# Generate Rockchip style loader binaries
RK_IDBLOCK_IMG = "idblock.img"
RK_LOADER_BIN = "loader.bin"
RK_TRUST_IMG = "trust.img"
UBOOT_BINARY = "uboot.img"

do_compile:append() {
        # Create a symlink to rkbin in WORKDIR so make.sh and boot_merger find it at ../rkbin
        ln -snf ${UNPACKDIR}/rkbin ${WORKDIR}/rkbin

        cd ${B}

	if [ -e "${B}/prebuilt/${UBOOT_BINARY}" ]; then
		bbnote "${PN}: Using prebuilt images."
		ln -sf ${B}/prebuilt/*.bin ${B}/prebuilt/*.img ${B}/
	else
		# Prepare needed files
		for d in make.sh scripts configs arch/arm/mach-rockchip; do
			cp -rT ${S}/${d} ${d}
		done

		# Pack rockchip loader images (generates uboot.img, trust.img, generic idblock)
		./make.sh
		# Re-pack idblock with our freshly built u-boot-spl.bin
		./make.sh --spl
	fi

	ln -sf *_loader*.bin "${RK_LOADER_BIN}"

	# The SDK's make.sh already natively generates a valid *idblock*.img for newer SoCs (NEWIDB=true).
	# We just need to symlink it instead of rebuilding it incorrectly.
	bbnote "${PN}: Symlinking natively generated idblock.img"
	ln -sf *_idblock*.img "${RK_IDBLOCK_IMG}"
}

do_deploy:append() {
	cd ${B}

	for binary in "${RK_IDBLOCK_IMG}" "${RK_LOADER_BIN}" "${RK_TRUST_IMG}";do
		[ -f "${binary}" ] || continue
		install "${binary}" "${DEPLOYDIR}/${binary}-${PV}"
		ln -sf "${binary}-${PV}" "${DEPLOYDIR}/${binary}"
	done
}
