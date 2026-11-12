"""Extract trusted OneConfig nested JARs to a compile-only dependency directory."""
from pathlib import Path
import io,sys,zipfile
out=Path(sys.argv[2]);out.mkdir(parents=True,exist_ok=True)
def unpack(data,depth=0):
 if depth>5:raise ValueError('Nested dependency limit exceeded')
 with zipfile.ZipFile(io.BytesIO(data)) as z:
  for entry in z.infolist():
   if entry.filename.endswith('.jar'):
    if entry.file_size>128*1024*1024:raise ValueError('Dependency too large')
    content=z.read(entry);(out/Path(entry.filename).name).write_bytes(content);unpack(content,depth+1)
unpack(Path(sys.argv[1]).read_bytes())
