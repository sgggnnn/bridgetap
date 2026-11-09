#!/usr/bin/env python3
import os, subprocess, tempfile
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
root = Path(__file__).resolve().parent
deps = os.environ.get('BRIDGETAP_CLASSPATH')
if not deps: raise SystemExit('Set BRIDGETAP_CLASSPATH to Minecraft intermediary, Fabric Loader, Mixin, LWJGL 2, Authlib and Guava JARs, separated by ' + os.pathsep)
out = root / 'BridgeTap-source-built.jar'
if out.exists(): raise SystemExit('Output already exists; refusing to overwrite')
with tempfile.TemporaryDirectory() as tmp:
 classes=Path(tmp)
 sources=sorted((root/'src/main/java').rglob('*.java'))
 subprocess.run(['javac','--release','8','-encoding','UTF-8','-Xlint:-options','-proc:none','-cp',deps,'-d',tmp,*map(str,sources)],check=True)
 with ZipFile(out,'x',ZIP_DEFLATED) as z:
  for folder in (classes,root/'src/main/resources'):
   for p in sorted(folder.rglob('*')):
    if p.is_file(): z.write(p,p.relative_to(folder).as_posix())
print('Built',out)
