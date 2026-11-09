package com.bridger.momentum;

import net.minecraft.unmapped.C_25287165;
import net.minecraft.unmapped.C_30593188;
import net.minecraft.unmapped.C_73166169;
import net.minecraft.unmapped.C_97595263;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreboardHelper {
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+):(\\d+(?:\\.\\d+)?)");
    private static final Pattern SECONDS_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)");

    public static String clean(String text) {
        if (text == null) return "";
        return text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").trim();
    }

    public static double parseTime(String text) {
        if (text == null) return -1.0;
        String clean = clean(text);

        // Try MM:SS.xxx
        Matcher mTime = TIME_PATTERN.matcher(clean);
        if (mTime.find()) {
            try {
                double min = Double.parseDouble(mTime.group(1));
                double sec = Double.parseDouble(mTime.group(2));
                return min * 60.0 + sec;
            } catch (Exception ignored) {}
        }

        // Try SS.xxx
        Matcher mSec = SECONDS_PATTERN.matcher(clean);
        if (mSec.find()) {
            try {
                return Double.parseDouble(mSec.group(1));
            } catch (Exception ignored) {}
        }

        return -1.0;
    }

    public static double readScoreboardTime(C_73166169 scoreboard) {
        if (scoreboard == null) return -1.0;

        // Display slot 1 is the sidebar
        C_30593188 objective = scoreboard.m_63794212(1);
        if (objective == null) return -1.0;

        Collection<C_25287165> scores = scoreboard.m_58722273(objective);
        if (scores == null || scores.isEmpty()) return -1.0;

        List<C_25287165> scoreList = new ArrayList<>(scores);
        // Sort descending by score points (top line to bottom line)
        Collections.sort(scoreList, new Comparator<C_25287165>() {
            @Override
            public int compare(C_25287165 a, C_25287165 b) {
                return Integer.compare(b.m_68168285(), a.m_68168285());
            }
        });

        boolean nextIsTime = false;
        for (C_25287165 s : scoreList) {
            String pName = s.m_94164059();
            C_97595263 team = scoreboard.m_42132668(pName);
            String fullLine = C_97595263.m_12027240(team, pName);
            String cleanLine = clean(fullLine);

            if (nextIsTime) {
                double parsed = parseTime(cleanLine);
                if (parsed >= 0.0) {
                    return parsed;
                }
                nextIsTime = false;
            }

            if (cleanLine.equalsIgnoreCase("Time") || cleanLine.startsWith("Time")) {
                // If "Time: 3.000s" is on the same line
                if (cleanLine.contains(":") || cleanLine.matches(".*\\d+.*")) {
                    double parsed = parseTime(cleanLine.substring(4));
                    if (parsed >= 0.0) return parsed;
                }
                // Otherwise the next line in the sidebar contains the time digits
                nextIsTime = true;
            }
        }

        return -1.0;
    }

    public static int readScoreboardBlocks(C_73166169 scoreboard) {
        if (scoreboard == null) return -1;
        C_30593188 objective = scoreboard.m_63794212(1);
        if (objective == null) return -1;

        Collection<C_25287165> scores = scoreboard.m_58722273(objective);
        if (scores == null || scores.isEmpty()) return -1;

        List<C_25287165> scoreList = new ArrayList<>(scores);
        Collections.sort(scoreList, new Comparator<C_25287165>() {
            @Override
            public int compare(C_25287165 a, C_25287165 b) {
                return Integer.compare(b.m_68168285(), a.m_68168285());
            }
        });

        boolean nextIsBlocks = false;
        for (C_25287165 s : scoreList) {
            String pName = s.m_94164059();
            C_97595263 team = scoreboard.m_42132668(pName);
            String fullLine = C_97595263.m_12027240(team, pName);
            String cleanLine = clean(fullLine);

            if (nextIsBlocks) {
                try {
                    Matcher m = Pattern.compile("(\\d+)").matcher(cleanLine);
                    if (m.find()) {
                        return Integer.parseInt(m.group(1));
                    }
                } catch (Exception ignored) {}
                nextIsBlocks = false;
            }

            if (cleanLine.equalsIgnoreCase("Blocks") || cleanLine.startsWith("Blocks")) {
                if (cleanLine.matches(".*\\d+.*")) {
                    Matcher m = Pattern.compile("(\\d+)").matcher(cleanLine);
                    if (m.find()) return Integer.parseInt(m.group(1));
                }
                nextIsBlocks = true;
            }
        }

        return -1;
    }
}
