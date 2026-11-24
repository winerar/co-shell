"""Theme inks for #493/#442 (owner poll 2026-11-23): deep palette ink, edge, tinted text — Oklab, WCAG audit.
Usage: python3 scripts/agent/themes_493_inks.py  (prints inks and contrasts; writes colors.json in cwd).
Change together with PaletteSpecs.kt when implementing (see design-system.md §«Цвета»)."""
import json, math
P = {
 "Лайм": dict(primary="B8FF2E", on="0E1F00", accL="7A2BD6", accD="C27BFF", inc="016D86", exp="C60B3D", bgD="050805"),
 "Гранат": dict(primary="FF3D6E", on="1F0009", accL="056E59", accD="2EE6BE", inc="036797", exp="AB3903", bgD="12060A"),
 "Банан": dict(primary="FFE135", on="2A2200", accL="4B3BCF", accD="9D8CFF", inc="027235", exp="C60B3D", bgD="0A0903"),
 "Квас": dict(primary="FFB020", on="2A1700", accL="036D73", accD="3DDBD9", inc="3B6E03", exp="C4063B", bgD="0D0904"),
 "Эспрессо": dict(primary="C67F4D", on="1F0C00", accL="1F5FA8", accD="4DA6FF", inc="006F47", exp="C0084C", bgD="110B08"),
 "Мате": dict(primary="B5D98A", on="1A2A08", accL="A1451A", accD="FFA77A", inc="016D67", exp="BE1140", bgD="0B0D08"),
 "Сакура": dict(primary="FFB3CF", on="3A0A1E", accL="0A7030", accD="8EE6A0", inc="00658A", exp="C0181A", bgD="120A0E"),
 "Лотос": dict(primary="CDB4FF", on="1E0A3A", accL="4C6902", accD="C7F25C", inc="05697F", exp="C3013A", bgD="0E0814"),
 "Василёк": dict(primary="5C8DFF", on="000B2A", accL="7A5600", accD="FFCC4D", inc="01712F", exp="C0084C", bgD="060912"),
}
def h2r(h): return [int(h[i:i+2],16)/255 for i in (0,2,4)]
def r2h(c): return "%02X%02X%02X"%tuple(round(max(0,min(1,x))*255) for x in c)
def lin(x): return x/12.92 if x<=0.04045 else ((x+0.055)/1.055)**2.4
def gam(x): return 12.92*x if x<=0.0031308 else 1.055*x**(1/2.4)-0.055
def lum(h): r,g,b=[lin(x) for x in h2r(h)]; return 0.2126*r+0.7152*g+0.0722*b
def cr(a,b): la,lb=sorted([lum(a),lum(b)],reverse=True); return (la+0.05)/(lb+0.05)
def to_ok(h):
    r,g,b=[lin(x) for x in h2r(h)]
    l=0.4122214708*r+0.5363325363*g+0.0514459929*b; m=0.2119034982*r+0.6806995451*g+0.1073969566*b; s=0.0883024619*r+0.2817188376*g+0.6299787005*b
    l,m,s=[x**(1/3) for x in (l,m,s)]
    return (0.2104542553*l+0.7936177850*m-0.0040720468*s, 1.9779984951*l-2.4285922050*m+0.4505937099*s, 0.0259040371*l+0.7827717662*m-0.8086757660*s)
def from_ok(L,a,b):
    l=(L+0.3963377774*a+0.2158037573*b)**3; m=(L-0.1055613458*a-0.0638541728*b)**3; s=(L-0.0894841775*a-1.2914855480*b)**3
    rgb=(4.0767416621*l-3.3077115913*m+0.2309699292*s, -1.2684380046*l+2.6097574011*m-0.3413193965*s, -0.0041960863*l-0.7034186147*m+1.7076147010*s)
    return rgb
def ingamut(rgb): return all(-1e-4<=x<=1+1e-4 for x in rgb)
def okh(L,C,H):
    a,b=C*math.cos(H),C*math.sin(H); rgb=from_ok(L,a,b)
    while not ingamut(rgb) and C>0: C-=0.002; a,b=C*math.cos(H),C*math.sin(H); rgb=from_ok(L,a,b)
    return r2h([gam(max(0,min(1,x))) for x in rgb])
def mix(a,b,t):  # oklab lerp like Compose
    A,B=to_ok(a),to_ok(b); L=[A[i]+(B[i]-A[i])*t for i in range(3)]
    return r2h([gam(max(0,min(1,x))) for x in from_ok(*L)])
def deep(h, bg, target):
    L,a,b=to_ok(h); C=math.hypot(a,b); H=math.atan2(b,a)
    for i in range(1000):
        Lx=L-i*0.002; c=okh(Lx,C,H)
        if cr(c,bg)>=target: return c
def boost(h, dC=0.03):
    L,a,b=to_ok(h); C=math.hypot(a,b); H=math.atan2(b,a); return okh(L,C+dC,H)
out={}
for n,p in P.items():
    pr=p["primary"]; surf=mix("FFFFFF",pr,0.16)
    ink=deep(pr,surf,4.6)          # headings, selected tab label, focus
    edge=deep(pr,"FFFFFF",3.0)     # hairline around neon fills
    text=mix(ink,"000000",0.55)    # body text: tinted near-black
    muted=mix(ink,"000000",0.22)
    accSoft=mix("FFFFFF",p["accL"],0.14); accFill=mix("FFFFFF",p["accL"],0.24)
    o=dict(p, surf=surf, ink=ink, edge=edge, text=text, muted=muted, neon=boost(pr),
           cap=mix("FFFFFF",pr,0.30), heroA=mix("FFFFFF",pr,0.55), heroB=mix(pr,p["accL"],0.18),
           accSoft=accSoft, accFill=accFill, accDsoft=mix(p["bgD"],p["accD"],0.22))
    o["audit"]={"ink/surf":round(cr(ink,surf),2),"ink/white":round(cr(ink,"FFFFFF"),2),"ink/cap":round(cr(ink,o["cap"]),2),
       "edge/white":round(cr(edge,"FFFFFF"),2),"text/surf":round(cr(text,surf),2),"muted/surf":round(cr(muted,surf),2),
       "on/heroA":round(cr(p["on"],o["heroA"]),2),"on/heroB":round(cr(p["on"],o["heroB"]),2),"accL/accSoft":round(cr(p["accL"],accSoft),2),
       "accD/darkbg":round(cr(p["accD"],p["bgD"]),2),"neon/white":round(cr(o["neon"],"FFFFFF"),2),"prim/white":round(cr(pr,"FFFFFF"),2)}
    out[n]=o; print(n, ink, edge, o["neon"], o["audit"])
json.dump(out,open("themes_493_inks.json","w"),ensure_ascii=False,indent=1)
