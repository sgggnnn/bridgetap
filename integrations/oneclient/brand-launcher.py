"""Apply visible branding without changing protocols, saved paths or upstream notices."""
import json
import re
from pathlib import Path

BRAND = re.compile(r'\b(?:One[ -]?Client|ONECLIENT|OneLauncher)\b')


def apply(destination):
    app = Path(destination) / 'packages/oneclient_app'
    # Translation identifiers remain compatible with the existing settings and remote tables.
    for path in (app / 'lang').glob('*.json'):
        values = json.loads(path.read_text(encoding='utf-8'))
        values = {key: BRAND.sub('bridgetapclient', value) if isinstance(value, str) else value
                  for key, value in values.items()}
        path.write_text(json.dumps(values, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    for path in (app / 'src').rglob('*.rs'):
        source = path.read_text(encoding='utf-8')
        # Only string-literal contents. Never rename Rust/API identifiers or URL schemes.
        source = re.sub(r'"(?:[^"\\]|\\.)*"',
                        lambda match: match.group() if ('://' in match.group() or (match.group() == '"OneClient"' and not any(part in ('view','components') for part in path.parts)) or re.search(r'org\.polyfrost|\.exe|\.dll|\.lnk', match.group())) else BRAND.sub('bridgetapclient', match.group()), source)
        path.write_text(source, encoding='utf-8')
    # The generated shortcut's fallback name is visible to the player.
    shortcut = app / 'src/shortcut/mod.rs'
    shortcut.write_text(shortcut.read_text(encoding='utf-8').replace('const FALLBACK_STEM: &str = "OneClient";', 'const FALLBACK_STEM: &str = "bridgetapclient";'), encoding='utf-8')
    # All wordmarks use actual text rather than the upstream outlined logo.
    path = app / 'src/theme/mod.rs'
    source = path.read_text(encoding='utf-8')
    start = source.index('pub fn wordmark(')
    end = source.index('\npub fn rounded_px', start)
    source = source[:start] + '''pub fn wordmark(width: f32, height: f32, color: Color) -> Element {
    label().text("bridgetapclient").font_family(font())
        .font_weight(FontWeight::BOLD).font_size(text_size(height * 0.56))
        .width(Size::px(width)).height(Size::px(height)).color(color).into_element()
}
''' + source[end:]
    path.write_text(source, encoding='utf-8')
    # The mod's bitmap logo must keep its colors; SVG tinting is for monochrome icons.
    path = app / 'src/components/icons.rs'
    source = path.read_text(encoding='utf-8')
    needle = '    fn render(&self) -> impl IntoElement {\n'
    replacement = needle + '''        if self.icon == IconType::IconLogo {
            return ImageViewer::new(("bridgetap-logo.png", AppAssets::get_bytes("bridgetap-logo.png").unwrap_or_default()))
                .width(Size::px(self.size_px)).height(Size::px(self.size_px)).into_element();
        }
'''
    source = source.replace(needle, replacement, 1)
    source = source.replace('.map(self.rotation, |svg, degrees| svg.rotation(degrees))',
                            '.map(self.rotation, |svg, degrees| svg.rotation(degrees)).into_element()')
    path.write_text(source, encoding='utf-8')
    # Runtime translations downloaded from the upstream service receive the same visible name.
    path = app / 'src/i18n.rs'
    source = path.read_text(encoding='utf-8').replace(
        'pub fn install(table: Option<HashMap<String, String>>) {',
        '''pub fn install(table: Option<HashMap<String, String>>) {
    let table = table.map(|table| table.into_iter().map(|(key, value)|
        (key, if value.contains("://") { value } else { value.replace("OneClient", "bridgetapclient").replace("One Client", "bridgetapclient").replace("ONECLIENT", "bridgetapclient") })
    ).collect());''')
    path.write_text(source, encoding='utf-8')


if __name__ == '__main__':
    import sys
    apply(Path(sys.argv[1]))
