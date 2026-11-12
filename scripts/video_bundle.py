"""Bundle a private JCodec 0.2.5 runtime for silent H.264/MP4 clips."""
import argparse, hashlib, os, subprocess, urllib.request, shutil
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

ROOT=Path(__file__).resolve().parents[1]
DEPS=ROOT/'build/clip-deps'

def fetch(name,url,sha):
    DEPS.mkdir(parents=True,exist_ok=True)
    file=DEPS/name
    if not file.exists():
        with urllib.request.urlopen(url,timeout=60) as response: data=response.read()
        if hashlib.sha256(data).hexdigest()!=sha: raise RuntimeError('Dependency checksum mismatch: '+name)
        file.write_bytes(data)
    if hashlib.sha256(file.read_bytes()).hexdigest()!=sha: raise RuntimeError('Dependency checksum mismatch: '+name)
    return file

def add_video(jar,java):
    dependencies=[fetch('jcodec-0.2.5.jar','https://repo.maven.apache.org/maven2/org/jcodec/jcodec/0.2.5/jcodec-0.2.5.jar','890329dad124e8b739c1d6602a59a53c8a474daddff265c2561e21c498496c81'),
        fetch('jcodec-javase-0.2.5.jar','https://repo.maven.apache.org/maven2/org/jcodec/jcodec-javase/0.2.5/jcodec-javase-0.2.5.jar','8a72ff7560bce1c89c1d815816d1304383bb4048afeeb91f200b7a1e213ab6ae')]
    runtime=DEPS/'jcodec-private-0.2.5.jar'
    if not runtime.exists():
        special=ROOT/'build/forge/specialsource.jar'
        if not special.exists() or hashlib.sha256(special.read_bytes()).hexdigest()!='e2cab24b1c12400ad73b15972bb21e4273a0dc7081c8b3c136ddfdd824c78518':
            special=fetch('specialsource.jar','https://repo.maven.apache.org/maven2/net/md-5/SpecialSource/1.11.4/SpecialSource-1.11.4-shaded.jar','e2cab24b1c12400ad73b15972bb21e4273a0dc7081c8b3c136ddfdd824c78518')
        packages=set();seen=set()
        with ZipFile(DEPS/'jcodec-merged.jar','w',ZIP_DEFLATED) as merged:
            for dep in dependencies:
                with ZipFile(dep) as source:
                    for name in source.namelist():
                        if not name.startswith('org/jcodec/') or name.endswith('/') or name in seen:continue
                        seen.add(name);merged.writestr(name,source.read(name))
                        if name.endswith('.class'):packages.add(name.rsplit('/',1)[0])
        mapping=DEPS/'jcodec-private.srg'
        mapping.write_text('\n'.join('PK: '+p+' '+p.replace('org/jcodec','com/bridger/momentum/shaded/jcodec',1) for p in sorted(packages))+'\n')
        subprocess.run([str(java),'-jar',str(special),'--in-jar',str(DEPS/'jcodec-merged.jar'),'--out-jar',str(runtime),'--srg-in',str(mapping)],check=True)
    with ZipFile(jar,'a',ZIP_DEFLATED) as target,ZipFile(runtime) as source:
        existing=set(target.namelist())
        for name in source.namelist():
            if name.startswith('com/bridger/momentum/shaded/jcodec/') and not name.endswith('/') and name not in existing:target.writestr(name,source.read(name))
        license_path='META-INF/licenses/jcodec-LICENSE.txt'
        if license_path not in existing:target.writestr(license_path,(ROOT/'third_party/jcodec-LICENSE.txt').read_bytes())
    add_windows_audio(jar)

