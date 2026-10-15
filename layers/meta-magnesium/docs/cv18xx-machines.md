# Milk-V Duo and LicheeRV Nano

These machines run Linux on the large RISC-V C906 and expose the small C906
through Linux remoteproc. Duo S and Nano must be in RISC-V boot mode. The
initial storage target is microSD, with a FAT boot partition and an ext4 root
filesystem on partition 2.

| MACHINE | SoC | RAM | Linux memory bank | Remote core reservation / entry |
| --- | --- | --- | --- | --- |
| `milkv-duo` | CV1800B | 64 MiB | 63.25 MiB | `0x83f40000`, 768 KiB |
| `milkv-duo-256` | SG2002 / CV1812CP | 256 MiB | 254 MiB | `0x8fe00000`, 2 MiB |
| `milkv-duo-s` | SG2000 / CV1813H | 512 MiB | 510 MiB | `0x9fe00000`, 2 MiB |
| `licheerv-nano` | SG2002 | 256 MiB | 254 MiB | `0x8fe00000`, 2 MiB |

OpenSBI reserves another 512 KiB at `0x80000000` inside each Linux memory bank.
Kernel and userspace allocations further reduce the memory shown as available.
The remote core is independent of Linux; enabling it does not add an SMP CPU.

## Kernel selection and provenance

