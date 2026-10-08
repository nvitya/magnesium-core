obj-m := rk3506_rproc.o

SRC := $(shell pwd)

ifneq ($(KERNEL_SRC),)
# Yocto / OpenEmbedded out-of-tree module build
all:
	$(MAKE) -C $(KERNEL_SRC) M=$(SRC) modules

modules_install:
	$(MAKE) -C $(KERNEL_SRC) M=$(SRC) modules_install

clean:
	$(MAKE) -C $(KERNEL_SRC) M=$(SRC) clean
else
# Standalone build outside Yocto
KDIR ?= ./kernel-source
ARCH_ARGS ?= ARCH=arm CROSS_COMPILE=arm-linux-gnueabihf-

all:
	$(MAKE) $(ARCH_ARGS) -C $(KDIR) M=$(SRC) modules

modules_install:
	$(MAKE) $(ARCH_ARGS) -C $(KDIR) M=$(SRC) modules_install

clean:
	$(MAKE) -C $(KDIR) M=$(SRC) clean
endif
