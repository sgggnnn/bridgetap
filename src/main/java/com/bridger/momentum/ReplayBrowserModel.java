package com.bridger.momentum;
import java.util.*;
/** Pure table ordering and counts; never opens recording files. */
public final class ReplayBrowserModel {
 public static Comparator<RunHistory.Run> order(int key,boolean ascending){
  Comparator<RunHistory.Run> result=key==1?Comparator.comparing(RunHistory.Run::displayName,String.CASE_INSENSITIVE_ORDER):key==2?Comparator.comparingDouble(r->r.time):key==3?Comparator.comparing(r->r.mode().label,String.CASE_INSENSITIVE_ORDER):Comparator.comparingLong(r->r.created);
  if(!ascending)result=result.reversed();return result.thenComparing(r->r.id);
 }
 public static Map<String,Integer> counts(Collection<RunHistory.Run> runs,ReplayFolders folders,Set<String> trash){
  Map<String,Integer> counts=new HashMap<>();for(RunHistory.Run run:runs){if(trash.contains(run.id)){counts.merge("browser-trash",1,Integer::sum);continue;}counts.merge(ReplayFolders.ALL,1,Integer::sum);if(run.isProtected())counts.merge(ReplayFolders.SAVED,1,Integer::sum);String at=folders.folderOf(run.id);while(!at.isEmpty()){counts.merge(at,1,Integer::sum);at=folders.parentOf(at);}}return counts;
 }
 private ReplayBrowserModel(){}
}
