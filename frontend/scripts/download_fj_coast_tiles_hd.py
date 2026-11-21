# -*- coding: utf-8 -*-
"""
高清版：更高缩放级别瓦片拼接，目标单张约十几～几十 MB。
数据源：EOX Sentinel-2 cloudless（CC BY 4.0，需署名）
"""
from __future__ import annotations

import json
import math
import time
import urllib.request
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "public" / "datas" / "images"
TILE_CACHE = OUT_DIR / "_tiles"

TILE_URL = (
	"https://tiles.maps.eox.at/wmts/1.0.0/s2cloudless_3857/"
	"default/GoogleMapsCompatible/{z}/{y}/{x}.jpg"
)
USER_AGENT = "disaster-survey-demo/1.0 (internal; open-tile download)"
# 单景最大瓦片数，防止全省范围失控
MAX_TILES = 420


def lonlat_to_tile(lon: float, lat: float, z: int) -> tuple[int, int]:
	lat = max(min(lat, 85.05112878), -85.05112878)
	n = 2**z
	x = int((lon + 180.0) / 360.0 * n)
	lat_rad = math.radians(lat)
	y = int((1.0 - math.asinh(math.tan(lat_rad)) / math.pi) / 2.0 * n)
	return x, y


def bbox_tiles(west: float, south: float, east: float, north: float, z: int):
	x0, y1 = lonlat_to_tile(west, south, z)
	x1, y0 = lonlat_to_tile(east, north, z)
	xs = list(range(min(x0, x1), max(x0, x1) + 1))
	ys = list(range(min(y0, y1), max(y0, y1) + 1))
	return xs, ys


def choose_zoom(west, south, east, north, preferred_z: int) -> tuple[int, list[int], list[int]]:
	z = preferred_z
	while z >= 8:
		xs, ys = bbox_tiles(west, south, east, north, z)
		if len(xs) * len(ys) <= MAX_TILES:
			return z, xs, ys
		z -= 1
	xs, ys = bbox_tiles(west, south, east, north, 8)
	return 8, xs, ys


def fetch_tile(url: str, dest: Path, retries: int = 4) -> bool:
	if dest.exists() and dest.stat().st_size > 800:
		return True
	dest.parent.mkdir(parents=True, exist_ok=True)
	req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
	for i in range(retries):
		try:
			with urllib.request.urlopen(req, timeout=90) as resp:
				data = resp.read()
			if len(data) < 500:
				# 个别缺失瓦片用深色占位，保证可拼接
				img = Image.new("RGB", (256, 256), (12, 28, 40))
				img.save(dest, "JPEG", quality=85)
				return True
			dest.write_bytes(data)
			return True
		except Exception as exc:
			if i == retries - 1:
				print(f"  FAIL {url} -> {exc}")
				img = Image.new("RGB", (256, 256), (12, 28, 40))
				img.save(dest, "JPEG", quality=85)
				return False
			time.sleep(1.5 * (i + 1))
	return False


def stitch(xs, ys, tile_paths, out_path: Path, quality: int = 95) -> tuple[bool, dict]:
	sample = None
	for key in tile_paths:
		sample = Image.open(tile_paths[key])
		break
	if sample is None:
		return False, {}
	tw, th = sample.size
	w, h = tw * len(xs), th * len(ys)
	# 超大图用临时文件拼，避免内存峰值过高
	canvas = Image.new("RGB", (w, h), (12, 28, 40))
	for yi, y in enumerate(ys):
		for xi, x in enumerate(xs):
			path = tile_paths.get((x, y))
			if not path or not path.exists():
				continue
			tile = Image.open(path).convert("RGB")
			canvas.paste(tile, (xi * tw, yi * th))
			tile.close()
	out_path.parent.mkdir(parents=True, exist_ok=True)
	canvas.save(out_path, "JPEG", quality=quality, optimize=True, progressive=True)
	meta = {"size": list(canvas.size), "bytes": out_path.stat().st_size, "quality": quality}
	canvas.close()
	print(f"  saved {out_path.name} {meta['size'][0]}x{meta['size'][1]} ({meta['bytes'] / 1e6:.1f} MB)")
	return True, meta


