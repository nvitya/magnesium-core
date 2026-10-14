FILESEXTRAPATHS:prepend := "${THISDIR}:"

SRC_URI += " \
	file://magnesium-uboot.cfg \
	file://rk3506_luckfox_defconfig \
	file://rk3506-luckfox.dts \
	file://rk3506-luckfox.dtsi \
"

do_configure:prepend() {
	# Install the SDK's Luckfox-specific defconfig before U-Boot configures.
	install -m 0644 ${UNPACKDIR}/rk3506_luckfox_defconfig \
		${S}/configs/rk3506_luckfox_defconfig
	install -m 0644 ${UNPACKDIR}/rk3506-luckfox.dts \
		${S}/arch/arm/dts/rk3506-luckfox.dts
	install -m 0644 ${UNPACKDIR}/rk3506-luckfox.dtsi \
		${S}/arch/arm/dts/rk3506-luckfox.dtsi
}
