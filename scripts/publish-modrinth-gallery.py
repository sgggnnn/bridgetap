"""Upload reviewed screenshots to a Modrinth gallery (PROJECT_WRITE required)."""
import argparse,hashlib,json,os,re,urllib.parse,urllib.request,urllib.error
from pathlib import Path

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument("--project",required=True);parser.add_argument("--dry-run",action="store_true");args=parser.parse_args()
    if not re.fullmatch(r"[A-Za-z0-9]{8}",args.project):raise SystemExit("Use the exact Modrinth project ID")
    token=os.environ.get("MODRINTH_TOKEN","").strip()
    if not token and not args.dry_run:raise SystemExit("Missing MODRINTH_TOKEN")
    root=Path("docs/screenshots");manifest=json.loads((root/"gallery.json").read_text(encoding="utf-8"))
    published=root/"published.json";receipts=json.loads(published.read_text(encoding="utf-8")) if published.exists() else []
    output=Path("build/modrinth-gallery-upload.json");output.parent.mkdir(parents=True,exist_ok=True)
    try:
        for entry in manifest:
            name=entry["file"]
            if Path(name).name!=name or not name.endswith(".jpg"):raise ValueError("Invalid gallery filename")
            data=(root/name).read_bytes();digest=hashlib.sha256(data).hexdigest()
            if digest!=entry["sha256"] or not data.startswith(b"\xff\xd8\xff") or len(data)>5*1024*1024:raise ValueError("Screenshot checksum/format/size mismatch: "+name)
            if any(r["project"]==args.project and r["sha256"]==digest for r in receipts):print("SKIP existing:",name,flush=True);continue
            print("PLAN" if args.dry_run else "UPLOAD",entry["title"],flush=True)
            if args.dry_run:continue
            query=urllib.parse.urlencode({"ext":"jpg","featured":str(entry["featured"]).lower(),"title":entry["title"],"description":entry["description"],"ordering":entry["ordering"]})
            request=urllib.request.Request("https://api.modrinth.com/v2/project/"+args.project+"/gallery?"+query,data=data,headers={"Authorization":token,"User-Agent":"BridgeTap-gallery/1.0 (github.com/sgggnnn/bridgetap)","Content-Type":"image/jpeg"},method="POST")
            try:
                with urllib.request.urlopen(request,timeout=90) as response:
                    if response.status!=204:raise RuntimeError("Unexpected gallery response: "+str(response.status))
            except urllib.error.HTTPError as error:
                detail=error.read(2048).decode("utf-8",errors="replace").replace(token,"[redacted]")
                raise RuntimeError("Gallery upload failed (HTTP "+str(error.code)+"). The token needs PROJECT_WRITE. "+detail[:300]) from None
            receipts.append({"project":args.project,"file":name,"title":entry["title"],"sha256":digest,"featured":entry["featured"],"status":204})
            output.write_text(json.dumps(receipts,indent=2)+"\n",encoding="utf-8");print("ACCEPTED",name,flush=True)
    finally:output.write_text(json.dumps(receipts,indent=2)+"\n",encoding="utf-8")
    print("COMPLETE:",len(receipts),"gallery uploads recorded",flush=True)
if __name__=="__main__":main()
