#!/usr/bin/env python3
"""Run the shared hook checks in Forge's runtime namespace.
Usage: python tests/check_forge_hooks.py <minecraft-srg.jar> <forge-mod.jar>
Run build-forge.py first to prepare the mapping cache.
"""
import importlib.util
import runpy
import sys
from pathlib import Path

root=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('forge_build',root/'build-forge.py')
builder=importlib.util.module_from_spec(spec);spec.loader.exec_module(builder)
classes,members=builder.mappings(root/'forge/mappings/calamus.tiny',root/'build/forge/joined.srg')
source=builder.convert((root/'tests/check_native_hooks.py').read_text(),classes,members)
source=source.replace('bridgetap.mixins.json','bridgetap.forge.mixins.json')
check=root/'build/forge/check_native_hooks.py';check.write_text(source)
sys.argv[0]=str(check)
state=runpy.run_path(str(check),run_name='__main__')
Class=state['Class'];mod=state['mod']
bridge=Class(mod.read('com/bridger/momentum/forge/BridgeTapForge.class'))
overlay=next(a for n,_,a in bridge.methods if n=='onOverlay')
assert any(kind.endswith('/SubscribeEvent;') for kind,_ in bridge.annotations(overlay)),'Forge HUD event handler is not subscribed'
assert bridge.has_call(overlay,bridge.calls('com/bridger/momentum/BridgeHUD','render','(Lnet/minecraft/client/gui/GuiIngame;)V')),'Forge overlay callback does not draw the coordinates HUD'
preinit=next(a for n,_,a in bridge.methods if n=='preInit')
assert bridge.has_call(preinit,bridge.calls('net/minecraftforge/fml/common/eventhandler/EventBus','register','(Ljava/lang/Object;)V')),'Forge HUD handler is not registered'
assert 'ALL' in bridge.cp,'Forge HUD must use the final ALL overlay event'
print('PASS: Forge overlay event registration and coordinates rendering path')
