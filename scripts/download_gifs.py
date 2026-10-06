#!/usr/bin/env python3
import json, re, time, random
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
import requests
from PIL import Image

OUT=Path("app/src/main/res/drawable-nodpi"); ASSETS=Path("app/src/main/assets")
OUT.mkdir(parents=True,exist_ok=True); ASSETS.mkdir(parents=True,exist_ok=True)
for p in OUT.glob("gif*.gif"): p.unlink()
for p in OUT.glob("_tmp_*.gif"): p.unlink()

TARGET=5000; WIDTH=160; MAX_BYTES=320_000; PAGES_PER_QUERY=12
API="https://commons.wikimedia.org/w/api.php"
HEADERS={"User-Agent":"APP-GIF-Gallery/1.0 (GitHub Actions; https://github.com/wngrlhnn/APP)"}
queries=[
("funny","animated reaction gif"),("funny","funny animated gif"),("funny","comedy animated gif"),("funny","animated meme gif"),("funny","humor animation gif"),
("reactions","surprised reaction gif"),("reactions","laughing reaction gif"),("reactions","facepalm reaction gif"),("reactions","wow reaction gif"),("reactions","angry reaction gif"),("reactions","crying reaction gif"),
("animals","animated animal gif"),("animals","animated cat gif"),("animals","animated dog gif"),("animals","animated bird gif"),("animals","animated monkey gif"),("animals","animated fish gif"),
("effects","animated optical illusion gif"),("effects","animated abstract gif"),("effects","animated fire gif"),("effects","animated water gif"),("effects","animated geometric gif"),("effects","animated light gif"),("effects","animated explosion gif"),
("gaming","animated video game gif"),("gaming","pixel art game animation"),("gaming","arcade game animation"),("gaming","game character animation gif"),
("cartoon","animated cartoon gif"),("cartoon","animated comic gif"),("cartoon","animation character gif"),
("space","animated galaxy gif"),("space","animated stars gif"),("space","animated planet gif"),("space","animated moon gif"),
("people","animated human gif"),("people","animated walking gif"),("people","animated dancing gif"),("people","animated gesture gif"),
("objects","animated machine gif"),("objects","animated vehicle gif"),("objects","animated technology gif"),
("nature","animated nature gif"),("nature","animated plant gif"),("nature","animated flower gif"),("nature","animated ocean gif"),
("love","animated love gif"),("love","heart animation gif"),("greetings","hello animation gif"),("greetings","thank you animation gif")]
blocked=re.compile(r"flag|country.?flag|national.?flag|logo.?flag|ensign",re.I)

def request_json(params, retries=5):
    for attempt in range(retries):
        try:
            r=requests.get(API,params=params,headers=HEADERS,timeout=30)
            if r.status_code in (429,500,502,503,504):
                time.sleep(min(10,2**attempt)); continue
            r.raise_for_status()
            return r.json()
        except requests.RequestException:
            if attempt==retries-1: return None
            time.sleep(min(10,2**attempt))
    return None

def search(cat,term):
    params={"action":"query","generator":"search","gsrsearch":term,"gsrnamespace":6,"gsrlimit":50,
            "prop":"imageinfo","iiprop":"url|mime|size|extmetadata","iiurlwidth":WIDTH,"format":"json","formatversion":2}
    found=[]; offsets=set()
    for _ in range(PAGES_PER_QUERY):
        data=request_json(params)
        if not data: break
        for page in data.get("query",{}).get("pages",[]):
            title=page.get("title","")
            if blocked.search(title): continue
            info=(page.get("imageinfo") or [{}])[0]
            if info.get("mime")!="image/gif": continue
            url=info.get("thumburl") or info.get("url")
            if url: found.append({"url":url,"title":title,"category":cat})
        cont=data.get("continue")
        if not cont or cont.get("gsroffset") in offsets: break
        offsets.add(cont["gsroffset"]); params["gsroffset"]=cont["gsroffset"]
    return cat,found

