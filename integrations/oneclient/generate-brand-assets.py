"""Convert the existing mod logo into launcher assets; no logo is redrawn."""
from pathlib import Path
import base64
import io
from PIL import Image
from fontTools.ttLib import TTFont
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen

ROOT = Path(__file__).resolve().parents[2]
APP = ROOT / 'integrations/oneclient/launcher/packages/oneclient_app'
logo = Image.open(ROOT / 'src/main/resources/assets/bridgetap/icon.png').convert('RGBA')
icons = APP / 'icons'
assets = APP / 'assets'
icons.mkdir(parents=True, exist_ok=True)
(assets / 'icons/pixel').mkdir(parents=True, exist_ok=True)
for name, size in [('32x32.png',32),('64x64.png',64),('128x128.png',128),('128x128@2x.png',256),('512x512.png',512),('icon.png',256),('tray.png',64),('StoreLogo.png',128)]:
    logo.resize((size,size),Image.Resampling.LANCZOS).save(icons/name)
logo.save(icons/'icon.ico',sizes=[(16,16),(24,24),(32,32),(48,48),(64,64),(128,128),(256,256)])
logo.resize((1024,1024),Image.Resampling.LANCZOS).save(icons/'icon.icns')
compact=logo.resize((256,256),Image.Resampling.LANCZOS)
compact.save(assets/'bridgetap-logo.png')
stream=io.BytesIO();compact.save(stream,format='PNG')
encoded=base64.b64encode(stream.getvalue()).decode('ascii')
icon=f'<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 256 256"><image width="256" height="256" xlink:href="data:image/png;base64,{encoded}"/></svg>\n'
for path in [assets/'icons/icon-logo.svg', assets/'icons/pixel/icon-logo.svg']:
    path.write_text(icon,encoding='utf-8')
# Keep the splash wordmark as vector outlines, using the upstream bundled font.
font=TTFont(ROOT/'build/BridgeTapClient-preinstalled/packages/oneclient_app/assets/fonts/Poppins/Poppins-Bold.ttf')
glyphs=font.getGlyphSet();cmap=font.getBestCmap();pen=SVGPathPen(glyphs);x=0
for char in 'bridgetapclient':
    name=cmap[ord(char)];glyphs[name].draw(TransformPen(pen,(1,0,0,1,x,0)));x+=font['hmtx'][name][0]
wordmark=f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {x} 1200"><g transform="translate(0,950) scale(1,-1)"><path fill="white" d="{pen.getCommands()}"/></g></svg>\n'
(assets/'logo.svg').write_text(wordmark,encoding='utf-8')
print('Generated launcher icons and wordmark from the mod logo and bundled font')
