export CONFIG_AIC_LOADFW_SUPPORT := m
export CONFIG_AIC8800_WLAN_SUPPORT := m
export CONFIG_AIC8800_BTUSB_SUPPORT := m

export CONFIG_PLATFORM_ROCKCHIP = y
export CONFIG_PLATFORM_UBUNTU = n
export CONFIG_USE_FW_REQUEST = n
export CONFIG_PREALLOC_RX_SKB = y
export CONFIG_PREALLOC_TXQ = y

obj-m := aic8800/aic_load_fw/ aic8800/aic8800_fdrv/ aic_btusb/

all: modules

modules:
	$(MAKE) -C $(KERNEL_SRC) M=$(CURDIR) modules

modules_install:
	$(MAKE) -C $(KERNEL_SRC) M=$(CURDIR) modules_install

clean:
	$(MAKE) -C $(KERNEL_SRC) M=$(CURDIR) clean
