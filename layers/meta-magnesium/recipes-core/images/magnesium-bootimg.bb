SUMMARY = "Magnesium Boot Image Generator"
LICENSE = "MIT"

inherit image

IMAGE_FSTYPES = "wic"
WKS_FILE = "magnesium-boot.wks"

# Do not include a rootfs in this image
IMAGE_INSTALL = ""
IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""

# Build U-Boot and deploy the binaries consumed by the WIC rawcopy sources.
EXTRA_IMAGEDEPENDS += "virtual/bootloader"
do_image_wic[depends] += "virtual/bootloader:do_deploy"

# Prevent the build system from failing if the rootfs is empty
IMAGE_ROOTFS_SIZE = "0"
