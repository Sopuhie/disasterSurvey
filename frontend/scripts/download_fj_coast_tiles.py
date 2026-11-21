# -*- coding: utf-8 -*-
"""
从开放瓦片服务下载福建沿海影像并拼接为 JPG。

数据源：EOX Sentinel-2 cloudless（2016/2017，CC BY 4.0）
服务：https://tiles.maps.eox.at
署名：Sentinel-2 cloudless by EOX IT Services GmbH
      (Contains modified Copernicus Sentinel data 2016 & 2017)

说明：无云镶嵌非某次台风当日快照；正式灾后时相请改用 Copernicus 按日期下载。
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

# EOX 2016/17 层为 CC BY，可再分发（需署名）
TILE_URL = (
	"https://tiles.maps.eox.at/wmts/1.0.0/s2cloudless_3857/"
	"default/GoogleMapsCompatible/{z}/{y}/{x}.jpg"
)

# NASA GIBS 真彩色（公开可用，分辨率较低，可提供“灾后日期”观感）
GIBS_URL = (
	"https://gibs.earthdata.nasa.gov/wmts/epsg3857/best/"
	"VIIRS_SNPP_CorrectedReflectance_TrueColor/default/{date}/"
	"GoogleMapsCompatible_Level9/{z}/{y}/{x}.jpg"
)

USER_AGENT = "disaster-survey-demo/1.0 (internal; open-tile download)"


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
	xs = range(min(x0, x1), max(x0, x1) + 1)
	ys = range(min(y0, y1), max(y0, y1) + 1)
	return list(xs), list(ys)


def fetch_tile(url: str, dest: Path, retries: int = 3) -> bool:
	if dest.exists() and dest.stat().st_size > 800:
		return True
	dest.parent.mkdir(parents=True, exist_ok=True)
	req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
	for i in range(retries):
		try:
			with urllib.request.urlopen(req, timeout=60) as resp:
				data = resp.read()
			if len(data) < 500:
				raise RuntimeError(f"tile too small: {len(data)} bytes")
			dest.write_bytes(data)
			return True
		except Exception as exc:
			if i == retries - 1:
				print(f"  FAIL {url} -> {exc}")
				return False
			time.sleep(1.2 * (i + 1))
	return False


def stitch(xs: list[int], ys: list[int], tile_paths: dict[tuple[int, int], Path], out_path: Path) -> bool:
	sample = None
	for key in tile_paths:
		sample = Image.open(tile_paths[key])
		break
	if sample is None:
		return False
	tw, th = sample.size
	canvas = Image.new("RGB", (tw * len(xs), th * len(ys)), (20, 40, 60))
	for yi, y in enumerate(ys):
		for xi, x in enumerate(xs):
			path = tile_paths.get((x, y))
			if not path or not path.exists():
				continue
			tile = Image.open(path).convert("RGB")
			canvas.paste(tile, (xi * tw, yi * th))
	out_path.parent.mkdir(parents=True, exist_ok=True)
	canvas.save(out_path, "JPEG", quality=90, optimize=True)
	print(f"  saved {out_path.name} ({canvas.size[0]}x{canvas.size[1]})")
	return True


def download_eox_scene(scene_id: str, bbox: tuple[float, float, float, float], z: int, filename: str):
	west, south, east, north = bbox
	xs, ys = bbox_tiles(west, south, east, north, z)
	# 控制单景瓦片规模，避免过大
	if len(xs) * len(ys) > 64:
		z = max(z - 1, 8)
		xs, ys = bbox_tiles(west, south, east, north, z)
	print(f"[{scene_id}] EOX z={z} tiles={len(xs)}x{len(ys)} x={xs[0]}..{xs[-1]} y={ys[0]}..{ys[-1]}")
	paths = {}
	ok = 0
	for y in ys:
		for x in xs:
			url = TILE_URL.format(z=z, x=x, y=y)
			dest = TILE_CACHE / "eox" / str(z) / f"{x}_{y}.jpg"
			if fetch_tile(url, dest):
				paths[(x, y)] = dest
				ok += 1
			time.sleep(0.08)
	print(f"  fetched {ok}/{len(xs) * len(ys)}")
	out = OUT_DIR / filename
	return stitch(xs, ys, paths, out), {
		"z": z,
		"tileCount": len(xs) * len(ys),
		"fetched": ok,
		"size": list(Image.open(out).size) if out.exists() else None,
	}


def download_gibs_scene(scene_id: str, bbox: tuple[float, float, float, float], date: str, filename: str, z: int = 8):
	west, south, east, north = bbox
	z = min(z, 9)
	xs, ys = bbox_tiles(west, south, east, north, z)
	print(f"[{scene_id}] GIBS {date} z={z} tiles={len(xs)}x{len(ys)}")
	paths = {}
	ok = 0
	for y in ys:
		for x in xs:
			url = GIBS_URL.format(date=date, z=z, x=x, y=y)
			dest = TILE_CACHE / "gibs" / date / str(z) / f"{x}_{y}.jpg"
			if fetch_tile(url, dest):
				paths[(x, y)] = dest
				ok += 1
			time.sleep(0.08)
	print(f"  fetched {ok}/{len(xs) * len(ys)}")
	out = OUT_DIR / filename
	return stitch(xs, ys, paths, out), {
		"z": z,
		"date": date,
		"tileCount": len(xs) * len(ys),
		"fetched": ok,
		"size": list(Image.open(out).size) if out.exists() else None,
	}


SCENES = [
	{
		"id": "typhoon-2024",
		"name": "闽东沿海（宁德—福州）Sentinel-2 无云镶嵌",
		"file": "fj_coast_typhoon_ningde_fuzhou.jpg",
		"bbox": (119.2, 25.8, 120.4, 27.0),
		"z": 10,
		"mode": "eox",
		"region": "闽东沿海",
		"date": "2016-2017 mosaic",
	},
	{
		"id": "storm-2023",
		"name": "闽南沿海（泉州—漳州）Sentinel-2 无云镶嵌",
		"file": "fj_coast_storm_quanzhou_zhangzhou.jpg",
		"bbox": (117.5, 23.7, 119.0, 25.1),
		"z": 10,
		"mode": "eox",
		"region": "闽南沿海",
		"date": "2016-2017 mosaic",
	},
	{
		"id": "surge-2025",
		"name": "福建全省沿海示意 Sentinel-2 无云镶嵌",
		"file": "fj_coast_wide_fujian.jpg",
		"bbox": (117.4, 23.6, 120.4, 27.2),
		"z": 9,
		"mode": "eox",
		"region": "全省沿海",
		"date": "2016-2017 mosaic",
	},
	{
		"id": "sar-wind-2024",
		"name": "闽江口—平潭近海 Sentinel-2 无云镶嵌",
		"file": "fj_coast_minjiang_pingtan.jpg",
		"bbox": (119.0, 25.2, 120.2, 26.4),
		"z": 10,
		"mode": "eox",
		"region": "闽江口—平潭",
		"date": "2016-2017 mosaic",
	},
	{
		"id": "sar-wave-2024",
		"name": "平潭—莆田近海 + 台风季 VIIRS 真彩色（2024-09-17）",
		"file": "fj_coast_pingtan_putian_viirs_20240917.jpg",
		"bbox": (118.6, 24.5, 120.3, 26.0),
		"z": 9,
		"mode": "gibs",
		"gibsDate": "2024-09-17",
		"region": "平潭—莆田",
		"date": "2024-09-17",
	},
]


def main():
	OUT_DIR.mkdir(parents=True, exist_ok=True)
	# 清理旧 SVG 占位，避免与真实影像混淆
	for old in OUT_DIR.glob("*_demo.svg"):
		old.unlink(missing_ok=True)

	items = []
	for scene in SCENES:
		if scene["mode"] == "eox":
			ok, meta = download_eox_scene(scene["id"], scene["bbox"], scene["z"], scene["file"])
			source = {
				"provider": "EOX Sentinel-2 cloudless",
				"layer": "s2cloudless_3857",
				"license": "CC BY 4.0",
				"attribution": (
					"Sentinel-2 cloudless https://s2maps.eu by EOX IT Services GmbH "
					"(Contains modified Copernicus Sentinel data 2016 & 2017)"
				),
				"urlTemplate": TILE_URL,
			}
		else:
			ok, meta = download_gibs_scene(
				scene["id"], scene["bbox"], scene["gibsDate"], scene["file"], scene["z"]
			)
			source = {
				"provider": "NASA GIBS VIIRS SNPP True Color",
				"license": "NASA Earthdata public imagery (acknowledge NASA GIBS)",
				"attribution": "Imagery courtesy NASA EOSDIS GIBS",
				"urlTemplate": GIBS_URL,
				"observationDate": scene["gibsDate"],
			}

		items.append(
			{
				"id": scene["id"],
				"name": scene["name"],
				"region": scene["region"],
				"date": scene["date"],
				"file": scene["file"],
				"bbox": list(scene["bbox"]),
				"downloadOk": ok,
				"meta": meta,
				"source": source,
			}
		)

	manifest = {
		"note": (
			"福建沿海开放影像瓦片拼接结果。"
			"EOX 层为 2016/2017 Sentinel-2 无云镶嵌（CC BY，需署名）；"
			"GIBS 为指定日期 VIIRS 真彩色（公开可用，分辨率较低）。"
			"非商用商业卫星灾后原片；正式台风当日快照请走 Copernicus / 业务订购渠道。"
		),
		"basePath": "/datas/images/",
		"items": items,
	}
	(OUT_DIR / "manifest.json").write_text(
		json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8"
	)
	print("manifest updated:", OUT_DIR / "manifest.json")


if __name__ == "__main__":
	main()
