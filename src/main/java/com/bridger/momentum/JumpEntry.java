package com.bridger.momentum;

import java.util.Locale;

public class JumpEntry {
    public enum EntryType {
        FLAT,
        TAP,
        FAIL_TAP,
        TURN,
        HEAD_HITTER,
        PLATES_MINUS_1,
        PLATES
    }

    private String label;
    private final double coordinate;
    private final double decimal;
    private final double delta;
    private final int ticksElapsed;
    private final EntryType type;
    private String gainType;

    public JumpEntry(String label, double coordinate, double decimal, double delta, int ticksElapsed, EntryType type) {
        this.label = label;
        this.coordinate = coordinate;
        this.decimal = decimal;
        this.delta = delta;
        this.ticksElapsed = ticksElapsed;
        this.type = type;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public double getCoordinate() {
        return coordinate;
    }

    public double getDecimal() {
        return decimal;
    }

    public double getDelta() {
        return delta;
    }

    public int getTicksElapsed() {
        return ticksElapsed;
    }

    public EntryType getType() {
        return type;
    }

    public String getGainType() {
        return gainType;
    }

    public void setGainType(String gainType) {
        this.gainType = gainType;
    }

    public String getFormattedLine() {
        StringBuilder sb = new StringBuilder();
        int decInt = (int) Math.round(decimal * 1000.0) % 1000;
        if (decInt < 0) decInt += 1000;
        String decStr = String.format(Locale.US, "\u00a7e.%03d", decInt);

        switch (type) {
            case FLAT:
                // » 1.05 Flat: .504 +.n/a (4t)
                sb.append("\u00a7b\u00bb \u00a7b").append(label).append(": ")
                  .append(decStr).append(" \u00a7a+.n/a \u00a77(")
                  .append(ticksElapsed).append("t)");
                break;

            case TAP:
                // » Tap #1: .460 -.044 (9t)
                sb.append("\u00a7b\u00bb \u00a7b").append(label).append(": ")
                  .append(decStr).append(" ")
                  .append(formatDelta(delta)).append(" \u00a77(")
                  .append(ticksElapsed).append("t)");
                break;

            case FAIL_TAP:
                // » Fail Tap: .680 -.011 (9t)
                sb.append("\u00a7c\u00bb \u00a7cFail Tap: ")
                  .append(decStr).append(" ")
                  .append(formatDelta(delta)).append(" \u00a77(")
                  .append(ticksElapsed).append("t)");
                break;

            case TURN:
                // » Turn: .216 +.475 (15t)
                sb.append("\u00a7f\u00bb \u00a7dTurn: ")
                  .append(decStr).append(" ")
                  .append(formatDelta(delta)).append(" \u00a77(")
                  .append(ticksElapsed).append("t)");
                break;

            case HEAD_HITTER:
                sb.append("§b» §bHead hitter: ").append(decStr);
                break;

            case PLATES_MINUS_1:
                // » Plates-1t: .260
                sb.append("\u00a7b\u00bb \u00a7bPlates-1t: ")
                  .append(decStr);
                break;

            case PLATES:
                // » Plates: .102
                sb.append("\u00a76\u00bb \u00a76Plates: ")
                  .append(decStr);
                break;
        }

        if (gainType != null) sb.append(" §7").append(gainType);
        return sb.toString();
    }

    private String formatDelta(double d) {
        if (Double.isNaN(d)) {
            return "\u00a7a+.n/a";
        }
        if (d >= 0.0) {
            int dInt = (int) Math.round(d * 1000.0);
            return String.format(Locale.US, "\u00a7a+.%03d", dInt);
        } else {
            int dInt = (int) Math.round(Math.abs(d) * 1000.0);
            return String.format(Locale.US, "\u00a7c-.%03d", dInt);
        }
    }
}
