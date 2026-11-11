#!/usr/bin/env python3
import argparse, json, os, subprocess, tempfile
from video_bundle import add_video
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description='Compile the Ornithe edition for Java 8.')
parser.add_argument('--java-home', default=os.environ.get('JAVA_HOME'))
parser.add_argument('--classpath', default=os.environ.get('BRIDGETAP_CLASSPATH'))
parser.add_argument('--output', type=Path)
args = parser.parse_args()
deps = args.classpath
if not deps: raise SystemExit('Set BRIDGETAP_CLASSPATH to Minecraft intermediary, Fabric Loader, Mixin, LWJGL 2, Authlib and Guava JARs, separated by ' + os.pathsep)
version = json.loads((root/'src/main/resources/fabric.mod.json').read_text(encoding='utf-8'))['version']
out = args.output or root/'dist'/('BridgeTap-'+version+'.jar')
if out.exists(): raise SystemExit('Output already exists; refusing to overwrite')
out.parent.mkdir(parents=True,exist_ok=True)
compiler = str(Path(args.java_home)/'bin'/('javac.exe' if os.name=='nt' else 'javac')) if args.java_home else 'javac'
with tempfile.TemporaryDirectory() as tmp:
 classes=Path(tmp)
 sources=sorted((root/'src/main/java').rglob('*.java'))
 subprocess.run([compiler,'--release','8','-encoding','UTF-8','-Xlint:-options','-proc:none','-cp',deps,'-d',tmp,*map(str,sources)],check=True)
 with ZipFile(out,'x',ZIP_DEFLATED) as z:
  for folder in (classes,root/'src/main/resources'):
   for p in sorted(folder.rglob('*')):
    if p.is_file(): z.write(p,p.relative_to(folder).as_posix())
java = str(Path(args.java_home)/'bin'/('java.exe' if os.name=='nt' else 'java')) if args.java_home else 'java'
add_video(out,java)
print('Built',out)
