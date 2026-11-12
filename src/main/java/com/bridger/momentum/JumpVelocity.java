package com.bridger.momentum;

import java.util.Locale;
/** Position at the start of a client tick; a jump at t reads only t - 1. */
public final class JumpVelocity {
    public static final class Sample {public final int tick;public final double x,y,z;Sample(int tick,double x,double y,double z){this.tick=tick;this.x=x;this.y=y;this.z=z;}public double horizontal(){return Math.hypot(x,z);}}
    private final Sample[] samples=new Sample[128];
    public void record(int tick,double x,double y,double z){if(Double.isFinite(x)&&Double.isFinite(y)&&Double.isFinite(z))samples[Math.floorMod(tick,samples.length)]=new Sample(tick,x,y,z);}
    public Sample beforeJump(int tick){Sample sample=samples[Math.floorMod(tick-1,samples.length)];return sample!=null&&sample.tick==tick-1?sample:null;}
    public void clear(){java.util.Arrays.fill(samples,null);}
    public static String decimalZ(Sample sample){return sample==null?"n/a":decimalZ(sample.z);}
    public static String decimalZ(double z){return String.format(Locale.US,".%03d",Math.floorMod((int)Math.round((Math.abs(z)-Math.floor(Math.abs(z)))*1000),1000));}
    public static String format(Sample sample){return "Velo Z (-1t): "+decimalZ(sample);}
}
