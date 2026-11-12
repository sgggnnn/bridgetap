package com.bridger.momentum;

import java.util.Locale;

/** Server-specific rules based on observed platform edges, not player travel. */
public final class ServerMode {
    public enum Mode{EXTRA_SHORT("Extra Short"),SHORT("Short"),NORMAL("Normal"),LONG("Long"),UNKNOWN("Unknown");public final String label;Mode(String label){this.label=label;}}
    public enum Filter{ALL("All modes"),EXTRA_SHORT("Extra Short"),SHORT("Short"),NORMAL("Normal"),LONG("Long"),UNKNOWN("Unknown mode");public final String label;Filter(String label){this.label=label;}public boolean matches(Mode mode){return this==ALL||name().equals(mode.name());}}
    public static final class Measurement{public final boolean available;public final double gap,rise;public Measurement(boolean available,double gap,double rise){if(!Double.isFinite(gap)||!Double.isFinite(rise)||gap<0||gap>1000000||Math.abs(rise)>4096)throw new IllegalArgumentException("Invalid mode measurement");this.available=available;this.gap=gap;this.rise=rise;}}
    public static final Measurement UNAVAILABLE=new Measurement(false,0,0);
    public static boolean mcplay(String server){String host=PresetServers.canonical(server);int colon=host.indexOf(':');if(colon>=0)host=host.substring(0,colon);return host.equals("mcplayhd.net")||host.endsWith(".mcplayhd.net");}
    public static Mode detect(String server,Measurement measurement){if(!mcplay(server)||measurement==null||!measurement.available)return Mode.UNKNOWN;double gap=measurement.gap;if(near(gap,16))return Mode.EXTRA_SHORT;if(near(gap,28))return Mode.SHORT;if(near(gap,51))return Mode.NORMAL;if(near(gap,83))return Mode.LONG;return Mode.UNKNOWN;}
    private static boolean near(double a,double b){return Math.abs(a-b)<.05;}
    public static Measurement measure(ReplayData data){if(data==null)return UNAVAILABLE;BridgeGeometry.Result result=data.geometry();return result.available?new Measurement(true,result.gap,result.rise):UNAVAILABLE;}
}
