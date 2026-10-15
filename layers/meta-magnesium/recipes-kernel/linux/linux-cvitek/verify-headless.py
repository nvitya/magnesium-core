#!/usr/bin/env python3
"""Check a compiled CV18xx DTB against the headless memory/remoteproc contract."""
import subprocess
import struct
import sys


def verify(dtb, dram_size, rtos_size):
    with open(dtb, "rb") as stream:
        blob = stream.read()
    offset = struct.unpack_from(">I", blob, 16)[0]
    reservations = []
    while True:
        address, size = struct.unpack_from(">QQ", blob, offset)
        if address == size == 0:
            break
        reservations.append((address, size))
        offset += 16
    assert reservations == [(0x80000000, 0x80000)], (
        f"Unexpected boot firmware reservations: {reservations}"
    )

    def get(node, prop, kind="s"):
        result = subprocess.run(
            ["fdtget", "-t", kind, str(dtb), node, prop],
            text=True, capture_output=True,
        )
        return result.stdout.strip() if result.returncode == 0 else None

    def cells(node, prop):
        value = get(node, prop, "x")
        assert value is not None, f"Missing {node}/{prop}"
        return [int(x, 16) for x in value.split()]

    def children(node):
        return subprocess.check_output(
            ["fdtget", "-l", str(dtb), node], text=True,
        ).split()

    assert cells("/memory@80000000", "reg") == [
        0, 0x80000000, 0, dram_size - rtos_size,
    ], "Linux memory bank does not reclaim multimedia RAM"
    regions = children("/reserved-memory")
    assert len(regions) == 1, f"Unexpected reserved regions: {regions}"
    region = "/reserved-memory/" + regions[0]
    assert cells(region, "reg") == [
        0, 0x80000000 + dram_size - rtos_size, 0, rtos_size,
    ], "Incorrect remote-core reservation"
    assert get(region, "no-map") is not None, "Remote RAM must be no-map"
    assert cells("/remoteproc", "memory-region") == cells(region, "phandle")
    assert get("/remoteproc", "status") == "okay"
    assert get("/remoteproc", "firmware") == "cvirtos.elf"
    assert cells("/remoteproc", "resets")[-1] == 294
    assert get("/mailbox@1900000", "status") == "okay"

    forbidden = {
        "cvitek,vi", "cvitek,vip", "cvitek,vpss", "cvitek,vo",
        "cvitek,mipi_tx", "cvitek,mipi-rx", "cvitek,cif", "cvitek,fb",
        "cvitek,ive", "cvitek,dwa", "cvitek,rgn", "cvitek,asic-vcodec",
        "cvitek,jpeg", "cvitek,cvi_vc_drv", "cvitek,tpu",
        "cvitek,cvitek-ion", "ion-region", "cvitek,rtos_image",
        "cvitek,rtos_cmdqu",
    }

    def walk(node, enabled=True):
        enabled = enabled and get(node, "status") in (None, "okay", "ok")
        compatible = set((get(node, "compatible") or "").split())
        assert not (enabled and compatible & forbidden), (
            f"Enabled multimedia/legacy RTOS node: {node} {compatible}"
        )
        for name in children(node):
            walk(node.rstrip("/") + "/" + name, enabled)

    walk("/")


if __name__ == "__main__":
    verify(sys.argv[1], int(sys.argv[2], 0), int(sys.argv[3], 0))
    print("Headless DTB and remoteproc memory checks passed")