def category_gifs(category="Animated GIF files"):
    params={"action":"query","list":"categorymembers","cmtitle":"Category:"+category,
            "cmnamespace":6,"cmlimit":500,"format":"json","formatversion":2}
    found=[]; seen_titles=set()
    for _ in range(80):
        data=request_json(params)
        if not data: break
        for page in data.get("query",{}).get("categorymembers",[]):
            title=page.get("title","")
            if title in seen_titles or blocked.search(title): continue
            seen_titles.add(title)
            found.append({"title":title,"category":"random","pageid":page.get("pageid")})
        cont=data.get("continue")
        if not cont: break
        params["cmcontinue"]=cont.get("cmcontinue")
        if not params["cmcontinue"]: break
    resolved=[]
    for i in range(0,len(found),50):
        ids=[str(x["pageid"]) for x in found[i:i+50] if x.get("pageid")]
        if not ids: continue
        p={"action":"query","pageids":"|".join(ids),"prop":"imageinfo",
           "iiprop":"url|mime|size|extmetadata","iiurlwidth":WIDTH,"format":"json","formatversion":2}
        data=request_json(p)
        if not data: continue
        for page in data.get("query",{}).get("pages",[]):
            info=(page.get("imageinfo") or [{}])[0]
            if info.get("mime")=="image/gif":
                url=info.get("thumburl") or info.get("url")
                if url: resolved.append({"url":url,"title":page.get("title",""),"category":"random"})
    return resolved

items=[]; seen=set(); titles=set()
cat_items=category_gifs()
random.shuffle(cat_items)
for item in cat_items:
    if item["url"] in seen or item["title"] in titles: continue
    seen.add(item["url"]); titles.add(item["title"]); items.append(item)

with ThreadPoolExecutor(max_workers=8) as pool:
    fs=[pool.submit(search,c,t) for c,t in queries]
    for f in as_completed(fs):
        cat,found=f.result()
        for item in found:
            if item["url"] in seen or item["title"] in titles: continue
            seen.add(item["url"]); titles.add(item["title"]); items.append(item)

random.shuffle(items)
print(f"Collected {len(items)} GIF candidates")

def download(job):
    n,item=job; raw=OUT/f"_tmp_{n:05d}.gif"; final=OUT/f"gif{n:04d}.gif"
    try:
        with requests.get(item["url"],headers=HEADERS,stream=True,timeout=45) as r:
            r.raise_for_status(); total=0
            with raw.open("wb") as f:
                for chunk in r.iter_content(65536):
                    total+=len(chunk)
                    if total>2_000_000: raise ValueError("source too large")
                    f.write(chunk)
        with Image.open(raw) as im:
            frames=[]; durations=[]
            for i in range(min(getattr(im,"n_frames",1),16)):
                im.seek(i); fr=im.convert("P",palette=Image.Palette.ADAPTIVE,colors=128)
                fr.thumbnail((WIDTH,WIDTH),Image.Resampling.LANCZOS); frames.append(fr.copy())
                durations.append(max(50,im.info.get("duration",80)))
            if not frames: raise ValueError("no frames")
            frames[0].save(final,save_all=True,append_images=frames[1:],duration=durations,loop=im.info.get("loop",0),optimize=True,disposal=2)
        if final.stat().st_size>MAX_BYTES:
            final.unlink(missing_ok=True)
            with Image.open(raw) as im:
                frames=[]; durations=[]
                for i in range(min(getattr(im,"n_frames",1),12)):
                    im.seek(i); fr=im.convert("P",palette=Image.Palette.ADAPTIVE,colors=96)
                    fr.thumbnail((128,128),Image.Resampling.LANCZOS); frames.append(fr.copy())
                    durations.append(max(50,im.info.get("duration",80)))
                if frames:
                    frames[0].save(final,save_all=True,append_images=frames[1:],duration=durations,loop=im.info.get("loop",0),optimize=True,disposal=2)
            if not final.exists() or final.stat().st_size>MAX_BYTES:
                final.unlink(missing_ok=True); raw.unlink(missing_ok=True); return None
        raw.unlink(missing_ok=True)
        return {**item,"file":final.name,"size":final.stat().st_size}
    except Exception:
        raw.unlink(missing_ok=True); final.unlink(missing_ok=True); return None

manifest=[]
with ThreadPoolExecutor(max_workers=12) as pool:
    fs=[pool.submit(download,(n,item)) for n,item in enumerate(items,1)]
    for i,f in enumerate(as_completed(fs),1):
        x=f.result()
        if x: manifest.append(x)
        if i%500==0: print(f"processed {i}/{len(fs)}, usable={len(manifest)}")
        if len(manifest)>=TARGET: break

if len(manifest)<TARGET:
    raise SystemExit(f"Only {len(manifest)} usable GIFs; need {TARGET} real assets.")
manifest=manifest[:TARGET]; manifest.sort(key=lambda x:x["file"])
(ASSETS/"manifest.json").write_text(json.dumps(manifest,ensure_ascii=False,separators=(",",":")),encoding="utf-8")
print(f"Final offline GIF count: {len(manifest)}")
