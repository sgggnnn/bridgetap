package com.bridger.momentum;

import java.util.Locale;

/** Timing and launch geometry rules shared by the tracker and regression tests. */
public final class BridgeDetection {
    private BridgeDetection() {}

    public static String flatLabel(int ticks, double duration) {
        if (ticks >= 11 && ticks <= 12) return "4b Flat";
        if (ticks == 3) return "1.0 Flat";
        if (ticks == 4) return "1.05 Flat";
        if (ticks == 5) return "1.1 Flat";
        if (ticks == 6) return duration >= 1.175 ? "1.2 Flat" : "1.15 Flat";
        if (duration <= 1.075) return "1.05 Flat";
        if (duration <= 1.125) return "1.1 Flat";
        if (duration <= 1.175) return "1.15 Flat";
        if (duration <= 1.225) return "1.2 Flat";
        return String.format(Locale.US, "%.2f Flat", duration);
    }

    public static String lateLabel(int jumpIndex, int openingTicks, int elapsedTicks,
            double fromX, double fromY, double fromZ, double toX, double toY, double toZ,
            boolean xAxis, boolean placements) {
        if (jumpIndex != 1 || elapsedTicks != 9 || !placements) return null;
        if (openingTicks != 3 && openingTicks != 4) return null;
        if (Math.abs((toY - fromY) - 1.0) > 0.12) return null;
        double from = xAxis ? fromX : fromZ, to = xAxis ? toX : toZ;
        double lateral = xAxis ? toZ - fromZ : toX - fromX;
        // Three axial block columns and one block up distinguish a late opening
        // from a two-column tap speed. Floor is deliberate for negative axes.
        if (Math.abs(Math.floor(to) - Math.floor(from)) != 3 || Math.abs(lateral) > 1.0) return null;
        return openingTicks == 3 ? "5b Late" : "6b Late";
    }
}
