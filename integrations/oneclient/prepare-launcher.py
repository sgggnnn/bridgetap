"""Apply the local integration to the pinned upstream source, without any git push."""
from pathlib import Path
import argparse,subprocess,shutil,json,zipfile,hashlib,importlib.util
ROOT=Path(__file__).resolve().parents[2]
def validate_payload(path):
 with zipfile.ZipFile(path) as jar:
  manifest=json.loads(jar.read('fabric.mod.json'))
  if manifest.get('id')!='bridgetap' or manifest.get('depends',{}).get('minecraft')!='1.8.9':
   raise ValueError('Expected a BridgeTap Minecraft 1.8.9 OneClient JAR')
  if 'com/bridger/momentum/oneclient/OneClientConfig.class' not in jar.namelist():
   raise ValueError('The payload must include the native OneConfig adapter')
  return manifest['version']

def main():
 p=argparse.ArgumentParser();p.add_argument('--jar',type=Path,required=True);p.add_argument('--destination',type=Path,default=ROOT/'build/oneclient-local');a=p.parse_args()
 revision=(Path(__file__).parent/'upstream.txt').read_text().strip()
 if a.destination.exists():raise SystemExit('Use a new destination to preserve existing work')
 if not a.jar.is_file():raise SystemExit('Build the OneClient integration JAR first')
 version=validate_payload(a.jar)
 subprocess.run(['git','clone','--filter=blob:none','https://github.com/Polyfrost/OneLauncher.git',str(a.destination)],check=True)
 subprocess.run(['git','checkout','--detach',revision],cwd=a.destination,check=True)
 patch=Path(__file__).parent/'launcher.patch'
 subprocess.run(['git','apply','--check',str(patch)],cwd=a.destination,check=True)
 subprocess.run(['git','apply',str(patch)],cwd=a.destination,check=True)
 shutil.copytree(Path(__file__).parent/'launcher',a.destination,dirs_exist_ok=True)
 assets=a.destination/'packages/oneclient_core/assets';assets.mkdir(exist_ok=True)
 shutil.copyfile(a.jar,assets/'BridgeTap-OneClient.jar')
 (assets/'bridgetap-version.txt').write_text(version+'\n',encoding='utf-8')
 shutil.copyfile(Path(__file__).parent/'menu-catalog.json',assets/'bridgetap-menus.json')
 spec=importlib.util.spec_from_file_location('brand_launcher',Path(__file__).parent/'brand-launcher.py');brand=importlib.util.module_from_spec(spec);spec.loader.exec_module(brand);brand.apply(a.destination)
 (a.destination/'BRIDGETAP-BUILD.json').write_text(json.dumps({'edition':'Unofficial OneClient with BridgeTap','upstream':revision,'bridgetap_version':version,'payload_sha256':hashlib.sha256(a.jar.read_bytes()).hexdigest()},indent=2)+'\n',encoding='utf-8')
 print('Prepared local launcher at',a.destination)
if __name__=='__main__':main()
