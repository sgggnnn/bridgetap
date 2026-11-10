#!/usr/bin/env python3
"""Build the shared Java 8 sources for Forge 1.8.9 using runtime SRG names.

The source namespace bridge is generated from Calamus and MCP's official SRG
mapping. No Minecraft, Forge, Fabric or user recordings are bundled in the mod.
Mixin and its private ASM runtime are bundled for standalone Forge installation.
"""
import argparse
import json
import os
import re
import html
import subprocess
import urllib.request
import uuid
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

ROOT = Path(__file__).resolve().parents[1]
WORK = ROOT / 'build' / 'forge'
FORGE = '1.8.9-11.15.1.2318-1.8.9'


def fetch(name, url):
    path = WORK / name
    if not path.exists():
        print('Downloading', name, flush=True)
        request = urllib.request.Request(url, headers={'User-Agent': 'BridgeTap-build/1.0'})
        with urllib.request.urlopen(request, timeout=60) as response:
            content = response.read()
        path.write_bytes(content)
    return path


def run(*args):
    subprocess.run([str(a) for a in args], check=True, cwd=ROOT)


def mappings(calamus, srg):
    classes, fields, methods = {}, {}, {}
    for line in srg.read_text().splitlines():
        p = line.split()
        if p[0] == 'CL:': classes[p[1]] = p[2]
        elif p[0] == 'FD:': fields[p[1]] = p[2].rsplit('/', 1)[1]
        elif p[0] == 'MD:': methods[p[1], p[2]] = p[3].rsplit('/', 1)[1]
    names, candidates = {}, {}
    for line in calamus.read_text().splitlines()[1:]:
        p = line.split('\t')
        if p[0] == 'CLASS': names[p[2]] = classes.get(p[1], p[1])
        else:
            kind, owner, desc, old, new = p
            target = fields.get(owner+'/'+old, old) if kind == 'FIELD' else methods.get((owner+'/'+old, desc), old)
            candidates.setdefault(new, set()).add(target)
    return names, {k: next(iter(v)) for k, v in candidates.items() if len(v) == 1}