`linux-cvitek` pins the kernel-only [CVITEK BSP branch](https://github.com/scpcom/linux/tree/3e03379912ddc8d3f785aa00cf6bf4e7c334eda8)
at `3e03379912ddc8d3f785aa00cf6bf4e7c334eda8` (Linux 5.10.4). It does not depend
on `/lindata2/milkv/duo-64-kernel/linux_5.10-milkv` or its build artifacts.

Newer sources exist: [scpcom's SG200x 6.12 branch](https://github.com/scpcom/linux/tree/5110d21881a08df4244c5f8f3bff1c1627b3a305)
is Linux 6.12.110 and includes CVITEK remoteproc/mailbox drivers. Its vendor
platform code and DTS set target CV181x/SG200x; it lacks the CV180x vendor
platform implementation used by the 64 MiB Duo. This integration uses one
5.10 BSP for all four boards rather than treating the newer branch as a drop-in
replacement. The 5.10.4 version number is intentional; this is not a current
upstream 5.10 stable kernel.

The shared machine configuration sets `OLDEST_KERNEL = "5.10"` so glibc is
built for this BSP instead of OE's default Linux 5.15 runtime minimum.

Mailbox, reset and remoteproc patches originate from
[Fishwaldo's SG200x integration](https://github.com/Fishwaldo/sophgo-sg200x-debian/tree/master/configs/common/patches/linux).
Local patches remove the forced ION selection, permit CV180x remoteproc clock
control, correct reset-controller bounds, and replace the obsolete `sptbr` CSR
alias for current assemblers.

The layer also patches `kern-tools-native` to skip directories when scanning
`Kconfig*` paths. The BSP contains a `Kconfiglib` directory that otherwise
crashes Yocto's `do_kernel_configcheck` with `IsADirectoryError`. Configuration
auditing remains enabled.

Milk-V board DTSs and defconfigs originate from the
[official V1 SDK](https://github.com/milkv-duo/duo-buildroot-sdk/tree/23eb84fecb29585dbb5728d6b7e2475ff273baac/build/boards).
The Nano uses the CV1812CP Nano DTS in the pinned kernel, with headless policy
and board LED/UART settings. Its defconfig uses the same SG2002 settings as the
Duo 256M. The Nano LED/SD settings follow
[the Nano BSP integration](https://github.com/Fishwaldo/sophgo-sg200x-debian/blob/master/configs/licheervnano/dts/sg2002_licheervnano_sd.dts).

## Headless policy

The final kernel disables media/camera, DRM, framebuffer (including FBTFT),
backlight, touchscreen, sound, ION, and CMA. It also disables the vendor RTOS
command queue, which otherwise competes with the Linux mailbox driver.
Remoteproc, the CVITEK hardware driver, mailbox and RPMsg support are built in.
Configuration checks fail the build if required options disappear or multimedia
allocators become enabled again.

The board DTS disables CSI, DSI, video input/output, processing and codec nodes.
There is no ION heap, framebuffer, boot logo, or camera buffer reservation.
The vendor `fast_image` node is removed. Only the remote-core region and
OpenSBI remain reserved. No initramfs is built or loaded.

Wi-Fi/BT drivers and firmware for the Duo S and Nano are not integrated here.
Initial networking uses Ethernet on boards fitted with it; UART is available
on all four machines. Nano wireless-only variants need an external network
interface until their wireless support is added. The RK3506 `aic8800` and
`rk3506-rproc` packages are not installed on these machines.

## Building and booting

In `build/conf/local.conf`, select one of the exact machine names above:

```bitbake
MACHINE = "milkv-duo"
```

From the repository root:

```sh
source layers/openembedded-core/oe-init-build-env build
bitbake image-magnesium
```

Outputs in `tmp/deploy/images/${MACHINE}/` include the rootfs `.ext4` / `.tar.gz`,
`Image.gz`, the board DTB, and `uImage` (a FIT containing the compressed kernel
and the headless DTB). The rootfs also contains `/boot/uImage`; copy the deployed
`uImage` to the FAT partition whenever updating the kernel.

Provision the board's FSBL/OpenSBI/U-Boot `fip.bin` separately. This layer does
not build or deploy a CV18xx bootloader or create a flashable CV18xx SD image.
`bootimg-magnesium` is restricted to the Rockchip machines because its raw
loader offsets and GPT layout are incompatible with these boards.

Use a board-specific RISC-V FIP configured for **no camera/ISP/display firmware**,
with the secondary-core entry matching the table. Do not reuse an ISP-enabled
FIP: its firmware may access multimedia RAM reclaimed by Linux. FSBL's DRAM
initialization and pinmux must match the board. Keep the FIT DTB rather than
booting with a stock bootloader DTB that reserves camera memory.

On a suitably provisioned SD card, place `fip.bin` and `uImage` in FAT partition
1 and install the Magnesium rootfs in ext4 partition 2. At the vendor U-Boot
prompt, this boots the FIT with its embedded DTB and no ramdisk:

```text
setenv bootargs 'console=ttyS0,115200 root=/dev/mmcblk0p2 rootwait rw'
fatload mmc 0:1 0x81400000 uImage
bootm 0x81400000#config-1
```

The DTB supplies `console=ttyS0,115200 root=/dev/mmcblk0p2 rootwait rw`. If the
card is enumerated differently, adjust these bootargs. Setting the environment
explicitly also removes vendor `mem=`/`cma=` limits. A saved
vendor boot command that passes its own FDT or initramfs must be updated.

## Remote processor

After boot, inspect the registered processor:

```sh
cat /sys/class/remoteproc/remoteproc0/name
cat /sys/class/remoteproc/remoteproc0/state
cat /sys/class/remoteproc/remoteproc0/firmware
```

Automatic firmware start is disabled. Supply a C906 ELF at
`/lib/firmware/cvirtos.elf`, linked entirely inside the board's remote-core
reservation and using its entry address. This BSP driver relies on the boot
firmware's C906 entry setup; it does not relocate arbitrary firmware entry
points. Firmware must provide a valid `.resource_table` (it may be empty for
an application without RPMsg). RPMsg firmware must agree with the mailbox
channel assignments and the memory layout; enabling the Linux framework alone
does not make arbitrary Arduino/vendor RTOS binaries compatible.

```sh
echo cvirtos.elf > /sys/class/remoteproc/remoteproc0/firmware
echo start > /sys/class/remoteproc/remoteproc0/state
echo stop > /sys/class/remoteproc/remoteproc0/state
```

On hardware, verify UART/SD/Ethernet, inspect `/proc/iomem` and `free -m`, and
exercise remoteproc start/stop with the intended firmware before deployment.
No physical boards were available for validating those runtime paths.

## Build validation

The initial integration was checked with BitBake metadata parsing for all four
machines and an `image-magnesium` dependency dry run. All four kernel/module
configurations were cross-compiled with GCC 15.2 and binutils 2.46. The generated
DTBs passed checks for Linux/remote-core memory boundaries, the OpenSBI
reservation, enabled remoteproc/mailbox nodes, and disabled multimedia nodes.
FIT payloads were extracted and compared with the original kernels and DTBs.
The complete `bitbake image-magnesium` build has also passed for `milkv-duo-s`.
Its deployed rootfs was checked for the Linux 5.10 libc ABI tag and a
`/boot/uImage` identical to the deployed FIT. Complete Yocto image builds for
the other three machines and physical-board boot tests remain to be run.

The recipe runs the configuration and compiled-DTB checks during normal builds.
The DTB checker can also be run manually (requires `fdtget` from dtc):

```sh
python3 layers/meta-magnesium/recipes-kernel/linux/linux-cvitek/verify-headless.py \
    path/to/milkv-duo.dtb 0x04000000 0x000c0000
```
