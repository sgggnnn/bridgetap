package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Export presentation is independent of the live replay HUD toggles. */
public final class ReplayVideoOptions {
    public final int fps,height,camera;public final boolean inputs,title,metrics,cleanHud,hotbar,ending;
    public ReplayVideoOptions(int fps,int height,int camera,boolean inputs,boolean title,boolean metrics,boolean cleanHud){this(fps,height,camera,inputs,title,metrics,cleanHud,true,true);}
    public ReplayVideoOptions(int fps,int height,int camera,boolean inputs,boolean title,boolean metrics,boolean cleanHud,boolean hotbar,boolean ending){if((fps!=30&&fps!=60&&fps!=120)||(height!=360&&height!=720&&height!=1080)||camera<0||camera>2)throw new IllegalArgumentException("Invalid replay video options");this.fps=fps;this.height=height;this.camera=camera;this.inputs=inputs;this.title=title;this.metrics=metrics;this.cleanHud=cleanHud;this.hotbar=hotbar;this.ending=ending;}
    public static ReplayVideoOptions defaults(){return new ReplayVideoOptions(60,1080,0,true,true,false,true);}
    public static Path path(){return Paths.get("config","bridgetap","replay-video.properties");}
    public static ReplayVideoOptions load(Path path){try{if(Files.size(path)>65536)return defaults();Properties p=new Properties();try(InputStream in=Files.newInputStream(path)){p.load(in);}return new ReplayVideoOptions(Integer.parseInt(p.getProperty("fps","60")),Integer.parseInt(p.getProperty("height","1080")),Integer.parseInt(p.getProperty("camera","0")),Boolean.parseBoolean(p.getProperty("inputs","true")),Boolean.parseBoolean(p.getProperty("title","true")),Boolean.parseBoolean(p.getProperty("metrics","false")),Boolean.parseBoolean(p.getProperty("cleanHud","true")),Boolean.parseBoolean(p.getProperty("hotbar","true")),Boolean.parseBoolean(p.getProperty("ending","true")));}catch(IOException|IllegalArgumentException invalid){return defaults();}}
    public void save(Path path)throws IOException{Properties p=new Properties();p.setProperty("fps",String.valueOf(fps));p.setProperty("height",String.valueOf(height));p.setProperty("camera",String.valueOf(camera));p.setProperty("inputs",String.valueOf(inputs));p.setProperty("title",String.valueOf(title));p.setProperty("metrics",String.valueOf(metrics));p.setProperty("cleanHud",String.valueOf(cleanHud));p.setProperty("hotbar",String.valueOf(hotbar));p.setProperty("ending",String.valueOf(ending));Files.createDirectories(path.toAbsolutePath().getParent());try(OutputStream out=Files.newOutputStream(path)){p.store(out,"BridgeTap replay video preferences");}}
}
