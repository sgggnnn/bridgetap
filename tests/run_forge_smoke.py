#!/usr/bin/env python3
"""Test real Forge discovery and all 13 Mixins without opening a game window.
--classpath must contain Forge universal, LaunchWrapper, the original Minecraft
1.8.9 client and its regular libraries, as provided by the launcher's metadata.
The smoke target replaces Minecraft.main only for this test, never in the mod.
"""
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
parser.add_argument('--mod',type=Path,default=root/'BridgeTap-1.4.4-Forge-1.8.9.jar')
args=parser.parse_args()
work=root/'build/forge'/('smoke-'+uuid.uuid4().hex)
classes=work/'classes';classes.mkdir(parents=True)
game=work/'game';(game/'mods').mkdir(parents=True)
shutil.copyfile(args.mod,game/'mods/BridgeTap.jar')
subprocess.run([args.javac,'--release','8','-proc:none','-cp',args.classpath,'-d',str(classes),*map(str,(root/'tests/forge').glob('*.java'))],check=True)
output=subprocess.run([args.java,'-Dmixin.debug.verbose=true','-cp',args.classpath+os.pathsep+str(classes),'net.minecraft.launchwrapper.Launch','--tweakClass','bridgetap.smoke.ForgeSmokeTweaker','--gameDir',str(game),'--version','1.8.9'],cwd=game,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
log=output.stdout.decode(errors='replace');(work/'launch.log').write_text(log,encoding='utf-8')
print('\n'.join(line for line in log.splitlines() if 'PASS' in line or 'ERROR' in line))
if output.returncode or 'PASS: Forge LaunchWrapper, bundled Mixin/ASM, 34 transformed methods and replay class linkage' not in log:
    raise SystemExit('Forge smoke failed. Full log: '+str(work/'launch.log'))
print('Forge smoke passed. Log:',work/'launch.log')
