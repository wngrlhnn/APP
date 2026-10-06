#!/usr/bin/env python3
import hashlib, json, os, re, time
from pathlib import Path
import requests
from PIL import Image

OUT = Path("app/src/main/res/drawable-nodpi")
ASSETS = Path("app/src/main/assets")
OUT.mkdir(parents=True, exist_ok=True)
ASSETS.mkdir(parents=True, exist_ok=True)
for p in OUT.glob("gif*.gif"):
    p.unlink()

TARGET = 5000
WIDTH = 128
MAX_BYTES = 300_000
API = "https://commons.wikimedia.org/w/api.php"

queries = [
    ("funny", "animated reaction gif"),
    ("funny", "funny animated gif"),
    ("funny", "comedy animated gif"),
    ("reactions", "surprised reaction gif"),
    ("reactions", "laughing reaction gif"),
    ("reactions", "facepalm reaction gif"),
    ("reactions", "wow reaction gif"),
    ("funny", "funny animated gif"),
    ("funny", "comedy animated gif"),
    ("reactions", "surprised reaction gif"),
    ("reactions", "laughing reaction gif"),
    ("reactions", "facepalm reaction gif"),
    ("reactions", "wow reaction gif"),
    ("funny", "animated cartoon gif"),
    ("reactions", "animated reaction"),
    ("reactions", "animated smiley"),
    ("animals", "animated animal gif"),
    ("animals", "animated cat gif"),
    ("animals", "animated dog gif"),
    ("animals", "animated bird gif"),
    ("animals", "animated insect gif"),
    ("effects", "animated optical illusion gif"),
    ("effects", "animated abstract gif"),
    ("effects", "animated fractal gif"),
    ("effects", "animated fire gif"),
    ("effects", "animated water gif"),
    ("effects", "animated space gif"),
    ("effects", "animated geometric gif"),
    ("gaming", "animated video game gif"),
    ("gaming", "pixel art game animation"),
    ("gaming", "arcade game animation"),
    ("cartoon", "animated cartoon gif"),
    ("cartoon", "animated comic gif"),
    ("space", "animated galaxy gif"),
    ("space", "animated stars gif"),
    ("space", "animated planet gif"),
    ("gaming", "pixel art game animation"),
    ("gaming", "arcade game animation"),
    ("cartoon", "animated cartoon gif"),
    ("cartoon", "animated comic gif"),
    ("space", "animated galaxy gif"),
    ("space", "animated stars gif"),
    ("space", "animated planet gif"),
    ("people", "animated human gif"),
    ("people", "animated walking gif"),
    ("people", "animated dancing gif"),
    ("objects", "animated machine gif"),
    ("objects", "animated vehicle gif"),
    ("nature", "animated nature gif"),
    ("nature", "animated plant gif"),
    ("funny", "animated meme gif"),
]

blocked = re.compile(r"flag|country.?flag|national.?flag|logo.?flag|ensign", re.I)
seen = set()
seen_titles = set()
items = []

def get_results(term, category, cont=None):
    params = {
        "action": "query", "generator": "search", "gsrsearch": term,
        "gsrnamespace": 6, "gsrlimit": 50,
        "prop": "imageinfo", "iiprop": "url|mime|size|extmetadata",
        "iiurlwidth": WIDTH, "format": "json", "formatversion": 2,
    }
    if cont:
        params.update(cont)
    r = requests.get(API, params=params, timeout=30)
    r.raise_for_status()
    return r.json()

for category, term in queries:
    cont = None
    while len(items) < TARGET:
        data = get_results(term, category, cont)
        pages = data.get("query", {}).get("pages", [])
        for page in pages:
            title = page.get("title", "")
            if blocked.search(title):
                continue
            info = (page.get("imageinfo") or [{}])[0]
            if info.get("mime") != "image/gif":
                continue
            url = info.get("thumburl") or info.get("url")
            if not url or url in seen or title in seen_titles:
                continue
            seen.add(url)
            seen_titles.add(title)
            items.append({"url": url, "title": title, "category": category})
            if len(items) >= TARGET:
                break
        cont = data.get("continue")
        if not cont or not pages:
            break
    if len(items) >= TARGET:
        break
    print(f"{len(items)}/{TARGET} candidates after {term}")

session = requests.Session()
manifest = []
for n, item in enumerate(items[:TARGET], 1):
    raw = OUT / f"_tmp_{n:04d}.gif"
    final = OUT / f"gif{n:04d}.gif"
    try:
        with session.get(item["url"], stream=True, timeout=45) as r:
            r.raise_for_status()
            total = 0
            with raw.open("wb") as f:
                for chunk in r.iter_content(64 * 1024):
                    total += len(chunk)
                    if total > MAX_BYTES:
                        raise ValueError("source thumbnail too large")
                    f.write(chunk)
        with Image.open(raw) as im:
            frames = []
            durations = []
            for i in range(min(getattr(im, "n_frames", 1), 16)):
                im.seek(i)
                frame = im.convert("P", palette=Image.Palette.ADAPTIVE, colors=96)
                frame.thumbnail((WIDTH, WIDTH), Image.Resampling.LANCZOS)
                frames.append(frame.copy())
                durations.append(max(40, im.info.get("duration", 80)))
            if not frames:
                raise ValueError("no frames")
            frames[0].save(final, save_all=True, append_images=frames[1:],
                           duration=durations, loop=im.info.get("loop", 0),
                           optimize=True, disposal=2)
        raw.unlink(missing_ok=True)
        if final.stat().st_size > MAX_BYTES:
            final.unlink(missing_ok=True)
            continue
        manifest.append({**item, "file": final.name, "size": final.stat().st_size})
    except Exception:
        raw.unlink(missing_ok=True)
        final.unlink(missing_ok=True)
    if n % 100 == 0:
        print(f"processed {n}/{len(items)}, usable={len(manifest)}")

if len(manifest) < TARGET:
    raise SystemExit(f"Only {len(manifest)} usable GIFs; refusing to fabricate the remaining assets.")

(ASSETS / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
print(f"Final offline GIF count: {len(manifest)}")