def add_windows_audio(jar):
    wheel=fetch('imageio_ffmpeg-0.6.0-py3-none-win_amd64.whl','https://files.pythonhosted.org/packages/2c/c6/fa760e12a2483469e2bf5058c5faff664acf66cadb4df2ad6205b016a73d/imageio_ffmpeg-0.6.0-py3-none-win_amd64.whl','02fa47c83703c37df6bfe4896aab339013f62bf02c5ebf2dce6da56af04ffc0a')
    helper=DEPS/'bridgetap-loopback.exe';source=ROOT/'native/Loopback.cs';stamp=DEPS/'loopback-source.sha256';digest=hashlib.sha256(source.read_bytes()).hexdigest()
    if not helper.exists() or not stamp.exists() or stamp.read_text()!=digest:
        compiler=Path(os.environ.get('WINDIR','C:/Windows'))/'Microsoft.NET/Framework64/v4.0.30319/csc.exe'
        if compiler.is_file():
            subprocess.run([str(compiler),'/nologo','/optimize+','/target:winexe','/out:'+str(helper),str(source)],check=True)
        else:
            # Cross-compile the same .NET Framework helper on Linux release runners.
            # The hosted Ubuntu runner supplies dotnet; no Mono or administrator install is needed.
            dotnet=shutil.which('dotnet')
            if not dotnet:raise RuntimeError('A .NET SDK is required to package the Windows audio helper on this platform')
            compiler_package=fetch('microsoft.net.compilers.toolset.4.8.0.nupkg','https://api.nuget.org/v3-flatcontainer/microsoft.net.compilers.toolset/4.8.0/microsoft.net.compilers.toolset.4.8.0.nupkg','37333f4f1e2ce55e621355d6da651dc23d4cb5f94a8f76b9478816e87f110ad9')
            refs_package=fetch('microsoft.netframework.referenceassemblies.net48.1.0.3.nupkg','https://api.nuget.org/v3-flatcontainer/microsoft.netframework.referenceassemblies.net48/1.0.3/microsoft.netframework.referenceassemblies.net48.1.0.3.nupkg','8a7e348538e7eb91351696911689f49e3d4f63f8bab517432bbe159b8b1104a2')
            roslyn=DEPS/'roslyn-4.8.0';roslyn.mkdir(exist_ok=True);refs=DEPS/'net48-refs';refs.mkdir(exist_ok=True)
            with ZipFile(compiler_package) as archive:
                prefix='tasks/netcore/bincore/'
                for entry in archive.namelist():
                    if entry.startswith(prefix) and not entry.endswith('/'):
                        relative=Path(entry[len(prefix):])
                        if relative.is_absolute() or '..' in relative.parts:raise RuntimeError('Invalid compiler package path')
                        target=roslyn/relative;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(archive.read(entry))
            with ZipFile(refs_package) as archive:
                for name in ('mscorlib.dll','System.dll','System.Core.dll'):
                    entry=next(n for n in archive.namelist() if n.endswith('/v4.8/'+name));(refs/name).write_bytes(archive.read(entry))
            env=dict(os.environ,DOTNET_ROLL_FORWARD='LatestMajor')
            subprocess.run([dotnet,str(roslyn/'csc.dll'),'/noconfig','/nologo','/optimize+','/target:winexe','/nostdlib+',*[('/r:'+str(refs/name)) for name in ('mscorlib.dll','System.dll','System.Core.dll')],'/out:'+str(helper),str(source)],check=True,env=env)
        stamp.write_text(digest)
    with ZipFile(wheel) as archive:
        binary=archive.read(next(n for n in archive.namelist() if n.endswith('.exe')))
        wrapper_license=archive.read(next(n for n in archive.namelist() if n.endswith('/LICENSE')))
    with ZipFile(jar,'a',ZIP_DEFLATED) as target:
        files={'ffmpeg.exe':binary,'bridgetap-loopback.exe':helper.read_bytes()}
        target.writestr('bridgetap-native/hashes.properties','\n'.join(name+'='+hashlib.sha256(data).hexdigest() for name,data in files.items())+'\n')
        target.writestr('META-INF/licenses/imageio-ffmpeg-LICENSE.txt',wrapper_license)
        target.writestr('META-INF/licenses/ffmpeg-GPLv3.txt',(ROOT/'third_party/ffmpeg-GPLv3.txt').read_bytes())
        target.writestr('META-INF/licenses/ffmpeg-NOTICE.md',(ROOT/'third_party/ffmpeg-NOTICE.md').read_bytes())
    # Explicit optional installation: the mod never extracts or downloads executables.
    pack=ROOT/'build/libs/BridgeTap-clip-tools-windows-x64.zip';pack.parent.mkdir(parents=True,exist_ok=True)
    with ZipFile(pack,'w',ZIP_DEFLATED) as archive:
        for name,data in files.items():archive.writestr('config/bridgetap/tools/'+name,data)
        archive.writestr('SHA256SUMS.txt',''.join(hashlib.sha256(data).hexdigest()+'  config/bridgetap/tools/'+name+'\n' for name,data in files.items()))
        archive.writestr('INSTALL.txt','Optional Windows x64 clip tools. Extract into your Minecraft game directory. Files must be in config/bridgetap/tools/. Restart the video buffer afterward. No administrator rights required. Do not disable antivirus protection.\n')
        for name in ('ffmpeg-GPLv3.txt','ffmpeg-NOTICE.md'):archive.writestr('licenses/'+name,(ROOT/'third_party'/name).read_bytes())
        archive.writestr('licenses/imageio-ffmpeg-LICENSE.txt',wrapper_license)


if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('jar',type=Path);p.add_argument('--java',required=True);a=p.parse_args();add_video(a.jar,a.java)
