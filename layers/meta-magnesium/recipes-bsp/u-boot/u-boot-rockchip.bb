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

SRCREV = "4d88b0a83c87488f343fb4cc4f56ffc598b2e0a3"
SRCREV_rkbin = "32ccaf811ae70ce050aa810869c63c2b34324d59"
SRC_URI = " \
	git:///lindata2/luckfox/luckfox_sdk_2508/u-boot;protocol=file;nobranch=1; \
	git:///lindata2/luckfox/luckfox_sdk_2508/rkbin;protocol=file;nobranch=1;name=rkbin;destsuffix=rkbin; \
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

	# Append custom bootcmd to evb_rk3506.h (safe as it overrides previous defines)
	echo '#undef CONFIG_BOOTCOMMAND' >> ${S}/include/configs/evb_rk3506.h
	echo '#define CONFIG_BOOTCOMMAND "ext4load mmc 0:2 ${kernel_addr_r} /boot/zImage; ext4load mmc 0:2 ${fdt_addr_r} /boot/devtree.dtb; bootz ${kernel_addr_r} - ${fdt_addr_r}"' >> ${S}/include/configs/evb_rk3506.h
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
