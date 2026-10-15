# Include the AP daemon but do not add SysV startup links. A board-specific
# hostapd configuration and explicit enablement are required to start an AP.
INHIBIT_UPDATERCD_BBCLASS = "1"
