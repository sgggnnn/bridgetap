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
runpy.run_path(str(check),run_name='__main__')