def download_scene(scene: dict) -> dict:
	west, south, east, north = scene["bbox"]
	z, xs, ys = choose_zoom(west, south, east, north, scene["z"])
	total = len(xs) * len(ys)
	print(f"[{scene['id']}] z={z} tiles={len(xs)}x{len(ys)}={total} -> ~{len(xs)*256}x{len(ys)*256}px")
	paths = {}
	ok = 0
	done = 0
	for y in ys:
		for x in xs:
			url = TILE_URL.format(z=z, x=x, y=y)
			dest = TILE_CACHE / "eox" / str(z) / f"{x}_{y}.jpg"
			if fetch_tile(url, dest):
				paths[(x, y)] = dest
				ok += 1
			done += 1
			if done % 40 == 0:
				print(f"  progress {done}/{total}")
			time.sleep(0.05)
	print(f"  fetched {ok}/{total}")
	out = OUT_DIR / scene["file"]
	success, meta = stitch(xs, ys, paths, out, quality=scene.get("quality", 95))
	return {
		"id": scene["id"],
		"name": scene["name"],
		"region": scene["region"],
		"date": "2016-2017 Sentinel-2 cloudless mosaic",
		"file": scene["file"],
		"bbox": list(scene["bbox"]),
		"downloadOk": success,
		"meta": {"z": z, "tileCount": total, "fetched": ok, **meta},
		"source": {
			"provider": "EOX Sentinel-2 cloudless",
			"layer": "s2cloudless_3857",
			"license": "CC BY 4.0",
			"attribution": (
				"Sentinel-2 cloudless https://s2maps.eu by EOX IT Services GmbH "
				"(Contains modified Copernicus Sentinel data 2016 & 2017)"
			),
			"urlTemplate": TILE_URL,
		},
	}


SCENES = [
	{
		"id": "typhoon-2024",
		"name": "闽东沿海高清（宁德—福州）",
		"file": "fj_coast_typhoon_ningde_fuzhou.jpg",
		"bbox": (119.2, 25.8, 120.4, 27.0),
		"z": 12,
		"region": "闽东沿海",
		"quality": 95,
	},
	{
		"id": "storm-2023",
		"name": "闽南沿海高清（泉州—漳州）",
		"file": "fj_coast_storm_quanzhou_zhangzhou.jpg",
		"bbox": (117.5, 23.7, 119.0, 25.1),
		"z": 12,
		"region": "闽南沿海",
		"quality": 95,
	},
	{
		"id": "surge-2025",
		"name": "福建沿海广域高清",
		"file": "fj_coast_wide_fujian.jpg",
		"bbox": (117.5, 23.7, 120.3, 27.1),
		"z": 11,
		"region": "全省沿海",
		"quality": 94,
	},
	{
		"id": "sar-wind-2024",
		"name": "闽江口—平潭高清",
		"file": "fj_coast_minjiang_pingtan.jpg",
		"bbox": (119.0, 25.2, 120.2, 26.4),
		"z": 12,
		"region": "闽江口—平潭",
		"quality": 95,
	},
	{
		"id": "sar-wave-2024",
		"name": "平潭—莆田高清",
		"file": "fj_coast_pingtan_putian.jpg",
		"bbox": (118.8, 24.7, 120.1, 25.9),
		"z": 12,
		"region": "平潭—莆田",
		"quality": 95,
	},
]


def main():
	OUT_DIR.mkdir(parents=True, exist_ok=True)
	# 删除旧的低分辨率 VIIRS 演示文件名（若存在）
	old = OUT_DIR / "fj_coast_pingtan_putian_viirs_20240917.jpg"
	if old.exists():
		old.unlink()

	items = []
	for scene in SCENES:
		items.append(download_scene(scene))

	manifest = {
		"note": (
			"高清瓦片拼接影像（约十几～几十 MB/张）。"
			"来源 EOX Sentinel-2 cloudless（CC BY 4.0，使用须署名）。"
			"为 2016/2017 无云镶嵌，非台风当日原片；正式灾后时相请走 Copernicus。"
		),
		"basePath": "/datas/images/",
		"items": items,
	}
	(OUT_DIR / "manifest.json").write_text(
		json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8"
	)
	print("done. manifest:", OUT_DIR / "manifest.json")
	for item in items:
		mb = (item.get("meta") or {}).get("bytes", 0) / 1e6
		print(f"  {item['file']}: {mb:.1f} MB")


if __name__ == "__main__":
	main()
