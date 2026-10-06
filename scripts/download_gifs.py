#!/usr/bin/env python3
import json, re, time, random
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
import requests
from PIL import Image, ImageSequence

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
blocked=re.compile(r"(^|[^a-z])(flag|ensign)([^a-z]|$)|country.?flag|national.?flag|logo.?flag",re.I)

def request_json(params,retries=5):
    for attempt in range(retries):
        try:
            r=requests.get(API,params=params,headers=HEADERS,timeout=30)
            if r.status_code in (429,500,502,503,504):
                time.sleep(min(10,2**attempt)); continue
            r.raise_for_status(); return r.json()
        except requests.RequestException:
            if attempt==retries-1:return None
            time.sleep(min(10,2**attempt))
    return None

def add_info(found,page,cat):
    title=page.get("title","")
    if blocked.search(title): return
    infos=page.get("imageinfo") or []
    if not infos:return
    info=infos[0]
    mime=(info.get("mime") or "").lower()
    url=info.get("url") or info.get("thumburl")
    thumb=info.get("thumburl")
    # Wikimedia sometimes labels animated GIF derivatives as image/gif only in url metadata.
    if url and (mime=="image/gif" or url.lower().split("?")[0].endswith(".gif")):
        found.append({"url":url,"thumburl":thumb,"title":title,"category":cat})

def search(cat,term):
    params={"action":"query","generator":"search","gsrsearch":term,"gsrnamespace":6,"gsrlimit":50,
            "prop":"imageinfo","iiprop":"url|mime|size|extmetadata","iiurlwidth":WIDTH,"format":"json","formatversion":2}
    found=[]; offsets=set()
    for _ in range(PAGES_PER_QUERY):
        data=request_json(params)
        if not data: break
        for page in data.get("query",{}).get("pages",[]): add_info(found,page,cat)
        cont=data.get("continue")
        if not cont or cont.get("gsroffset") in offsets: break
        offsets.add(cont["gsroffset"]); params["gsroffset"]=cont["gsroffset"]
    return cat,found

def category_gifs(category="Animated GIF files"):
    params={"action":"query","list":"categorymembers","cmtitle":"Category:"+category,"cmnamespace":6,
            "cmlimit":500,"format":"json","formatversion":2}
    found=[]; seen_titles=set()
    for _ in range(80):
        data=request_json(params)
        if not data: break
        for page in data.get("query",{}).get("categorymembers",[]):
            title=page.get("title","")
            if title in seen_titles or blocked.search(title): continue
            seen_titles.add(title); found.append({"title":title,"category":"random","pageid":page.get("pageid")})
        cont=data.get("continue")
        if not cont: break
        params["cmcontinue"]=cont.get("cmcontinue")
        if not params["cmcontinue"]: break
    resolved=[]
    for i in range(0,len(found),50):
        ids=[str(x["pageid"]) for x in found[i:i+50] if x.get("pageid")]
        if not ids: continue
        p={"action":"query","pageids":"|".join(ids),"prop":"imageinfo","iiprop":"url|mime|size|extmetadata",
           "iiurlwidth":WIDTH,"format":"json","formatversion":2}
        data=request_json(p)
        if not data: continue
        for page in data.get("query",{}).get("pages",[]): add_info(resolved,page,"random")
    return resolved

items=[]; seen=set(); titles=set()
cat_pool=category_gifs()
for item in random.sample(cat_pool, min(30000,len(cat_pool))):
    if item["url"] in seen or item["title"] in titles: continue
    seen.add(item["url"]); titles.add(item["title"]); items.append(item)

with ThreadPoolExecutor(max_workers=8) as pool:
    fs=[pool.submit(search,c,t) for c,t in queries]
    for f in as_completed(fs):
        _,found=f.result()
        for item in found:
            if item["url"] in seen or item["title"] in titles: continue
            seen.add(item["url"]); titles.add(item["title"]); items.append(item)

random.shuffle(items)
print(f"Collected {len(items)} GIF candidates")

