# Magnesium-Core Embedded Linux

## Introduction

Magnesium-Core is a small, headless, multi-purpose Linux base system targeting multiple devices.
The magnesium-core is a set of Yocto Linux-build configurations, to create an optimized Linux system.

Main features:
 * Fast boot time (under 10s)
 * Low RAM usage
 * Low disk usage
 * Simple, Classic Linux configs
 * Makes easier to special boot configurations
 * Makes easier to replace kernel or the device tree
 * Flexible u-boot with saveable environment and USB-Mass-Storage (ums) support

## Yocto Version

The project currently uses the Wrynose Yocto version as system base.

## Supported Devices

| Board Name | SoC |CPU Arch. | RAM | Comm. Features
+------------+-----------+----------+---------------
| Luckfox Lyra Plus   | RK3506  | 3 x ARM-A7 | 128 M | Ethernet (100MBit/s), USB2.0
| Luckfox Lyra Ultra  | RK3506B | 3 x ARM-A7 | 512 M | Ethernet (100MBit/s), WLAN, USB2.0
| Luckfox Lyra Pi     | RK3506B | 3 x ARM-A7 | 512 M | 2 x Ethernet (100MBit/s), WLAN, USB2.0
| Luckfox Lyra Zero W | RK3506B | 3 x ARM-A7 | 512 M | WLAN, USB2.0
