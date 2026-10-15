require conf/machine/include/riscv/tune-riscv.inc

MACHINEOVERRIDES =. "cv18xx:"
DEFAULTTUNE = "riscv64"

PREFERRED_PROVIDER_virtual/kernel = "linux-cvitek"
PREFERRED_VERSION_linux-cvitek = "5.10%"
# Match glibc's runtime minimum to the BSP kernel, rather than OE's 5.15
# default. A libc built for the default minimum cannot run on this BSP.
OLDEST_KERNEL = "5.10"
KERNEL_IMAGETYPE = "Image.gz"
KERNEL_DEVICETREE = "cvitek/${MACHINE}.dtb"
SERIAL_CONSOLES = "115200;ttyS0"
MACHINE_FEATURES = "serial ethernet usbhost usbgadget ext2"

# Provision the board-specific FSBL/OpenSBI/U-Boot separately; these machines
# use FAT boot partitions and must never select the RK3506 bootloader/layout.
PREFERRED_PROVIDER_virtual/bootloader = "u-boot-dummy"

# Keep the SDK's secondary-core entry address, but reclaim all multimedia RAM.
CVITEK_DRAM_SIZE = "0x10000000"
CVITEK_RTOS_SIZE = "0x00200000"
CVITEK_SOC_FAMILY = "cv181x"

