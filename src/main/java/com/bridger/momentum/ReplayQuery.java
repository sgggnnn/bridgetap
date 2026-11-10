package com.bridger.momentum;

import java.text.Normalizer;
import java.util.Locale;

/** Composable library filters; search terms are ANDed and ignore accents/case. */
public final class ReplayQuery {
    public enum Status { ALL, SUCCESS, FAILED }
    public enum Axis { ALL, X, Z }
    public enum Content { ALL, REPLAYS, SUMMARIES }
    public enum Technique {
        ALL("All techniques", ""), FLAT_100("1.0 Flat", "1.0 Flat"), FLAT_105("1.05 Flat", "1.05 Flat"),
        LATE_5("5b Late", "5b Late"), LATE_6("6b Late", "6b Late"), TAP_SPEED("Tap Speed", "Tap Speed"),
        HOLD_SPEED("Hold Speed", "Hold Speed"), FLAT_4("4b Flat", "4b Flat"), TURN("Turn", "Turn");
        public final String label, match;
        Technique(String label, String match) { this.label=label; this.match=match; }
    }
    public Status status=Status.ALL;
    public Axis axis=Axis.ALL;
    public Content content=Content.ALL;
    public Technique technique=Technique.ALL;
    public String search="", minimum="", maximum="";

    public static <T extends Enum<T>> T parse(Class<T> type, String value, T fallback) {
        try { return Enum.valueOf(type,value); } catch(IllegalArgumentException|NullPointerException ex) { return fallback; }
    }
    public static ReplayQuery load() {
        ReplayQuery q=new ReplayQuery();q.search=BridgeConfig.replayQuery;
        q.status=parse(Status.class,BridgeConfig.replayStatus,Status.ALL);
        q.axis=parse(Axis.class,BridgeConfig.replayAxis,Axis.ALL);
        q.content=parse(Content.class,BridgeConfig.replayContent,Content.ALL);
        q.technique=parse(Technique.class,BridgeConfig.replayTechnique,Technique.ALL);
        q.minimum=BridgeConfig.replayMinTime;q.maximum=BridgeConfig.replayMaxTime;
        if(q.error()!=null) { q.minimum="";q.maximum=""; }
        return q;
    }
    public void store() {
        BridgeConfig.replayQuery=search;BridgeConfig.replayStatus=status.name();BridgeConfig.replayAxis=axis.name();
        BridgeConfig.replayContent=content.name();BridgeConfig.replayTechnique=technique.name();
        BridgeConfig.replayMinTime=minimum;BridgeConfig.replayMaxTime=maximum;
    }
    private static double seconds(String value) {
        if(value.trim().isEmpty())return Double.NaN;
        double result=Double.parseDouble(value.trim().replace(',', '.'));
        if(!Double.isFinite(result)||result<0)throw new IllegalArgumentException();
        return result;
    }
    public String error() {
        try {
            double min=seconds(minimum),max=seconds(maximum);
            return !Double.isNaN(min)&&!Double.isNaN(max)&&min>max?"Minimum time must not exceed maximum time.":null;
        } catch(IllegalArgumentException ex) { return "Enter a positive time in seconds, or leave the field empty."; }
    }
    public int activeFilters() { return (status!=Status.ALL?1:0)+(axis!=Axis.ALL?1:0)+(technique!=Technique.ALL?1:0)+(content!=Content.ALL?1:0)+(!minimum.isEmpty()?1:0)+(!maximum.isEmpty()?1:0); }
    public boolean matches(RunHistory.Run run,boolean playable) {
        if(status!=Status.ALL&&!status.name().equals(run.status))return false;
        if(axis!=Axis.ALL&&!axis.name().equals(run.axis))return false;
        if(content==Content.REPLAYS&&!playable||content==Content.SUMMARIES&&playable)return false;
        String details=normalize(String.join(" ",run.details));
        if(technique!=Technique.ALL&&!details.contains(normalize(technique.match)))return false;
        double min,max;try{min=seconds(minimum);max=seconds(maximum);}catch(IllegalArgumentException ex){return false;}
        if(!Double.isNaN(min)&&run.time<min||!Double.isNaN(max)&&run.time>max)return false;
        String haystack=normalize(run.name()+" "+run.id+" "+run.replayId()+" "+run.status+" "+run.axis+" "+details);
        for(String term:normalize(search).trim().split("\\s+"))if(!haystack.contains(term))return false;
        return true;
    }
    public static String normalize(String text) {
        return Normalizer.normalize(text.replaceAll("\u00a7.", ""),Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}
