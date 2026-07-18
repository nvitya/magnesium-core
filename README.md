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
| `luckfox-lyra-pi-b-w`  | Luckfox Lyra Pi-B WiFi                        |
| `milkv-duos`           | Milk-V Duo-S                                  |

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
saveenv
boot
```

### Build the target:

`bitbake image-magnesium`
