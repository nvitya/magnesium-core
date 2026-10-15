# Magnesium-Core  Linux Build Configurations

This repository contains all necessary instructions and data to build the Magnesium Yocto Image.
The Magnesium-Core configurations here currently are using the `wrynose` (LTS) Yocto version.

# Currently Supported Target Images

| Image Name         | Description                                            |
| ------------------ | ------------------------------------------------------ |
| `magnesium-main`   | Magnesium Main Linux Image                             |

# Currently Supported Machines

| Machine Name           | Description                                   |
| ---------------------- | --------------------------------------------- |
| `luckfox-lyra-plus`    | Luckfox Lyra Plus                             |
| `luckfox-lyra-ultra`   | Luckfox Lyra Ultra WiFi                       |
| `luckfox-lyra-ultra-w` | Luckfox Lyra Ultra WiFi                       |

| `luckfox-lyra-pi`      | Luckfox Lyra Pi-B WiFi                        |
| `luckfox-lyra-zero-w`  | Luckfox Lyra Zero W                           |
| `milkv-duo`           | Milk-V Duo, 64 MiB (RISC-V)                   |
| `milkv-duo-s`         | Milk-V Duo S, 512 MiB (RISC-V)                |
| `milkv-duo-256`       | Milk-V Duo 256M, 256 MiB (RISC-V)             |
| `licheerv-nano`       | Sipeed LicheeRV Nano, 256 MiB (RISC-V)         |

For the Duo/Nano machines, see the [CV18xx build and boot instructions](layers/meta-magnesium/docs/cv18xx-machines.md).
They use a headless kernel with remoteproc and require a separately provisioned
board-specific RISC-V bootloader. The Rockchip boot image below is not suitable
for these machines.

# Development Environment Setup

### Clone the repository:

`git clone ???  magnesium-core`

### Initialize the submodules/layers:

`git submodule update --init --recursive`


The compilation works natively on an Ubuntu 22.04 or 24.04 Host.

# Image Compilation

### Initialize the environment:
`source layers/openembedded-core/oe-init-build-env build`

### Build the bootimg

`bitbake magnesium-bootimg`

After copying the `.wic` file to the sdcard the GPT partition must be repaired
using the gdisk /dev/sdX

Menu commands: x, e, m, w

The boot command does not set correctly for now, stop the u-boot (with ctrl-c)
and enter these:

```
setenv bootcmd 'ext4load mmc 0:2 ${kernel_addr_r} /boot/zImage; ext4load mmc 0:2 ${fdt_addr_r} /boot/devtree.dtb; bootz ${kernel_addr_r} - ${fdt_addr_r}'
setenv bootargs root=/dev/mmcblk0p2
saveenv
boot
```

### Build the target:

`bitbake image-magnesium`
