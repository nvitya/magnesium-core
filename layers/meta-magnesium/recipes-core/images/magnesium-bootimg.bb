SUMMARY = "Magnesium Boot Image Generator"
LICENSE = "MIT"

inherit image

IMAGE_FSTYPES = "wic"
WKS_FILE = "magnesium-boot.wks"

# Do not include a rootfs in this image
IMAGE_INSTALL = ""
IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""

# Since U-Boot is not currently integrated in the build, you will need to manually
# Since U-Boot is now integrated, we depend on it to produce the binaries for WIC
EXTRA_IMAGEDEPENDS += "virtual/bootloader"

# Prevent the build system from failing if the rootfs is empty
IMAGE_ROOTFS_SIZE = "0"
