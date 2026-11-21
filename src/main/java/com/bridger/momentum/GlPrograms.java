package com.bridger.momentum;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.lwjgl.opengl.*;

/** Tiny GLSL 1.20 programs. No engine shader group or global reload is installed. */
final class GlPrograms {
 static final class Program {
  final int id;private final Map<String,Integer> uniforms=new HashMap<>();Program(int id){this.id=id;}
  int location(String name){return uniforms.computeIfAbsent(name,n->GL20.glGetUniformLocation(id,n));}
  void f(String name,float value){GL20.glUniform1f(location(name),value);}void i(String name,int value){GL20.glUniform1i(location(name),value);}
  void vec(String name,float x,float y){GL20.glUniform2f(location(name),x,y);}
  void color(String name,int color){GL20.glUniform4f(location(name),((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f,(color>>>24)/255f);}
 }
 private static final Map<String,Program> programs=new HashMap<>();private static final Set<String> failed=new HashSet<>();
 static Program get(String name,boolean fullscreen){
  if(failed.contains(name)||!GLContext.getCapabilities().OpenGL20)return null;if(programs.containsKey(name))return programs.get(name);
  int vertex=0,fragment=0,program=0;
  try{vertex=compile(GL20.GL_VERTEX_SHADER,source(fullscreen?"fullscreen.vert":"geometry.vert"));fragment=compile(GL20.GL_FRAGMENT_SHADER,source(name+".frag"));program=GL20.glCreateProgram();GL20.glAttachShader(program,vertex);GL20.glAttachShader(program,fragment);GL20.glLinkProgram(program);if(GL20.glGetProgrami(program,GL20.GL_LINK_STATUS)==0)throw new IOException("Link failed");Program result=new Program(program);programs.put(name,result);return result;
  }catch(IOException|RuntimeException invalid){if(program!=0)GL20.glDeleteProgram(program);failed.add(name);return null;}finally{if(vertex!=0)GL20.glDeleteShader(vertex);if(fragment!=0)GL20.glDeleteShader(fragment);}
 }
 private static String source(String name)throws IOException{try(InputStream in=GlPrograms.class.getResourceAsStream("/assets/bridgetap/shaders/gui/"+name)){if(in==null)throw new FileNotFoundException(name);ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[1024];int n;while((n=in.read(buffer))!=-1){if(bytes.size()+n>16384)throw new IOException("Shader too large");bytes.write(buffer,0,n);}return new String(bytes.toByteArray(),StandardCharsets.UTF_8);}}
 private static int compile(int type,String source)throws IOException{int id=GL20.glCreateShader(type);GL20.glShaderSource(id,source);GL20.glCompileShader(id);if(GL20.glGetShaderi(id,GL20.GL_COMPILE_STATUS)==0){GL20.glDeleteShader(id);throw new IOException("Compile failed");}return id;}
 static void quad(double x,double y,double w,double h){GL11.glBegin(GL11.GL_QUADS);GL11.glTexCoord2f(0,0);GL11.glVertex2d(x,y);GL11.glTexCoord2f(1,0);GL11.glVertex2d(x+w,y);GL11.glTexCoord2f(1,1);GL11.glVertex2d(x+w,y+h);GL11.glTexCoord2f(0,1);GL11.glVertex2d(x,y+h);GL11.glEnd();}
 static void fullscreen(){quad(-1,-1,2,2);}
 private GlPrograms(){}
}
