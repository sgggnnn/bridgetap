package com.bridger.momentum;

import java.util.*;

/** Small, isolated 3D dioramas. No live world, entity, inventory or resource reloads. */
public final class TutorialScene {
    private static final class Box {
        final double x,y,z,w,h,d;final int color;String material="";int meta,hidden;
        Box(double x,double y,double z,double w,double h,double d,int color){this.x=x;this.y=y;this.z=z;this.w=w;this.h=h;this.d=d;this.color=color;}
    }
    private static final class Face {double[] x,y;double depth;int color;String texture;Face(double[] x,double[] y,double depth,int color,String texture){this.x=x;this.y=y;this.depth=depth;this.color=color;this.texture=texture;}}
    private final List<Box> boxes=new ArrayList<>();
    private boolean stableLesson;private double[] stableBounds;private double fitYaw=Double.NaN;private int fitLesson=-1;
    private void box(double x,double y,double z,double w,double h,double d,int color){boxes.add(new Box(x,y,z,w,h,d,color));}
    private static int rgb(int c,double brightness){return 0xFF000000|((int)(((c>>16)&255)*brightness)<<16)|((int)(((c>>8)&255)*brightness)<<8)|(int)((c&255)*brightness);}
    private static String texture(int color,boolean top){
        switch(color){
            case 0x79A58A:case 0x69A48B:return "minecraft:textures/blocks/grass_"+(top?"top":"side")+".png";
            case 0xCFB782:return "minecraft:textures/blocks/sandstone_"+(top?"top":"normal")+".png";
            case 0xB5C1CB:return "minecraft:textures/blocks/iron_block.png";
            case 0x65717D:case 0x475564:return "minecraft:textures/blocks/stone.png";
            case 0x71827A:return "minecraft:textures/blocks/log_oak"+(top?"_top":"")+".png";
            case 0x63AD85:return "minecraft:textures/blocks/leaves_oak.png";
            case 0xA68AC5:case 0xA18ABF:return "minecraft:textures/blocks/wool_colored_purple.png";
            case 0x5BA9BD:return "minecraft:textures/blocks/wool_colored_cyan.png";
            case 0x3E536D:return "minecraft:textures/blocks/wool_colored_blue.png";
            default:return "";
        }
    }
    private static String blockTexture(String key,int meta,boolean top){
        if(!key.startsWith("minecraft:"))return "";String name=key.substring(10),texture="";
        String[] woods={"oak","spruce","birch","jungle","acacia","big_oak"};
        String[] colors={"white","orange","magenta","light_blue","yellow","lime","pink","gray","silver","cyan","purple","blue","brown","green","red","black"};
        switch(name){
            case "grass":texture=top?"grass_top":"grass_side";break;
            case "sandstone":texture=top?"sandstone_top":"sandstone_normal";break;
            case "planks":texture="planks_"+woods[Math.min(5,meta&7)];break;
            case "log":texture="log_"+woods[meta&3]+(top?"_top":"");break;
            case "log2":texture="log_"+woods[4+(meta&1)]+(top?"_top":"");break;
            case "leaves":texture="leaves_"+woods[meta&3];break;
            case "wool":texture="wool_colored_"+colors[meta&15];break;
            case "stained_glass":texture="glass_"+colors[meta&15];break;
            case "prismarine":texture=meta==1?"prismarine_bricks":meta==2?"prismarine_dark":"prismarine_rough";break;
            case "stonebrick":texture="stonebrick";break;
            case "quartz_block":texture=top?"quartz_block_top":"quartz_block_side";break;
            case "stone":case "cobblestone":case "dirt":case "sand":case "glass":case "nether_brick":case "obsidian":case "gold_block":case "iron_block":texture=name;break;
        }
        return texture.isEmpty()?"":"minecraft:textures/blocks/"+texture+".png";
    }
    private void island(double x,int color,int stage){
        for(int a=0;a<3;a++)for(int z=0;z<3;z++){
            box(x+a,0,z,1,.75,1,color);
            if(stage>0&&(a==1||z==1))box(x+a,-1,z,1,1,1,0x65717D);
            if(stage>1&&a==1&&z==1)box(x+a,-2,z,1,1,1,0x475564);
        }
        box(x+2,.75,1,.85,.065,.85,0xB5C1CB);
        if(stage>1){box(x+.2,.75,.2,.35,1.1,.35,0x71827A);box(x+.05,1.5,.05,.7,.55,.7,0x63AD85);}
    }
    private void player(double x,double y,double z,double t,int color){
        box(x-.24,y+.8,z-.14,.48,.65,.28,color);box(x-.23,y+1.45,z-.23,.46,.46,.46,0xE0B798);
        box(x-.24,y+1.8,z-.24,.48,.16,.48,0x4B352D);
        box(x-.15,y+1.62,z-.244,.08,.055,.02,0x243B55);box(x+.07,y+1.62,z-.244,.08,.055,.02,0x243B55);
        box(x-.05,y+1.51,z-.25,.1,.04,.025,0xA26F5B);
        double swing=Math.sin(t*9)*.17;
        box(x-.22,y,z-.12+swing,.18,.8,.22,0x3E536D);box(x+.04,y,z-.12-swing,.18,.8,.22,0x3E536D);
        box(x-.42,y+.8,z-.12-swing,.16,.65,.2,0xE0B798);box(x+.26,y+.8,z-.12+swing,.16,.65,.2,0xE0B798);
    }
    private void folders(double t,int step,int kind){
        for(int k=0;k<3;k++){
            int color=k==1?0xF3943E:0x526A82;
            box(k*3,0,0,2.3,.18,1.8,color);box(k*3,0,1.65,2.3,1.8,.15,color);box(k*3,1.65,1.65,.9,.45,.15,color);
            for(int n=0;n<3;n++)box(k*3+.3+n*.2,.2+n*.13,.25,1.4,.12,1.1,kind==8&&k==2?0xAD5662:0xCCD9E1);
        }
        double move=(Math.sin(t*.8)+1)/2;box(1+move*4,2+Math.sin(t)*.15,.5,1.1,.12,.9,0xFFAB54);
        if(kind==7){box(3,-.35,-.6,3,.13,.15,0x90A7C0);box(3.7+move*1.8,-.25,-.65,.35,.3,.3,0x7CD7BA);}
    }
    private void build(int lesson,double t){
        boxes.clear();int step=Math.min(3,(int)(t/6));double cycle=(t%6)/6;
        if(lesson==6||lesson==7||lesson==8||lesson==21){folders(t,step,lesson);return;}
        if(lesson==9){for(int i=0;i<8;i++){double h=.8+(i%3)*.65+Math.sin(i)*.3;box(i*.8,0,0,.55,h*(.4+.6*Math.min(1,t/8)),.8,i==7?0xFF923B:0x65BFA5);box(i*.8,-.2,0,.7,.12,1,0x475461);}return;}
        if(lesson==20){for(int i=0;i<6;i++)box((i%3)*1.4,Math.sin(t*2+i)>.5?.15:0,(i/3)*1.4,1.2,.3,1.2,(int)(t*2)%6==i?0xFF923B:0x71879B);return;}
        int material=lesson==13?(step<1?0x95A0AB:step<2?0xA68AC5:0x69A48B):0x79A58A;
        island(0,material,2);island(10,material,2);
        if(lesson==11||lesson==12||lesson==13||lesson==14||lesson==15){
            for(int i=0;i<7;i++)box(3+i,.05,1,1,.55,1,0xCFB782);
            if(lesson==12){for(int i=0;i<3;i++){box(-.15,i*.6,-.15,3.3,.05,.05,0xFFAA5C);box(9.85,i*.6,-.15,3.3,.05,.05,0xFFAA5C);}}
            if(lesson==15&&step<2){island(-5,0x4A5865,1);island(15,0x4A5865,1);}
            if(lesson==14){box(5,2,0,2,.1,1.4,0xFFAA5C);box(5.2,2.15,.3,1.6,.08,.8,0xCDD4DB);}
            player(1.5,.75,1.5,0,0xD17D44);return;
        }
        if(lesson==16){for(int i=0;i<7;i++)box(3+i,0,1,1,.65,1,new int[]{0xA18ABF,0xCFB782,0x658F88,0xC58E66}[((i+step)%4)]);player(5,.65,1.5,t,0x5BA9BD);return;}
        if(lesson==2){double move=step<2?cycle*2:2;box(3+move,2.5,0,2,.1,.9,0xFF963D);box(3.2+move,2.6,.2,1.6,.06,.12,0xE9EFF5);box(3.2+move,2.6,.5,1.1,.06,.12,0x81D3B1);}
        if(lesson==4){for(int i=0;i<4;i++)box(4+(i%2)*.7,1.5,(i/2)*.7,.55,.15,.55,(int)(t*2)%4==i?0xFF963D:0x73879B);}
        int placed=Math.min(7,Math.max(0,(int)(t*1.15)));
        for(int i=0;i<placed;i++)box(3+i,.05,1,1,.55,1,lesson==10&&i==6?0xCD6571:0xCFB782);
        double travel=Math.min(9,t*.6),jump=Math.max(0,Math.sin(t*3.1))*.75;
        if(lesson==10)travel=Math.min(7.2,travel);
        player(1.5+travel,.75+jump,1.5,t,lesson==3?0x9B89D1:0x5BA9BD);
        if(lesson==5)for(int i=0;i<4;i++)box(3+i*2,1.3,2.3,.15,.65,.15,0xFF963D);
        if(lesson==17){for(int i=0;i<12;i++)box(i,-1,4,1,5,.1,step<2?0x35536C:0x202D52);box(7,2.3,3.8,.75,.75,.18,step<2?0xFFD791:0xE0E8FF);}
        if(lesson==18||lesson==19){for(int i=0;i<3;i++){box(3+i*2.3,-1.3,-1.2,1.8,.1,1.1,0xBBCAD8);box(3.2+i*2.3,-1.15,-1,1.4,.05,.65,0x527A88);}}
        if(lesson==1){box(1.5+travel,-.2,1.5,.08,.08,2,0xFF963D);box(1.5+travel,-.2,1.5,1.5,.08,.08,0x79C6A0);}
    }
    public static boolean spatial(int lesson){return lesson==0||lesson==1||lesson==3||lesson==10||(lesson>=11&&lesson<=17)||lesson==19;}
    private static void diagram(UiCanvas ui,int lesson,double time,int x,int y,int width,int height){
        int active=Math.min(3,(int)(time/6)),accent=UiTheme.accent();
        ui.roundedRect(x,y,width,height,12,UiTheme.alpha(UiTheme.RAISED,.85));
        if(lesson==5){
            ui.text("REPLAY TIMELINE",x+24,y+22,UiTheme.SECONDARY);
            int left=x+32,right=x+width-32,line=y+height/2;
            ui.roundedRect(left,line,right-left,4,2,UiTheme.BORDER);
            for(int i=0;i<4;i++){int px=left+(right-left)*i/3;ui.roundedRect(px-6,line-4,12,12,6,i==active?accent:UiTheme.SECONDARY);ui.text("Jump "+(i+1),px-20,line+24,UiTheme.TEXT);}
            ui.text("Choose a jump marker to inspect that exact tick.",x+24,y+height-32,UiTheme.TEXT);return;
        }
        if(lesson==4||lesson==20){
            ui.text(lesson==4?"RECORDED INPUTS":"KEYBIND PROFILES",x+24,y+20,UiTheme.SECONDARY);
            String[] keys={"W","A","S","D","LMB","RMB"};
            for(int i=0;i<6;i++){int xx=x+32+i*(width-64)/6;boolean pressed=(i%4)==active;ui.roundedRect(xx,y+62,(width-84)/6,48,8,pressed?accent:UiTheme.PANEL);ui.text(keys[i],xx+12,y+80,pressed?UiTheme.onAccent(accent):UiTheme.TEXT);}
            ui.text(lesson==4?"Highlighted keys show recorded inputs, not your current keys.":"Save controls / Name profiles / Apply / Cycle with a shortcut",x+24,y+height-35,UiTheme.TEXT);return;
        }
        String[] titles;
        switch(lesson){
            case 2:titles=new String[]{"Select HUD","Drag to move","Resize","Choose colors"};break;
            case 6:titles=new String[]{"Server folder","Choose mode","Filter runs","Play replay"};break;
            case 7:titles=new String[]{"Select replay","Copy code","Paste code","Confirm import"};break;
            case 8:titles=new String[]{"Recent runs","Save favorites","Set retention","Clean old fails"};break;
            case 9:titles=new String[]{"Select server","Select mode","Choose period","Read averages"};break;
            case 22:titles=new String[]{"Select pack","Choose textures","Preview colors","Create overlay"};break;
            case 18:titles=new String[]{"Set up tools","Choose FPS","Enable buffer","Save clip"};break;
            default:titles=new String[]{"Check update","Review version","Install","Restart game"};break;
        }
        ui.text("WORKFLOW / STEP "+(active+1),x+24,y+22,UiTheme.SECONDARY);
        for(int i=0;i<4;i++){
            int xx=x+22+i*(width-38)/4,ww=(width-54)/4;boolean selected=i==active;
            ui.glassRect(xx,y+65,ww,88,10,selected?UiTheme.alpha(accent,.18):UiTheme.PANEL,selected?accent:UiTheme.BORDER,.02f);
            ui.text("0"+(i+1),xx+12,y+78,selected?accent:UiTheme.SECONDARY);ui.text(titles[i],xx+12,y+115,UiTheme.TEXT);
        }
        ui.text("Follow the steps below, then use Try it to open the feature.",x+24,y+height-28,UiTheme.SECONDARY);
    }
    public void paint(UiCanvas ui,int lesson,double time,double yaw,double zoom,int x,int y,int width,int height){
        if(!spatial(lesson)){diagram(ui,lesson,time,x,y,width,height);return;}
        stableLesson=true;
        if(fitLesson!=lesson||fitYaw!=yaw){
            double[] bounds={Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};double sn=Math.sin(yaw),cs=Math.cos(yaw);
            for(double sample:new double[]{0,3,6,9,12,15,18,21,23.99}){
                build(lesson,sample);for(Box b:boxes)for(int i=0;i<8;i++){
                    double wx=b.x+((i&1)!=0?b.w:0),wy=b.y+((i&2)!=0?b.h:0),wz=b.z+((i&4)!=0?b.d:0),rx=wx*cs-wz*sn,ry=(wx*sn+wz*cs)*.48-wy;
                    bounds[0]=Math.min(bounds[0],rx);bounds[1]=Math.min(bounds[1],ry);bounds[2]=Math.max(bounds[2],rx);bounds[3]=Math.max(bounds[3],ry);
                }
            }
            stableBounds=bounds;fitLesson=lesson;fitYaw=yaw;
        }
        build(lesson,time);render(ui,yaw,zoom,x,y,width,height);
    }
    public void paintIsland(UiCanvas ui,IslandTemplate island,int x,int y,int width,int height){
        stableLesson=false;boxes.clear();Set<String> occupied=new HashSet<>();for(IslandTemplate.Block b:island.blocks)occupied.add(b.x+":"+b.y+":"+b.z);
        int[][] offsets={{0,0,-1},{0,0,1},{-1,0,0},{1,0,0},{0,1,0}};
        for(IslandTemplate.Block block:island.blocks){int mask=0;for(int i=0;i<offsets.length;i++){int[] d=offsets[i];if(occupied.contains((block.x+d[0])+":"+(block.y+d[1])+":"+(block.z+d[2])))mask|=1<<i;}if(mask==31)continue;
            box(block.x,block.y,block.z,1,1,1,IslandPreview.material(block.key,block.meta));Box b=boxes.get(boxes.size()-1);b.material=block.key;b.meta=block.meta;b.hidden=mask;}

        render(ui,-.65,.92,x,y,width,height);
    }
    private void render(UiCanvas ui,double yaw,double zoom,int x,int y,int width,int height){
        List<Face> faces=new ArrayList<>();double sin=Math.sin(yaw),cos=Math.cos(yaw);
        double minX=1e9,minY=1e9,maxX=-1e9,maxY=-1e9;
        for(Box b:boxes){
            double[][] p=new double[8][3];
            for(int i=0;i<8;i++){double wx=b.x+((i&1)!=0?b.w:0),wy=b.y+((i&2)!=0?b.h:0),wz=b.z+((i&4)!=0?b.d:0);
                double rx=wx*cos-wz*sin,rz=wx*sin+wz*cos;p[i]=new double[]{rx,rz*.48-wy,rz+wy*.48};minX=Math.min(minX,p[i][0]);maxX=Math.max(maxX,p[i][0]);minY=Math.min(minY,p[i][1]);maxY=Math.max(maxY,p[i][1]);}
            int[][] sides={{0,1,3,2},{4,6,7,5},{0,2,6,4},{1,5,7,3},{2,3,7,6}};
            for(int side=0;side<sides.length;side++){if((b.hidden&(1<<side))!=0||(side==0&&cos>=0)||(side==1&&cos<=0)||(side==2&&sin>=0)||(side==3&&sin<=0))continue;double[] px=new double[4],py=new double[4];double depth=0;
                for(int i=0;i<4;i++){double[] v=p[sides[side][i]];px[i]=v[0];py[i]=v[1];depth+=v[2];}
                String texture=b.material.isEmpty()?texture(b.color,side==4):blockTexture(b.material,b.meta,side==4);int tint=texture.isEmpty()?b.color:(b.color==0x79A58A||b.color==0x69A48B||b.material.equals("minecraft:grass"))&&side==4?0x93C66B:texture.contains("leaves_")?0x78B85A:0xFFFFFF;
                faces.add(new Face(px,py,depth/4,rgb(tint,side==4?1:side<2?.80:.63),texture));}
        }
        if(stableLesson&&stableBounds!=null){minX=stableBounds[0];minY=stableBounds[1];maxX=stableBounds[2];maxY=stableBounds[3];}
        double unit=Math.min((width-28)/Math.max(1,maxX-minX),(height-28)/Math.max(1,maxY-minY))*zoom;
        final double cx=(minX+maxX)/2,cy=(minY+maxY)/2;
        faces.sort(Comparator.comparingDouble((Face f)->f.depth));
        // Soft contact shadow keeps the diorama grounded without editing a world.
        for(int ring=4;ring>=1;ring--){double[] sx=new double[24],sy=new double[24];for(int i=0;i<24;i++){double angle=i*Math.PI/12;sx[i]=x+width*.5+Math.cos(angle)*width*(.21+ring*.012);sy[i]=y+height*.78+Math.sin(angle)*height*(.075+ring*.005);}ui.polygon(sx,sy,0x0B000000);}
        for(Face face:faces){for(int i=0;i<4;i++){face.x[i]=x+width*.5+(face.x[i]-cx)*unit;face.y[i]=y+height*.5+(face.y[i]-cy)*unit;}if(face.texture.isEmpty())ui.polygon(face.x,face.y,face.color);else ui.texturedPolygon(face.x,face.y,face.texture,face.color);}
    }
}
