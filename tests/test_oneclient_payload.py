"""Reject wrong editions before fetching or modifying upstream source."""
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
import zipfile

spec = importlib.util.spec_from_file_location('prepare_launcher', Path(__file__).resolve().parents[1] / 'integrations/oneclient/prepare-launcher.py')
prepare = importlib.util.module_from_spec(spec)
spec.loader.exec_module(prepare)


class PayloadTest(unittest.TestCase):
    def check_payload(self, mod_id='bridgetap', minecraft='1.8.9', adapter=True):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'payload.jar'
            with zipfile.ZipFile(path, 'w') as jar:
                jar.writestr('fabric.mod.json', json.dumps({'id': mod_id, 'version': 'test', 'depends': {'minecraft': minecraft}}))
                if adapter:
                    jar.writestr('com/bridger/momentum/oneclient/OneClientConfig.class', b'fixture')
            return prepare.validate_payload(path)

    def test_native_payload(self):
        self.assertEqual(self.check_payload(), 'test')

    def test_wrong_mod(self):
        with self.assertRaises(ValueError):
            self.check_payload(mod_id='other')

    def test_wrong_minecraft(self):
        with self.assertRaises(ValueError):
            self.check_payload(minecraft='1.21')

    def test_missing_native_adapter(self):
        with self.assertRaises(ValueError):
            self.check_payload(adapter=False)


if __name__ == '__main__':
    unittest.main()
