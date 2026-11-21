"""Contracts for the prepared, production launcher assets and visible naming."""
import json
from pathlib import Path
import re
import sys
import unittest

SOURCE = Path(sys.argv.pop(1)) if len(sys.argv)>1 else Path('build/BridgeTapClient-branded')
ROOT = Path(__file__).resolve().parents[1]


class BrandingTest(unittest.TestCase):
    def test_all_translated_visible_names(self):
        languages=SOURCE/'packages/oneclient_app/lang'
        self.assertTrue(list(languages.glob('*.json')))
        for path in languages.glob('*.json'):
            for key,value in json.loads(path.read_text(encoding='utf-8')).items():
                if isinstance(value,str):self.assertIsNone(re.search(r'\bOne[ -]?Client\b|ONECLIENT',value),(path.name,key,value))

    def test_actual_mod_bitmap_and_windows_icon(self):
        app=SOURCE/'packages/oneclient_app'
        self.assertEqual((app/'assets/bridgetap-logo.png').read_bytes(),(ROOT/'integrations/oneclient/launcher/packages/oneclient_app/assets/bridgetap-logo.png').read_bytes())
        self.assertEqual((app/'icons/icon.ico').read_bytes()[:4],b'\x00\x00\x01\x00')
        self.assertIn('bridgetap-logo.png',(app/'src/components/icons.rs').read_text(encoding='utf-8'))

    def test_menu_catalog_matches_native_actions(self):
        catalog=json.loads((ROOT/'integrations/oneclient/menu-catalog.json').read_text(encoding='utf-8'))
        self.assertEqual(len(catalog),30)
        self.assertEqual(len({row['id'] for row in catalog}),len(catalog))
        java=(ROOT/'src/main/java/com/bridger/momentum/BridgeTapMenus.java').read_text(encoding='utf-8')
        adapter=(ROOT/'integrations/oneclient/java/com/bridger/momentum/oneclient/OneClientConfig.java').read_text(encoding='utf-8')
        for menu in catalog:
            self.assertIn('case "'+menu['id']+'":',java)
            self.assertIn('BridgeTapMenus.open("'+menu['id']+'")',adapter)
        self.assertEqual(json.loads((SOURCE/'packages/oneclient_core/assets/bridgetap-menus.json').read_text(encoding='utf-8')),catalog)

    def test_saved_path_and_api_identifiers_remain_compatible(self):
        paths=(SOURCE/'packages/oneclient_common/src/paths.rs').read_text(encoding='utf-8')
        self.assertIn('"BridgeTapClient"',paths)
        self.assertIn('"user_data.db"',paths)
        translations=json.loads((SOURCE/'packages/oneclient_app/lang/en_us.json').read_text(encoding='utf-8'))
        self.assertIn('oneclient.onboarding.welcome.title',translations)
        self.assertEqual(translations['oneclient.onboarding.welcome.title'],'Welcome to BridgeTapClient')


if __name__=='__main__':unittest.main()
