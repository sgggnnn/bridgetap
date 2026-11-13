#!/usr/bin/env python3
"""Test real Forge discovery and all required Mixins without opening a game window.
--classpath must contain Forge universal, LaunchWrapper, the original Minecraft
1.8.9 client and its regular libraries, as provided by the launcher's metadata.
The smoke target replaces Minecraft.main only for this test, never in the mod.
"""
import re
import argparse
import os
import shutil
import subprocess
import uuid
from pathlib import Path

root=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--java',required=True,help='Java 8 executable')
parser.add_argument('--javac',required=True,help='JDK 9+ javac executable')
parser.add_argument('--classpath',required=True)
parser.add_argument('--argentum-fixture',action='store_true',help='Exercise optional Argentum cache interception with a descriptor-compatible fixture')
parser.add_argument('--mod',type=Path,default=root/'dist/BridgeTap-1.7.0-dev.4-Forge-1.8.9.jar')
args=parser.parse_args()
work=root/'build/forge'/('smoke-'+uuid.uuid4().hex)
classes=work/'classes';classes.mkdir(parents=True)
game=work/'game';(game/'mods').mkdir(parents=True)
shutil.copyfile(args.mod,game/'mods/BridgeTap.jar')
subprocess.run([args.javac,'--release','8','-proc:none','-cp',args.classpath,'-d',str(classes),*map(str,(root/'tests/forge').glob('*.java'))],check=True)
subprocess.run([args.javac,'--release','8','-proc:none','-cp',str(root/'build/forge/minecraft-srg.jar')+os.pathsep+args.classpath,'-d',str(classes),str(root/'tests/compat/hud/CountingFontRenderer.java'),str(root/'tests/compat/audio/RecordingSoundHandler.java'),str(root/'tests/compat/cera/ConnectedTextures.java'),str(root/'tests/compat/cera/CtmRenderContext.java')],check=True)
if args.argentum_fixture:
    subprocess.run([args.javac,'--release','8','-proc:none','-cp',str(root/'build/forge/minecraft-srg.jar')+os.pathsep+args.classpath,'-d',str(classes),str(root/'tests/compat/argentum/ChunkRenderContext.java'),str(root/'tests/compat/blur/BlurRenderer.java')],check=True)
output=subprocess.run([args.java,'-Dmixin.debug.verbose=true','-cp',args.classpath+os.pathsep+str(classes),'net.minecraft.launchwrapper.Launch','--tweakClass','bridgetap.smoke.ForgeSmokeTweaker','--gameDir',str(game),'--version','1.8.9'],cwd=game,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
log=output.stdout.decode(errors='replace');(work/'launch.log').write_text(log,encoding='utf-8')
print('\n'.join(line for line in log.splitlines() if 'PASS' in line or 'ERROR' in line))
if output.returncode or not re.search(r'PASS: Forge LaunchWrapper, bundled Mixin/ASM, [0-9]+ transformed methods and replay class linkage',log):
    raise SystemExit('Forge smoke failed. Full log: '+str(work/'launch.log'))
print('Forge smoke passed. Log:',work/'launch.log')
