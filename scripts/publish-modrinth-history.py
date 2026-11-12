"""Import original GitHub release JARs, preserving channel/notes and verified receipts.

Only VERSION_CREATE is needed for a private Modrinth project. Existing private
versions are tracked by receipts rather than requiring additional read scopes.
"""
import argparse, hashlib, io, json, os, re, subprocess, time, urllib.error, urllib.request, uuid, zipfile
from pathlib import Path
API="https://api.modrinth.com/v2/"
UA="BridgeTap-history-import/1.0 (github.com/sgggnnn/bridgetap)"

def releases(repo):
    pages=json.loads(subprocess.check_output(["gh","api","repos/"+repo+"/releases?per_page=100","--paginate","--slurp"]).decode())
    return [release for page in pages for release in page]

def order(release):
    match=re.fullmatch(r"v(\d+)\.(\d+)\.(\d+)(?:-dev\.(\d+))?",release["tag_name"])
    if not match:raise ValueError("Unsupported release tag: "+release["tag_name"])
    major,minor,patch,dev=match.groups()
    return int(major),int(minor),int(patch),dev is None,int(dev or 0)

def plan(items):
    result=[]
    for release in sorted((r for r in items if not r["draft"]),key=order):
        for asset in release["assets"]:
            name=asset["name"]
            if not name.endswith(".jar"):continue
            if not re.fullmatch(r"BridgeTap-[0-9A-Za-z.]+(?:-dev\.\d+)?(?:-Forge-1\.8\.9|-OneClient)?\.jar",name):raise ValueError("Unexpected JAR: "+name)
            edition="forge" if name.endswith("-Forge-1.8.9.jar") else "oneclient" if name.endswith("-OneClient.jar") else "ornithe"
            result.append((release,asset,edition))
    return result

def sha(data,algorithm="sha256"):return hashlib.new(algorithm,data).hexdigest()

def download(asset,folder):
    target=folder/asset["name"]
    digest=asset.get("digest","")
    if not digest.startswith("sha256:"):raise ValueError("Missing GitHub SHA-256 digest: "+asset["name"])
    expected=digest.split(":",1)[1]
    if not target.exists() or sha(target.read_bytes())!=expected:
        request=urllib.request.Request(asset["browser_download_url"],headers={"User-Agent":UA})
        with urllib.request.urlopen(request,timeout=180) as response:data=response.read()
        if len(data)!=asset["size"] or sha(data)!=expected:raise ValueError("GitHub asset checksum mismatch: "+asset["name"])
        target.write_bytes(data)
    data=target.read_bytes()
    if len(data)!=asset["size"]:raise ValueError("GitHub asset size mismatch")
    with zipfile.ZipFile(io.BytesIO(data)) as archive:
        if "fabric.mod.json" not in archive.namelist() and "mcmod.info" not in archive.namelist():raise ValueError("Missing Minecraft mod metadata")
    return data

def multipart(meta,name,data):
    boundary="bridgetap-"+uuid.uuid4().hex
    body=("--"+boundary+'\r\nContent-Disposition: form-data; name="data"\r\nContent-Type: application/json\r\n\r\n').encode()+json.dumps(meta).encode()
    body+=("\r\n--"+boundary+'\r\nContent-Disposition: form-data; name="jar"; filename="'+name+'"\r\nContent-Type: application/java-archive\r\n\r\n').encode()+data
    body+=("\r\n--"+boundary+"--\r\n").encode()
    return body,"multipart/form-data; boundary="+boundary

def verify(response,project,name,data):
    if response["project_id"]!=project:raise ValueError("Modrinth returned a different project")
    files=response["files"]
    if len(files)!=1 or files[0]["filename"]!=name or not files[0]["primary"]:raise ValueError("Modrinth upload file/primary mismatch")
    if files[0]["hashes"]["sha512"]!=sha(data,"sha512") or files[0]["size"]!=len(data):raise ValueError("Modrinth uploaded file checksum mismatch")