def convert(text, classes, members):
    text = re.sub(r'import net\.minecraft\.unmapped\.[^;]+;\s*', '', text)
    # Descriptors and @At target strings use slash names; Java uses dotted names.
    for old, new in sorted(classes.items(), key=lambda pair: -len(pair[0])):
        text = text.replace(old, new)
    short = {k.rsplit('/', 1)[1].replace('$', '.'): v.replace('/', '.').replace('$', '.') for k, v in classes.items() if '/unmapped/' in k}
    text = re.sub(r'\bC_\d{8}(?:\.C_\d{8})*\b', lambda m: short[m[0]], text)
    text = re.sub(r'\b[fm]_\d{8}\b', lambda m: members[m[0]], text)
    assert not re.search(r'net[./]minecraft[./]unmapped|\b[fm]_\d{8}\b|\bC_\d{8}\b', text)
    return text


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--java-home', default=os.environ.get('JAVA_HOME'))
    parser.add_argument('--minecraft', type=Path, required=True, help='Original Mojang 1.8.9 client JAR')
    parser.add_argument('--classpath', default=os.environ.get('BRIDGETAP_CLASSPATH', ''), help='LWJGL, Authlib, Guava and other Minecraft libraries')
    parser.add_argument('--output', type=Path, default=ROOT / 'dist' / 'BridgeTap-1.5.0-Forge-1.8.9.jar')
    args = parser.parse_args()
    if args.output.exists(): raise SystemExit('Output already exists: '+str(args.output))
    args.output.parent.mkdir(parents=True, exist_ok=True)
    WORK.mkdir(parents=True, exist_ok=True)
    suffix = '.exe' if os.name == 'nt' else ''
    def java_tool(name): return Path(args.java_home)/'bin'/(name+suffix) if args.java_home else name
    java, javac = java_tool('java'), java_tool('javac')
    mcp = fetch('mcp-srg.zip', 'https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/1.8.9/mcp-1.8.9-srg.zip?download=1')
    with ZipFile(mcp) as archive: (WORK/'joined.srg').write_bytes(archive.read('joined.srg'))
    calamus = ROOT/'forge/mappings/calamus.tiny'
    special = fetch('specialsource.jar', 'https://repo.maven.apache.org/maven2/net/md-5/SpecialSource/1.11.4/SpecialSource-1.11.4-shaded.jar')
    forge = fetch('forge-universal.jar', 'https://maven.minecraftforge.net/net/minecraftforge/forge/'+FORGE+'/forge-'+FORGE+'-universal.jar')
    mixin = fetch('mixin-0.8.5.jar', 'https://maven.minecraftforge.net/org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar')
    asm = [fetch('asm'+module+'-9.2.jar', 'https://repo.maven.apache.org/maven2/org/ow2/asm/asm'+module+'/9.2/asm'+module+'-9.2.jar') for module in ('', '-tree', '-commons', '-util', '-analysis')]
    guava = fetch('guava-21.0.jar', 'https://repo.maven.apache.org/maven2/com/google/guava/guava/21.0/guava-21.0.jar')
    minecraft = WORK/'minecraft-srg.jar'
    if not minecraft.exists(): run(java, '-jar', special, '--in-jar', args.minecraft, '--out-jar', minecraft, '--srg-in', WORK/'joined.srg')
    classes, members = mappings(calamus, WORK/'joined.srg')
    generated = WORK/'generated'
    generated.mkdir(exist_ok=True)
    sources = []
    for source in (ROOT/'src/main/java').rglob('*.java'):
        if source.name == 'BridgeTapMod.java': continue
        target = generated/source.relative_to(ROOT/'src/main/java')
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(convert(source.read_text(encoding='utf-8'), classes, members), encoding='utf-8')
        sources.append(target)
    sources.extend((ROOT/'forge/src').rglob('*.java'))
    output_classes = WORK/('classes-'+uuid.uuid4().hex)
    output_classes.mkdir(exist_ok=True)
    # Avoid leaking a Fabric loader, its Mixin build or another MC namespace into javac.
    libraries = [p.strip() for p in args.classpath.split(os.pathsep) if p.strip() and not any(s in p.lower() for s in ('fabric-loader', 'sponge-mixin', 'client-intermediary'))]
    classpath = os.pathsep.join(map(str, [minecraft, forge, mixin, *asm, *libraries]))
    (WORK/'classpath.txt').write_text(classpath)
    run(javac, '--release', '8', '-encoding', 'UTF-8', '-proc:none', '-cp', classpath, '-d', output_classes, *sources)
    # Mixin uses its own ASM 9, without replacing Forge's ASM 5 in LaunchWrapper.
    relocation = WORK/'relocate.srg'
    packages = {'org/objectweb/asm'+package:'com/bridger/momentum/shaded/asm'+package for package in ('', '/tree', '/tree/analysis', '/commons', '/util', '/signature')}
    with ZipFile(guava) as lib:
        for name in lib.namelist():
            if name.startswith('com/google/common/') and name.endswith('.class'):
                package=name.rsplit('/',1)[0]
                packages[package]=package.replace('com/google/common','com/bridger/momentum/shaded/guava',1)
    relocation.write_text('\n'.join('PK: '+old+' '+new for old,new in packages.items())+'\n')
    runtime = WORK/'runtime.jar'
    with ZipFile(runtime, 'w', ZIP_DEFLATED) as out:
        seen = set()
        for jar in [mixin, *asm, guava]:
            with ZipFile(jar) as lib:
                for name in lib.namelist():
                    if name.endswith('/') or name in seen or name.startswith('META-INF/') or name == 'module-info.class': continue
                    seen.add(name); out.writestr(name, lib.read(name))
    relocated = WORK/'runtime-relocated.jar'
    if relocated.exists(): relocated.unlink()
    run(java, '-jar', special, '--in-jar', runtime, '--out-jar', relocated, '--srg-in', relocation)
    config = json.loads((ROOT/'src/main/resources/bridgetap.mixins.json').read_text())
    config['minVersion'] = '0.8.5'
    manifest = 'Manifest-Version: 1.0\r\nFMLCorePlugin: com.bridger.momentum.forge.BridgeTapLoadingPlugin\r\nFMLCorePluginContainsFMLMod: true\r\nForceLoadAsMod: true\r\nTweakClass: org.spongepowered.asm.launch.MixinTweaker\r\nTweakOrder: 0\r\n\r\n'
    with ZipFile(args.output, 'x', ZIP_DEFLATED) as out:
        out.writestr('META-INF/MANIFEST.MF', manifest)
        out.writestr('bridgetap.forge.mixins.json', json.dumps(config, indent=2))
        out.writestr('mcmod.info', json.dumps([{'modid':'bridgetap', 'name':'BridgeTap', 'version':'1.5.0', 'mcversion':'1.8.9', 'clientSideOnly':True, 'description':'Bridge recording, replay, free camera, POV and telemetry.', 'authorList':['Bridger']}]))
        for p in output_classes.rglob('*.class'): out.write(p, p.relative_to(output_classes).as_posix())
        for p in (ROOT/'src/main/resources/assets').rglob('*'):
            if p.is_file(): out.write(p, p.relative_to(ROOT/'src/main/resources').as_posix())
        with ZipFile(relocated) as lib:
            # These string-only adapters deliberately bridge to Forge's ASM 5
            # Remapper instance. Keeping their original descriptor avoids casting
            # that instance to our private ASM 9 Remapper type.
            native_bridges = {'org/spongepowered/asm/bridge/RemapperAdapter.class', 'org/spongepowered/asm/bridge/RemapperAdapterFML.class'}
            for name in lib.namelist():
                if not name.startswith('META-INF/') and not name.endswith('/'):
                    if name in native_bridges:
                        with ZipFile(mixin) as original: out.writestr(name, original.read(name))
                    else: out.writestr(name, lib.read(name))
        # The bundled runtime targets LaunchWrapper, not modern ModLauncher.
        services = {'org.spongepowered.asm.service.IMixinService':'org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper', 'org.spongepowered.asm.service.IMixinServiceBootstrap':'org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapperBootstrap', 'org.spongepowered.asm.service.IGlobalPropertyService':'org.spongepowered.asm.service.mojang.Blackboard'}
        for service, implementation in services.items(): out.writestr('META-INF/services/'+service, implementation+'\n')
        out.writestr('META-INF/licenses/mixin-LICENSE.txt', fetch('mixin-LICENSE.txt', 'https://raw.githubusercontent.com/SpongePowered/Mixin/0.8.5/LICENSE.txt').read_bytes())
        asm_license = fetch('asm-LICENSE.html', 'https://asm.ow2.io/license.html').read_text(encoding='utf-8')
        asm_license = re.search(r'<pre[^>]*>(.*?)</pre>', asm_license, re.S).group(1)
        out.writestr('META-INF/licenses/asm-LICENSE.txt', html.unescape(re.sub(r'<[^>]+>', '', asm_license)))
        out.writestr('META-INF/licenses/guava-LICENSE.txt', fetch('guava-LICENSE.txt', 'https://raw.githubusercontent.com/google/guava/v21.0/COPYING').read_bytes())
    print('Built', args.output, flush=True)


if __name__ == '__main__': main()
