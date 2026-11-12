"""Apply the local integration to the pinned upstream source, without any git push."""
from pathlib import Path
import argparse,subprocess,shutil
ROOT=Path(__file__).resolve().parents[2]
def main():
 p=argparse.ArgumentParser();p.add_argument('--jar',type=Path,required=True);p.add_argument('--destination',type=Path,default=ROOT/'build/oneclient-local');a=p.parse_args()
 revision=(Path(__file__).parent/'upstream.txt').read_text().strip()
 if a.destination.exists():raise SystemExit('Use a new destination to preserve existing work')
 if not a.jar.is_file():raise SystemExit('Build the OneClient integration JAR first')
 subprocess.run(['git','clone','https://github.com/Polyfrost/OneLauncher.git',str(a.destination)],check=True)
 subprocess.run(['git','checkout','--detach',revision],cwd=a.destination,check=True)
 patch=Path(__file__).parent/'launcher.patch'
 subprocess.run(['git','apply','--check',str(patch)],cwd=a.destination,check=True)
 subprocess.run(['git','apply',str(patch)],cwd=a.destination,check=True)
 shutil.copytree(Path(__file__).parent/'launcher',a.destination,dirs_exist_ok=True)
 assets=a.destination/'packages/oneclient_core/assets';assets.mkdir(exist_ok=True)
 shutil.copyfile(a.jar,assets/'BridgeTap-OneClient.jar')
 print('Prepared local launcher at',a.destination)
if __name__=='__main__':main()
