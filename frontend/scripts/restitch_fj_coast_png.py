# -*- coding: utf-8 -*-
"""用已缓存瓦片重新拼接 PNG（体积约几十 MB），无需重新下载。"""
from __future__ import annotations

import json
import math
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "public" / "datas" / "images"
TILE_CACHE = OUT_DIR / "_tiles" / "eox"
MAX_TILES = 420

SCENES = [
	{"id": "typhoon-2024", "name": "闽东沿海高清（宁德—福州）", "file": "fj_coast_typhoon_ningde_fuzhou.png", "bbox": (119.2, 25.8, 120.4, 27.0), "z": 12, "region": "闽东沿海"},
	{"id": "storm-2023", "name": "闽南沿海高清（泉州—漳州）", "file": "fj_coast_storm_quanzhou_zhangzhou.png", "bbox": (117.5, 23.7, 119.0, 25.1), "z": 12, "region": "闽南沿海"},
	{"id": "surge-2025", "name": "福建沿海广域高清", "file": "fj_coast_wide_fujian.png", "bbox": (117.5, 23.7, 120.3, 27.1), "z": 11, "region": "全省沿海"},
	{"id": "sar-wind-2024", "name": "闽江口—平潭高清", "file": "fj_coast_minjiang_pingtan.png", "bbox": (119.0, 25.2, 120.2, 26.4), "z": 12, "region": "闽江口—平潭"},
	{"id": "sar-wave-2024", "name": "平潭—莆田高清", "file": "fj_coast_pingtan_putian.png", "bbox": (118.8, 24.7, 120.1, 25.9), "z": 12, "region": "平潭—莆田"},
]


def lonlat_to_tile(lon, lat, z):
	lat = max(min(lat, 85.05112878), -85.05112878)
	n = 2**z
	x = int((lon + 180.0) / 360.0 * n)
	y = int((1.0 - math.asinh(math.tan(math.radians(lat))) / math.pi) / 2.0 * n)
	return x, y


def choose_zoom(west, south, east, north, preferred_z):
	z = preferred_z
	while z >= 8:
		x0, y1 = lonlat_to_tile(west, south, z)
		x1, y0 = lonlat_to_tile(east, north, z)
		xs = list(range(min(x0, x1), max(x0, x1) + 1))
		ys = list(range(min(y0, y1), max(y0, y1) + 1))
		if len(xs) * len(ys) <= MAX_TILES:
			return z, xs, ys
		z -= 1
	x0, y1 = lonlat_to_tile(west, south, 8)
	x1, y0 = lonlat_to_tile(east, north, 8)
	return 8, list(range(min(x0, x1), max(x0, x1) + 1)), list(range(min(y0, y1), max(y0, y1) + 1))


def restitch(scene):
	west, south, east, north = scene["bbox"]
	z, xs, ys = choose_zoom(west, south, east, north, scene["z"])
	print(f"[{scene['id']}] stitch PNG z={z} {len(xs)}x{len(ys)}")
	# 探测瓦片尺寸
	probe = None
	for y in ys:
		for x in xs:
			p = TILE_CACHE / str(z) / f"{x}_{y}.jpg"
			if p.exists() and p.stat().st_size > 800:
				probe = Image.open(p)
				break
		if probe:
			break
	if not probe:
		raise RuntimeError(f"no cached tiles for {scene['id']} z={z}")
	tw, th = probe.size
	probe.close()
	canvas = Image.new("RGB", (tw * len(xs), th * len(ys)), (12, 28, 40))
	missing = 0
	for yi, y in enumerate(ys):
		for xi, x in enumerate(xs):
			p = TILE_CACHE / str(z) / f"{x}_{y}.jpg"
			if not p.exists():
				missing += 1
				continue
			tile = Image.open(p).convert("RGB")
			canvas.paste(tile, (xi * tw, yi * th))
			tile.close()
	out = OUT_DIR / scene["file"]
	# 压缩级 3：体积与速度折中，仍可达几十 MB
	canvas.save(out, "PNG", optimize=True, compress_level=3)
	meta = {"z": z, "size": list(canvas.size), "bytes": out.stat().st_size, "missingTiles": missing}
	print(f"  saved {out.name} {meta['size'][0]}x{meta['size'][1]} ({meta['bytes']/1e6:.1f} MB) missing={missing}")
	canvas.close()
	return {
		"id": scene["id"],
		"name": scene["name"],
		"region": scene["region"],
		"date": "2016-2017 Sentinel-2 cloudless mosaic",
		"file": scene["file"],
		"bbox": list(scene["bbox"]),
		"downloadOk": True,
		"meta": meta,
		"source": {
			"provider": "EOX Sentinel-2 cloudless",
			"layer": "s2cloudless_3857",
			"license": "CC BY 4.0",
			"attribution": (
				"Sentinel-2 cloudless https://s2maps.eu by EOX IT Services GmbH "
				"(Contains modified Copernicus Sentinel data 2016 & 2017)"
			),
		},
	}


def main():
	items = [restitch(s) for s in SCENES]
	# 保留同名 jpg 作为轻量预览，manifest 指向 png 主文件
	manifest = {
		"note": (
			"高清 PNG 拼接影像（约几十 MB/张）。"
			"来源 EOX Sentinel-2 cloudless（CC BY 4.0，须署名）。"
			"同目录 jpg 为较低体积预览版。"
		),
		"basePath": "/datas/images/",
		"items": items,
	}
	(OUT_DIR / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
	print("manifest updated")


if __name__ == "__main__":
	main()