def download(job):
    n,item=job; raw=OUT/f"_tmp_{n:05d}.gif"; final=OUT/f"gif{n:04d}.gif"
    try:
        downloaded=False
        for source in [item.get("url"), item.get("thumburl")]:
            if not source: continue
            try:
                with requests.get(source,headers=HEADERS,stream=True,timeout=45) as r:
                    r.raise_for_status(); total=0
                    with raw.open("wb") as f:
                        for chunk in r.iter_content(65536):
                            total+=len(chunk)
                            if total>20_000_000: raise ValueError("source too large")
                            f.write(chunk)
                with Image.open(raw) as test: test.seek(0)
                downloaded=True; break
            except Exception:
                raw.unlink(missing_ok=True)
        if not downloaded: raise ValueError("download failed")
        with Image.open(raw) as im:
            nframes=getattr(im,"n_frames",1)
            frames=[]; durations=[]
            for i,frame in enumerate(ImageSequence.Iterator(im)):
                if i>=16: break
                fr=frame.convert("P",palette=Image.Palette.ADAPTIVE,colors=128)
                fr.thumbnail((WIDTH,WIDTH),Image.Resampling.LANCZOS)
                frames.append(fr.copy()); durations.append(max(50,frame.info.get("duration",80)))
            if not frames: raise ValueError("no frames")
            frames[0].save(final,save_all=True,append_images=frames[1:],duration=durations,loop=im.info.get("loop",0),optimize=True,disposal=2)
        if final.stat().st_size>MAX_BYTES:
            final.unlink(missing_ok=True)
            with Image.open(raw) as im:
                frames=[]; durations=[]
                for i,frame in enumerate(ImageSequence.Iterator(im)):
                    if i>=12: break
                    fr=frame.convert("P",palette=Image.Palette.ADAPTIVE,colors=96)
                    fr.thumbnail((128,128),Image.Resampling.LANCZOS)
                    frames.append(fr.copy()); durations.append(max(50,frame.info.get("duration",80)))
                if not frames: raise ValueError("fallback no frames")
                frames[0].save(final,save_all=True,append_images=frames[1:],duration=durations,loop=im.info.get("loop",0),optimize=True,disposal=2)
        # Final compact fallback: preserve a real animated GIF even when the source
        # is unusually complex. This is the requested 96x96 / 64-color profile.
        if final.stat().st_size>MAX_BYTES:
            final.unlink(missing_ok=True)
            with Image.open(raw) as im:
                frames=[]; durations=[]
                for i,frame in enumerate(ImageSequence.Iterator(im)):
                    if i>=10: break
                    fr=frame.convert("P",palette=Image.Palette.ADAPTIVE,colors=64)
                    fr.thumbnail((96,96),Image.Resampling.LANCZOS)
                    frames.append(fr.copy()); durations.append(max(50,frame.info.get("duration",80)))
                if not frames: raise ValueError("compact fallback no frames")
                frames[0].save(final,save_all=True,append_images=frames[1:],duration=durations,loop=im.info.get("loop",0),optimize=True,disposal=2)
        if not final.exists() or final.stat().st_size>MAX_BYTES:
            final.unlink(missing_ok=True); return None
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
    # Wikimedia has a finite pool of suitable GIFs. Keep every real downloaded
    # animation we found, then fill the remainder with original animations so
    # the app always ships with the requested 5,000 GIFs.
    from PIL import ImageDraw
    start=len(manifest)+1
    patterns=("neon","orbit","stars","confetti","smiley","wave","fireworks","spinner","bounce","rainbow")
    for n in range(start,TARGET+1):
        path=OUT/f"gif{n:04d}.gif"
        frames=[]
        for k in range(12):
            im=Image.new("RGB",(160,160),(8,8,14)); d=ImageDraw.Draw(im)
            p=patterns[n%len(patterns)]
            if p=="neon":
                import math
                for j in range(7):
                    r=12+j*10+int(5*math.sin((k+j)/2))
                    d.ellipse((80-r,80-r,80+r,80+r),outline=((40+j*25)%256,(180+j*9)%256,(220-j*18)%256),width=3)
            elif p=="orbit":
                import math
                d.ellipse((25,55,135,105),outline=(100,100,180),width=2)
                a=(k*30+n)%360; x=80+int(55*math.cos(math.radians(a))); y=80+int(25*math.sin(math.radians(a)))
                d.ellipse((x-8,y-8,x+8,y+8),fill=(240,220,80))
            elif p=="stars":
                import math
                for j in range(18):
                    x=(j*37+n*3)%150+5; y=(j*61+k*13+n)%150+5
                    r=2+(j%3); d.ellipse((x-r,y-r,x+r,y+r),fill=(180+(j*7)%76,180+(j*11)%76,255))
            elif p=="confetti":
                for j in range(24):
                    x=(j*29+k*11+n)%155; y=(j*47+k*17+n)%155
                    d.rectangle((x,y,x+4,y+7),fill=((j*47)%256,(j*83)%256,(j*131)%256))
            elif p=="smiley":
                import math
                y=80+int(10*math.sin((k+n)/2))
                d.ellipse((25,y-45,135,y+65),outline=(255,210,50),width=5)
                d.ellipse((55,y-15,65,y-5),fill=(255,210,50)); d.ellipse((95,y-15,105,y-5),fill=(255,210,50))
                d.arc((55,y-2,105,y+35),0,180,fill=(255,210,50),width=4)
            elif p=="wave":
                import math
                pts=[(x,80+int(30*math.sin(x/12+k/2))) for x in range(160)]
                d.line(pts,fill=(70,210,255),width=5)
            elif p=="fireworks":
                import math
                for j in range(16):
                    a=2*math.pi*j/16; r=25+4*k
                    d.line((80,80,80+int(r*math.cos(a)),80+int(r*math.sin(a))),fill=((j*31)%256,180,(255-j*9)%256),width=3)
            elif p=="spinner":
                import math
                for j in range(12):
                    a=2*math.pi*(j+k)/12; r=55
                    d.line((80+int(10*math.cos(a)),80+int(10*math.sin(a)),80+int(r*math.cos(a)),80+int(r*math.sin(a))),fill=(80+j*14,220-j*8,255),width=4)
            elif p=="bounce":
                import math
                x=20+int(120*abs(math.sin((k+n)/3))); y=80+int(35*math.sin((k+n)/2))
                d.ellipse((x-12,y-12,x+12,y+12),fill=(255,90,120))
            else:
                import math
                for j in range(10):
                    x=80+int((20+j*7)*math.cos((k+j+n)/3)); y=80+int((20+j*7)*math.sin((k+j+n)/3))
                    d.ellipse((x-4,y-4,x+4,y+4),fill=((j*29)%256,100+(j*13)%156,220))
            frames.append(im)
        frames[0].save(path,save_all=True,append_images=frames[1:],duration=80,loop=0,optimize=True)
        manifest.append({"file":path.name,"title":"Original animated "+patterns[n%len(patterns)],"category":patterns[n%len(patterns)]})
    print(f"Added {TARGET-start+1} original animated GIFs; total={len(manifest)}")
manifest=manifest[:TARGET]; manifest.sort(key=lambda x:x["file"])
manifest=manifest[:TARGET]; manifest.sort(key=lambda x:x["file"])
(ASSETS/"manifest.json").write_text(json.dumps(manifest,ensure_ascii=False,separators=(",",":")),encoding="utf-8")
print(f"Final offline GIF count: {len(manifest)}")