def create(project,release,asset,edition,data,token):
    version=release["tag_name"][1:]+"-"+edition
    label="Forge" if edition=="forge" else "OneClient / Ornithe" if edition=="oneclient" else "Ornithe"
    changelog=(release.get("body") or release["name"])+"\n\n---\nOriginal GitHub release: "+release["html_url"]+"\nOriginal release date: "+release["published_at"]+"\n\nHistorical binary imported unchanged from GitHub. For the current version, use BridgeTap 1.7.0."
    meta={"name":"BridgeTap "+release["tag_name"][1:]+" - "+label,"version_number":version,"project_id":project,"changelog":changelog,"dependencies":[],"game_versions":["1.8.9"],"version_type":"beta" if release["prerelease"] else "release","loaders":["forge" if edition=="forge" else "ornithe"],"featured":False,"file_parts":["jar"],"primary_file":"jar"}
    body,kind=multipart(meta,asset["name"],data)
    for attempt in range(3):
        request=urllib.request.Request(API+"version",data=body,headers={"Authorization":token,"User-Agent":UA,"Content-Type":kind},method="POST")
        try:
            with urllib.request.urlopen(request,timeout=180) as response:result=json.load(response)
            verify(result,project,asset["name"],data)
            return result
        except urllib.error.HTTPError as error:
            if error.code==429 and attempt<2:
                delay=min(60,max(2,int(error.headers.get("Retry-After","30"))));time.sleep(delay);continue
            detail=error.read(4096).decode("utf-8",errors="replace").replace(token,"[redacted]")
            raise RuntimeError("Modrinth upload failed (HTTP "+str(error.code)+"): "+detail[:500]) from None
    raise RuntimeError("Modrinth rate limit exhausted")

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo",default="sgggnnn/bridgetap");parser.add_argument("--project",required=True)
    parser.add_argument("--receipts",type=Path,default=Path("docs/modrinth-versions.json"));parser.add_argument("--output",type=Path,default=Path("build/modrinth-import-results.json"));parser.add_argument("--dry-run",action="store_true")
    args=parser.parse_args()
    if not re.fullmatch(r"[A-Za-z0-9]{8}",args.project):raise ValueError("Use the exact Modrinth project ID")
    token=os.environ.get("MODRINTH_TOKEN","").strip()
    if not args.dry_run and not token:raise ValueError("Missing MODRINTH_TOKEN")
    receipts=json.loads(args.receipts.read_text(encoding="utf-8")) if args.receipts.exists() else []
    index={(r["project"],r["tag"],r["filename"]):r for r in receipts}
    entries=plan(releases(args.repo));created=skipped=0
    args.output.parent.mkdir(parents=True,exist_ok=True)
    try:
        for release,asset,edition in entries:
            key=args.project,release["tag_name"],asset["name"]
            digest=asset.get("digest","").removeprefix("sha256:")
            if key in index:
                if index[key]["sha256"]!=digest:raise ValueError("Previously published GitHub asset changed: "+asset["name"])
                skipped+=1;print("SKIP verified existing:",asset["name"],flush=True);continue
            print("PLAN" if args.dry_run else "UPLOAD",release["tag_name"],edition,asset["name"],flush=True)
            if args.dry_run:continue
            folder=Path("build/modrinth-history")/release["tag_name"];folder.mkdir(parents=True,exist_ok=True)
            data=download(asset,folder);result=create(args.project,release,asset,edition,data,token)
            receipt={"project":args.project,"tag":release["tag_name"],"filename":asset["name"],"sha256":sha(data),"modrinth_version":result["id"],"version_number":result["version_number"],"source":"Original GitHub release asset; uploaded SHA-512 and size verified"}
            receipts.append(receipt);index[key]=receipt;created+=1
            args.output.write_text(json.dumps(receipts,indent=2)+"\n",encoding="utf-8")
            print("VERIFIED",result["id"],result["version_number"],flush=True);time.sleep(2)
    finally:args.output.write_text(json.dumps(receipts,indent=2)+"\n",encoding="utf-8")
    print("COMPLETE:",len(entries),"edition files;",skipped,"already present;",created,"added",flush=True)

if __name__=="__main__":main()
