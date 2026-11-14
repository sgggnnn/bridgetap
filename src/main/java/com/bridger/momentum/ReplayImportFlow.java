package com.bridger.momentum;
import java.io.*;
import java.util.*;

/** Call only after the import preview has been explicitly confirmed. */
public final class ReplayImportFlow {
 private ReplayImportFlow(){}
 public static RunHistory.Run confirm(RunHistory history,ReplayArchive.Content content,ReplayFolders folders)throws IOException{
  RunHistory.previewImport(content);String target=null;
  for(Map.Entry<String,String> entry:folders.folders().entrySet())if(entry.getValue().equalsIgnoreCase("Imported")&&folders.parentOf(entry.getKey()).isEmpty()){target=entry.getKey();break;}
  if(target==null)target=folders.create("Imported");
  RunHistory.Run run=history.importReplay(content);folders.move(run.id,target);return run;
 }
}
