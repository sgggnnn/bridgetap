package com.bridger.momentum;

/** Small geometric line icons, independent of font glyph coverage. */
public final class MenuIcons {
    public static void paint(UiCanvas ui,String key,int x,int y,int color){
        if(key.equals("folders")||key.equals("share")||key.equals("storage")){
            ui.rect(x,y+9,40,27,0xFF8E653B);ui.rect(x+2,y+4,16,8,color);ui.rect(x,y+13,40,23,color);
            if(key.equals("share")){ui.rect(x+11,y+23,18,3,0xFF29241E);ui.polygon(new double[]{x+24,x+24,x+32},new double[]{y+18,y+30,y+24},0xFF29241E);}
        }else if(key.equals("stats")){
            for(int i=0;i<4;i++)ui.rect(x+i*11,y+28-i*7,7,12+i*7,color);
        }else if(key.equals("bookmark")){
            ui.polygon(new double[]{x+6,x+34,x+34,x+20,x+6},new double[]{y,y,y+40,y+29,y+40},color);
        }else if(key.equals("keys")){
            for(int i=0;i<4;i++){int a=i==0?14:(i-1)*14,b=i==0?0:15;ui.rect(x+a,y+b,12,12,color);}
        }else if(key.equals("islands")||key.equals("presets")||key.equals("blocks")){
            ui.polygon(new double[]{x,x+20,x+40,x+20},new double[]{y+10,y,y+10,y+20},color);
            ui.polygon(new double[]{x,x+18,x+18,x},new double[]{y+13,y+23,y+40,y+30},0xFF9E612E);
            ui.polygon(new double[]{x+22,x+40,x+40,x+22},new double[]{y+23,y+13,y+30,y+40},0xFFC57936);
        }else if(key.equals("replays")||key.equals("clips")){
            ui.rect(x,y,40,2,color);ui.rect(x,y+30,40,2,color);ui.rect(x,y,2,32,color);ui.rect(x+38,y,2,32,color);
            ui.polygon(new double[]{x+16,x+16,x+28},new double[]{y+8,y+24,y+16},color);
        }else if(key.equals("sky")){
            for(int row=0;row<28;row++){int inset=(int)Math.abs(row-14)*2/3;ui.rect(x+inset,y+row,30-inset*2,1,color);}ui.rect(x+34,y+4,4,4,0xFFE8EDF5);
        }else{
            for(int i=0;i<3;i++){ui.rect(x,y+6+i*12,40,2,0xFF8A969E);ui.rect(x+8+(i%2)*16,y+i*12,7,14,color);}
        }
    }
    private MenuIcons(){}
}
