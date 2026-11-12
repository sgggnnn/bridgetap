#!/usr/bin/env python3
"""Portable release build used by ./gradlew build (no private/local Minecraft paths)."""
import argparse,hashlib,io,json,os,re,shutil,subprocess,sys,urllib.request,zipfile,uuid
from pathlib import Path
from video_bundle import add_video
ROOT=Path(__file__).resolve().parents[1]
CACHE=ROOT/'build/gradle-bootstrap'
HEADERS={'User-Agent':'BridgeTap-build/1.0 (https://github.com/sgggnnn/bridgetap)'}
def request(url):
    with urllib.request.urlopen(urllib.request.Request(url,headers=HEADERS),timeout=60) as response:return response.read()
def fetch(name,url,sha=None,algorithm='sha1'):
    path=CACHE/name;path.parent.mkdir(parents=True,exist_ok=True)
    if path.exists() and (not sha or hashlib.new(algorithm,path.read_bytes()).hexdigest()==sha):return path
    print("Downloading",name,flush=True)
    data=request(url)
    if sha and hashlib.new(algorithm,data).hexdigest()!=sha:raise ValueError('Checksum mismatch: '+name)
    temp=path.with_suffix(path.suffix+'.part');temp.write_bytes(data);temp.replace(path);return path
def json_url(url):return json.loads(request(url))
def modrinth(version_id):
    meta=json_url('https://api.modrinth.com/v2/version/'+version_id);entry=next((f for f in meta['files'] if f.get('primary')),meta['files'][0])
    return fetch('mods/'+entry['filename'],entry['url'],entry['hashes']['sha512'],'sha512')
def run(*args):subprocess.run([str(a) for a in args],cwd=ROOT,check=True)
def tool(home,name):return Path(home)/'bin'/(name+('.exe' if os.name=='nt' else ''))
def nested_jars(jar,out):
    out.mkdir(parents=True,exist_ok=True)
    def unpack(data,depth=0):
        if depth>5:raise ValueError('Nested dependency limit')
        with zipfile.ZipFile(io.BytesIO(data)) as z:
            for entry in z.infolist():
                if entry.filename.endswith('.jar'):
                    if entry.file_size>128*1024*1024:raise ValueError('Dependency too large')
                    content=z.read(entry);(out/Path(entry.filename).name).write_bytes(content);unpack(content,depth+1)
    unpack(jar.read_bytes())
def prepare(java):
    manifest=json_url('https://piston-meta.mojang.com/mc/game/version_manifest_v2.json');version=next(v for v in manifest['versions'] if v['id']=='1.8.9');meta=json.loads(fetch('minecraft-version.json',version['url'],version.get('sha1')).read_bytes())
    client=meta['downloads']['client'];minecraft=fetch('minecraft-1.8.9-client.jar',client['url'],client['sha1']);libraries=[]
    for lib in meta['libraries']:
        artifact=lib.get('downloads',{}).get('artifact')
        if artifact:libraries.append(fetch('libraries/'+artifact['path'],artifact['url'],artifact['sha1']))
    libraries += [fetch('fabric-loader-0.20.1.jar','https://maven.fabricmc.net/net/fabricmc/fabric-loader/0.20.1/fabric-loader-0.20.1.jar'),fetch('sponge-mixin-0.17.5.jar','https://maven.fabricmc.net/net/fabricmc/sponge-mixin/0.17.5+mixin.0.8.7/sponge-mixin-0.17.5+mixin.0.8.7.jar')]
    special=fetch('specialsource.jar','https://repo.maven.apache.org/maven2/net/md-5/SpecialSource/1.11.4/SpecialSource-1.11.4-shaded.jar','e2cab24b1c12400ad73b15972bb21e4273a0dc7081c8b3c136ddfdd824c78518','sha256')
    tiny=(ROOT/'forge/mappings/calamus.tiny').read_text().splitlines()[1:];classes={p[1]:p[2] for line in tiny if (p:=line.split('\t'))[0]=='CLASS'}
    def descriptor(value):return re.sub(r'L([^;]+);',lambda m:'L'+classes.get(m[1],m[1])+';',value)
    lines=[]
    for line in tiny:
        p=line.split('\t')
        if p[0]=='CLASS':lines.append('CL: '+p[1]+' '+p[2])
        elif p[0]=='FIELD':lines.append('FD: '+p[1]+'/'+p[3]+' '+classes.get(p[1],p[1])+'/'+p[4])
        elif p[0]=='METHOD':lines.append('MD: '+p[1]+'/'+p[3]+' '+p[2]+' '+classes.get(p[1],p[1])+'/'+p[4]+' '+descriptor(p[2]))
    mapping=CACHE/'intermediary.srg';mapping.write_text('\n'.join(lines)+'\n');mapped=CACHE/'client-intermediary.jar'
    if not mapped.exists():run(java,'-jar',special,'--in-jar',minecraft,'--out-jar',mapped,'--srg-in',mapping)
    return minecraft,[mapped,*libraries]
def main():
    p=argparse.ArgumentParser();p.add_argument('--java-home',required=True);p.add_argument('--oneclient-java-home',required=True);p.add_argument('--python-executable',default=sys.executable);a=p.parse_args();CACHE.mkdir(parents=True,exist_ok=True)
    java,javac,jar=[tool(a.java_home,n) for n in ('java','javac','jar')];minecraft,libraries=prepare(java);cp=os.pathsep.join(map(str,libraries))
    version=json.loads((ROOT/'src/main/resources/fabric.mod.json').read_text())['version'];out=ROOT/'build/libs';out.mkdir(parents=True,exist_ok=True)
    work=ROOT/('build/gradle-classes-'+uuid.uuid4().hex);work.mkdir(parents=True,exist_ok=True)
    sources=sorted((ROOT/'src/main/java').rglob('*.java'));argfile=CACHE/'sources.txt';argfile.write_text('\n'.join('"'+str(s).replace('\\','/')+'"' for s in sources))
    run(javac,'--release','8','-encoding','UTF-8','-proc:none','-cp',cp,'-d',work,'@'+str(argfile))
    fabric=out/('BridgeTap-'+version+'-OneClient.jar');forge=out/('BridgeTap-'+version+'-Forge-1.8.9.jar')
    for target in (fabric,forge):target.unlink(missing_ok=True)
    run(jar,'cf',fabric,'-C',work,'.','-C',ROOT/'src/main/resources','.')
    add_video(fabric,java)
    compile_deps=CACHE/'oneconfig';nested_jars(modrinth('htWUN4sV'),compile_deps);nested_jars(modrinth('SKW1BTL3'),compile_deps)
    adapter=ROOT/('build/gradle-oneconfig-classes-'+uuid.uuid4().hex);adapter.mkdir(parents=True,exist_ok=True)
    run(tool(a.oneclient_java_home,'javac'),'--release','8','-encoding','UTF-8','-proc:none','-cp',os.pathsep.join((str(fabric),str(compile_deps/'*'),cp)),'-d',adapter,*sorted((ROOT/'integrations/oneclient/java').rglob('*.java')))
    run(jar,'uf',fabric,'-C',adapter,'.')
    # Run the existing namespace conversion and standalone Forge packaging.
    run(a.python_executable,ROOT/'scripts/build-forge.py','--java-home',a.java_home,'--minecraft',minecraft,'--classpath',cp,'--output',forge)
    print('Release artifacts:',fabric,forge,sep='\n')
if __name__=='__main__':main()
