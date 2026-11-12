"""Resolve the existing Modrinth project without exposing the publishing token."""
import json, os, re, urllib.request, urllib.error
TOKEN=os.environ.get("MODRINTH_TOKEN", "").strip()
if not TOKEN:raise SystemExit("Missing MODRINTH_TOKEN (or legacy BRIDGERTAP) repository secret")
def get(path):
    request=urllib.request.Request("https://api.modrinth.com/v2/"+path,headers={"Authorization":TOKEN,"User-Agent":"BridgeTap-publish/1.7 (github.com/sgggnnn/bridgetap)"})
    with urllib.request.urlopen(request,timeout=60) as response:return json.load(response)
try:
    project=os.environ.get("MODRINTH_PROJECT", "").strip()
    if project:
        if not re.fullmatch(r"[A-Za-z0-9_-]+",project):raise SystemExit("Invalid Modrinth project ID/slug")
        selected=get("project/"+project)
    else:
        user=get("user");projects=get("user/"+user["id"]+"/projects")
        candidates=[p for p in projects if any(re.sub(r"[^a-z]","",p.get(k,"").lower()) in ("bridgetap","bridgertap") for k in ("title","slug"))]
        if len(candidates)!=1:raise SystemExit("Set MODRINTH_PROJECT_ID to the intended existing project ID/slug; automatic matching was ambiguous or empty")
        selected=candidates[0]
    with open(os.environ["GITHUB_OUTPUT"],"a",encoding="utf-8") as out:out.write("project="+selected["id"]+"\n")
    print("Publishing project:",selected["title"],"("+selected["id"]+")")
except urllib.error.HTTPError as error:
    try:
        user=get("user")
        print("Token identity:",user.get("username",user["id"]))
        projects=get("user/"+user["id"]+"/projects")
        matches=[(p["id"],p.get("slug"),p.get("title")) for p in projects if "bridge" in p.get("title","").lower() or "bridge" in p.get("slug","").lower()]
        print("Accessible BridgeTap projects:",matches)
    except urllib.error.HTTPError as auth:
        print("Token identity/project-list check: HTTP",auth.code)
        try:
            detail=json.loads(auth.read(4096))
            reason=str(detail.get("description",detail.get("error",""))).replace(TOKEN,"[redacted]")
            print("Authentication reason:",reason[:300])
        except (ValueError,UnicodeError):pass
    raise SystemExit("Modrinth project lookup failed (HTTP "+str(error.code)+"). Check the repository secret and project access.")
