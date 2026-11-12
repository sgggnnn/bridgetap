package com.bridger.momentum;

import net.minecraft.unmapped.C_85740840;

/** Component picker, actual color swatches and a live HUD preview. */
public final class BridgeColorsScreen extends BridgeScreen {
    private static final int[] PALETTE={0x000000,0x0000AA,0x00AA00,0x00AAAA,0xAA0000,0xAA00AA,0xFFAA00,0xAAAAAA,0x555555,0x5555FF,0x55FF55,0x55FFFF,0xFF5555,0xFF55FF,0xFFFF55,0xFFFFFF};
    private static final String[] NAMES={"Black","Dark blue","Dark green","Dark aqua","Dark red","Dark purple","Gold","Gray","Dark gray","Blue","Green","Aqua","Red","Pink","Yellow","White"};
    private static final String[] DEFAULTS={"b","e","a","c","7","a","e","b","0","7","7","a","c","7"};
    private final C_85740840 parent;
    private int part;
    public BridgeColorsScreen(){this(new BridgeSettingsScreen());}
    public BridgeColorsScreen(C_85740840 parent){this.parent=parent;}
    @Override protected void paint(){
        header("Overlay colors","Choose an element, then a color");
        panel(20,72,250,358);panel(286,72,554,358);
        text("HUD ELEMENTS",36,88,TEXT);
        for(int i=0;i<BridgeConfig.colors.length;i++){
            final int index=i;int y=111+i*22;
            if(part==i||over(28,y-4,234,21))rect(28,y-4,234,21,part==i?0xFF254050:RAISED);
            rect(38,y+2,12,12,0xFF000000|BridgeConfig.rgb(i));
            text(BridgeConfig.COLOR_NAMES[i],60,y+4,part==i?ACCENT:TEXT);
            if(part==i)check(243,y+4,ACCENT);
            region(28,y-4,234,21,()->part=index);
        }
        text(BridgeConfig.COLOR_NAMES[part],302,88,TEXT);
        int current=Integer.parseInt(BridgeConfig.colors[part],16);
        label("Current: "+NAMES[current]+"  /  Minecraft color "+BridgeConfig.colors[part].toUpperCase(java.util.Locale.ROOT),302,108);
        for(int c=0;c<16;c++){
            final int code=c;int x=306+(c%8)*55,y=139+(c/8)*52;
            if(c==current){rect(x-4,y-4,40,40,ACCENT);rect(x-2,y-2,36,36,TEXT);}
            else if(over(x-2,y-2,36,36))rect(x-2,y-2,36,36,MUTED);
            rect(x,y,32,32,0xFF000000|PALETTE[c]);
            if(c==current)check(x+12,y+12,c>=10||c==6||c==7?0xFF101722:0xFFFFFFFF);
            region(x-4,y-4,40,40,()->{OverlayStyle.resetPart(part);BridgeConfig.colors[part]=Integer.toHexString(code);BridgeConfig.save();});
            if(over(x-4,y-4,40,40))label(NAMES[c],302,239);
        }
        label("LIVE PREVIEW",302,265);
        rect(302,284,522,130,0xFF000000|BridgeConfig.rgb(9));
        rect(304,286,518,126,0xE0000000|BridgeConfig.rgb(8));
        text(BridgeConfig.color(7)+"Success 3.85s  /  7 jumps  /  Axis Z",316,299,TEXT);
        sample("1.0 Flat", ".504", "+.n/a", "3t",1,321);
        sample("Tap Speed #1", ".460", "-.044", "9t",-1,342);
        text(BridgeConfig.color(10)+"Velo: /123",650,342,TEXT);
        text(BridgeConfig.color(13)+"[Yaw: 135.0\u00b0]",650,363,TEXT);
        sample("Turn", ".216", "+.475", "15t",1,363);
        text(BridgeConfig.color(1)+"X .584 Z .289  "+BridgeConfig.color(11)+"[\u0394: +.295]  "+BridgeConfig.color(12)+"[\u0394: -.295]",316,384,TEXT);
        button("Reset selected",302,442,128,()->{OverlayStyle.resetPart(part);BridgeConfig.colors[part]=DEFAULTS[part];BridgeConfig.save();});
        button("Reset all",442,442,96,()->{for(int i=0;i<DEFAULTS.length;i++)OverlayStyle.resetPart(i);System.arraycopy(DEFAULTS,0,BridgeConfig.colors,0,DEFAULTS.length);BridgeConfig.save();});
        button(parent instanceof BridgeLayoutScreen?"Back to layout":"Back to settings",700,442,140,26,true,true,this::back);
        footer("Preview updates immediately  /  Esc: back");
    }
    private void sample(String label,String coord,String delta,String ticks,int sign,int y){
        text(BridgeConfig.color(0)+"> "+label+": "+BridgeConfig.color(1)+coord+"  "+BridgeConfig.color(sign<0?3:2)+delta+"  "+BridgeConfig.color(4)+"("+ticks+")",316,y,TEXT);
    }
    @Override protected void back(){BridgeConfig.save();open(parent);}
}
