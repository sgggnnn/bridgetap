package com.bridger.momentum;

/** Small, independently testable rules; ambiguous input is left unlabelled. */
public final class JumpSignals {
    private JumpSignals() {}
    public static String gain(boolean forward, boolean left, boolean right, boolean back) {
        if (!forward || back) return null;
        return left == right ? "Gain" : "No Gain";
    }
    public static boolean isUpwardTeleport(double previousY, double currentY) {
        return currentY - previousY > 1.5;
    }
}
