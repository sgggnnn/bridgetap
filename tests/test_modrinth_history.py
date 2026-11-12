"""Safety checks for importing immutable release artifacts into Modrinth."""
import importlib.util, json, unittest
from email.parser import BytesParser
from pathlib import Path
spec=importlib.util.spec_from_file_location("history",Path(__file__).resolve().parents[1]/"scripts/publish-modrinth-history.py")
history=importlib.util.module_from_spec(spec);spec.loader.exec_module(history)
class ImportRegression(unittest.TestCase):
    def test_multipart_keeps_binary_and_metadata(self):
        data=bytes(range(256))*2
        meta={"name":"BridgeTap","file_parts":["jar"],"primary_file":"jar"}
        body,kind=history.multipart(meta,"BridgeTap-1.0.0.jar",data)
        message=BytesParser().parsebytes(("MIME-Version: 1.0\r\nContent-Type: "+kind+"\r\n\r\n").encode()+body)
        parts=message.get_payload();self.assertEqual(len(parts),2)
        self.assertEqual(json.loads(parts[0].get_payload(decode=True)),meta)
        self.assertEqual(parts[1].get_payload(decode=True),data)
        self.assertEqual(parts[1].get_filename(),"BridgeTap-1.0.0.jar")
    def test_response_must_match_project_and_exact_file(self):
        data=b"original release"
        response={"project_id":"ueMpSLbl","files":[{"filename":"BridgeTap-1.0.0.jar","primary":True,"size":len(data),"hashes":{"sha512":history.sha(data,"sha512")}}]}
        history.verify(response,"ueMpSLbl","BridgeTap-1.0.0.jar",data)
        with self.assertRaises(ValueError):history.verify(response,"another-project","BridgeTap-1.0.0.jar",data)
        with self.assertRaises(ValueError):history.verify(response,"ueMpSLbl","BridgeTap-1.0.0.jar",b"modified bytes")
    def test_numeric_development_order_and_no_invented_editions(self):
        def release(tag,names,draft=False):return {"tag_name":tag,"draft":draft,"assets":[{"name":n} for n in names]}
        entries=history.plan([release("v1.6.0-dev.10",["BridgeTap-1.6.0-dev.10.jar"]),release("v1.6.0-dev.2",["BridgeTap-1.6.0-dev.2-Forge-1.8.9.jar"]),release("v1.0.0",["BridgeTap-1.0.0.jar","SHA256SUMS.txt"]),release("v1.6.0",["BridgeTap-1.6.0.jar"],True)])
        self.assertEqual([r[0]["tag_name"] for r in entries],["v1.0.0","v1.6.0-dev.2","v1.6.0-dev.10"])
        self.assertEqual([r[2] for r in entries],["ornithe","forge","ornithe"])
        with self.assertRaises(ValueError):history.plan([release("v1.0.0",["../../foreign.jar"])])
if __name__=="__main__":unittest.main()
