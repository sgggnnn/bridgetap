package com.bridger.momentum;

import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_32594356;
import net.minecraft.unmapped.C_42232651;
import net.minecraft.unmapped.C_76792751;
import net.minecraft.unmapped.C_57778754;
import net.minecraft.unmapped.C_81592558;
import net.minecraft.unmapped.C_69136080;
import net.minecraft.unmapped.C_82690114;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BridgeTracker {
    public enum RunStatus {
        IDLE,
        BRIDGING,
        SUCCESS,
        FAILED
    }

    private static RunStatus status = RunStatus.IDLE;
    private static boolean isBridging = false;
    private static double currentBridgeTime = 0.0;
    private static String currentAxis = "Z";
    private static int jumpCount = 0;

    private static int startTick = 0;
    private static int currentTick = 0;
    private static final RunHistory HISTORY = new RunHistory(java.nio.file.Paths.get("config", "bridgetap", "saved"));
    private static boolean historyCaptured = true;
    public static RunHistory history() { return HISTORY; }
    private static void captureFinishedRun() {
        if (historyCaptured || isBridging || pendingPlateSample || !pendingJumps.isEmpty() || (status != RunStatus.SUCCESS && status != RunStatus.FAILED)) return;
        java.util.List<String> lines = new java.util.ArrayList<>();
        for (JumpEntry entry : entries) lines.add(entry.getFormattedLine());
        HISTORY.add(status.name(), currentBridgeTime, getJumpCount(), currentAxis, lines, ReplayRecorder.snapshot());
        if (BridgeConfig.chatOutput) for (JumpEntry entry : entries) {
            String line=EntryFormatter.format(entry);
            if(!line.isEmpty())sendMessage("\u00a7b[BridgeTap] " + line);
        }
        historyCaptured = true;
    }

    private static int lastJumpTick = 0;
    private static int lastJumpSignalTick = Integer.MIN_VALUE;
    private static final int POSITION_HISTORY_SIZE = 81;
    private static final int[] positionHistoryTicks = new int[POSITION_HISTORY_SIZE];
    private static final double[] positionHistoryX = new double[POSITION_HISTORY_SIZE];
    private static final double[] positionHistoryZ = new double[POSITION_HISTORY_SIZE];
    private static final boolean[] positionHistoryValid = new boolean[POSITION_HISTORY_SIZE];
    private static final int[] movementHistoryTicks = new int[POSITION_HISTORY_SIZE];
    private static final boolean[] movementHistoryForward = new boolean[POSITION_HISTORY_SIZE];
    private static final boolean[] movementHistoryLeft = new boolean[POSITION_HISTORY_SIZE];
    private static final boolean[] movementHistoryRight = new boolean[POSITION_HISTORY_SIZE];
    private static final boolean[] movementHistoryValid = new boolean[POSITION_HISTORY_SIZE];
    private static double previousY;
    private static double previousVelocityY;
    private static long runStartNanos;
    private static final String[] jumpInputHistory = new String[POSITION_HISTORY_SIZE];
    private static final int[] jumpInputTicks = new int[POSITION_HISTORY_SIZE];
    private static final boolean[] jumpInputValid = new boolean[POSITION_HISTORY_SIZE];
    private static double lastJumpCoord = 0.0;
    private static double lastJumpDecimal = 0.0;

    private static double prevTickCoord = 0.0;
    private static double lastTickCoord = 0.0;

    private static double startX = 0.0;
    private static double startY = 0.0;
    private static double startZ = 0.0;

    private static double flatDuration = 1.05;
    private static boolean flatFinalized = false;
    private static boolean turnRecorded = false;
    private static boolean platesRecorded = false;

    private static int groundTicksAfterLanding = 0;
    private static boolean wasInAir = false;

    private static final List<JumpEntry> entries = new ArrayList<>();
    private static final List<PendingJump> pendingJumps = new ArrayList<>();
    private static final List<PendingGainSample> pendingGainSamples = new ArrayList<>();
    private static boolean pendingPlateSample = false;
    private static int pendingPlateTick = 0;

    private static final class PendingGainSample {
        private final JumpEntry entry;
        private final int inputTick;

        private PendingGainSample(JumpEntry entry, int inputTick) {
            this.entry = entry;
            this.inputTick = inputTick;
        }
    }

    private static final class PendingJump {
        private final int sampleTick;
        private final int jumpTick;
        private final double flatTime;

        private PendingJump(int sampleTick, int jumpTick, double flatTime) {
            this.sampleTick = sampleTick;
            this.jumpTick = jumpTick;
            this.flatTime = flatTime;
        }
    }

    public static synchronized List<JumpEntry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public static boolean isBridging() {
        return isBridging;
    }

    public static RunStatus getStatus() {
        return status;
    }

    public static double getCurrentBridgeTime() {
        return currentBridgeTime;
    }

    public static int getJumpCount() {
        return entries.isEmpty() ? 0 : jumpCount + 1; // Flat (jump 0) + Taps
    }

    public static String getCurrentAxis() {
        return currentAxis;
    }

    public static int bridgetap$getTick() { return currentTick; }

    public static synchronized void reset() {
        ReplayRecorder.reset();
        PlacementEvidence.reset();
        status = RunStatus.IDLE;
        isBridging = false;
        currentBridgeTime = 0.0;
        jumpCount = 0;
        startTick = 0;
        lastJumpTick = 0;
        lastJumpCoord = 0.0;
        lastJumpDecimal = 0.0;
        flatFinalized = false;
        flatDuration = 1.05;
        turnRecorded = false;
        platesRecorded = false;
        groundTicksAfterLanding = 0;
        wasInAir = false;
        entries.clear();
        pendingJumps.clear();
        pendingGainSamples.clear();
        pendingPlateSample = false;
    }

    private static void savePositionSnapshot(C_57778754 player, int tick) {
        int index = Math.floorMod(tick, POSITION_HISTORY_SIZE);
        positionHistoryTicks[index] = tick;
        positionHistoryX[index] = player.f_78826675;
        positionHistoryZ[index] = player.f_97691941;
        positionHistoryValid[index] = true;
    }

    private static void saveMovementSnapshot(Minecraft mc, int tick) {
        C_76792751 settings = mc.f_58088711;
        if (settings == null) return;
        int index = Math.floorMod(tick, POSITION_HISTORY_SIZE);
        movementHistoryTicks[index] = tick;
        movementHistoryForward[index] = settings.f_15607202.m_70603120();
        movementHistoryLeft[index] = settings.f_20678369.m_70603120();
        movementHistoryRight[index] = settings.f_25268871.m_70603120();
        movementHistoryValid[index] = true;
    }

    private static boolean hasMovementSnapshot(int tick) {
        int index = Math.floorMod(tick, POSITION_HISTORY_SIZE);
        return movementHistoryValid[index] && movementHistoryTicks[index] == tick;
    }

    private static String gainTypeAtTick(int tick) {
        if (!hasMovementSnapshot(tick)) return null;
        int index = Math.floorMod(tick, POSITION_HISTORY_SIZE);
        boolean forward = movementHistoryForward[index];
        boolean left = movementHistoryLeft[index];
        boolean right = movementHistoryRight[index];
        if (forward && left && right) return "Gain";
        if (forward && left != right) return "No Gain";
        return null;
    }

    private static void attachGainTypeSample(JumpEntry entry, int jumpTick) {
        int inputTick = jumpTick + 1;
        String gainType = gainTypeAtTick(inputTick);
        if (gainType != null) {
            entry.setGainType(gainType);
        } else if (inputTick > currentTick) {
            pendingGainSamples.add(new PendingGainSample(entry, inputTick));
        }
    }

    private static void processPendingGainSamples() {
        for (int i = 0; i < pendingGainSamples.size();) {
            PendingGainSample pending = pendingGainSamples.get(i);
            if (hasMovementSnapshot(pending.inputTick)) {
                pending.entry.setGainType(gainTypeAtTick(pending.inputTick));
                pendingGainSamples.remove(i);
            } else if (currentTick - pending.inputTick >= POSITION_HISTORY_SIZE) {
                pendingGainSamples.remove(i);
            } else {
                i++;
            }
        }
    }

    private static boolean hasPositionSnapshot(int tick) {
        int index = Math.floorMod(tick, POSITION_HISTORY_SIZE);
        return positionHistoryValid[index] && positionHistoryTicks[index] == tick;
    }

    private static double coordinateAtTick(int tick) {
        if (!hasPositionSnapshot(tick)) return Double.NaN;
        int index = Math.floorMod(tick, POSITION_HISTORY_SIZE);
        return currentAxis.equals("X") ? positionHistoryX[index] : positionHistoryZ[index];
    }

    private static void processPendingSamples() {
        for (int i = 0; i < pendingJumps.size();) {
            PendingJump pending = pendingJumps.get(i);
            if (hasPositionSnapshot(pending.sampleTick)) {
                int index = Math.floorMod(pending.sampleTick, POSITION_HISTORY_SIZE);
                recordJump(positionHistoryX[index], positionHistoryZ[index], pending.jumpTick, pending.flatTime);
                pendingJumps.remove(i);
            } else {
                i++;
            }
        }
        if (pendingPlateSample && hasPositionSnapshot(pendingPlateTick)) {
            pendingPlateSample = false;
            recordPlateEntries(pendingPlateTick);
        }
    }

    public static synchronized void onBlockPlaced() {
        Minecraft mc = Minecraft.m_20213497();
        if (mc == null || mc.f_28396371 == null) return;
        C_57778754 player = mc.f_28396371;

        if (!isBridging || status == RunStatus.SUCCESS || status == RunStatus.FAILED) {
            // Start a new run upon placing the first block!
            startNewRun(player);
        }
    }

    private static void startNewRun(C_57778754 player) {
        captureFinishedRun();
        historyCaptured = false;
        PlacementEvidence.reset();
        entries.clear();
        jumpCount = 0;
        pendingJumps.clear();
        pendingGainSamples.clear();
        pendingPlateSample = false;
        startTick = currentTick;
        lastJumpTick = currentTick;
        lastJumpSignalTick = Integer.MIN_VALUE;
        startX = player.f_78826675;
        startY = player.f_31030949;
        previousY = startY;
        previousVelocityY = player.f_35148123;
        runStartNanos = System.nanoTime();
        java.util.Arrays.fill(jumpInputValid, false);
        startZ = player.f_97691941;
        savePositionSnapshot(player, currentTick);
        lastTickCoord = (currentAxis.equals("X") ? player.f_78826675 : player.f_97691941);
        prevTickCoord = lastTickCoord;
        flatFinalized = false;
        flatDuration = 1.05;
        turnRecorded = false;
        platesRecorded = false;
        groundTicksAfterLanding = 0;
        wasInAir = false;
        currentBridgeTime = 0.0;
        status = RunStatus.BRIDGING;
        isBridging = true;
        ReplayRecorder.start();
    }

    public static synchronized void onTick(Minecraft mc) {
        if (mc == null || mc.f_28396371 == null || mc.f_26407825 == null) {
            if (isBridging) { isBridging=false; status=RunStatus.FAILED; }
            pendingPlateSample=false;pendingJumps.clear();captureFinishedRun();
            return;
        }

        currentTick++;
        C_57778754 player = mc.f_28396371;
        double currentY = player.f_31030949;
        if (isBridging && JumpSignals.isUpwardTeleport(previousY, currentY)) {
            onFailSpeed(lastTickCoord, "Teleport", -1);
            // Empirical correction for the observed delay before the return teleport.
            currentBridgeTime = Math.max(0, currentTick - startTick - 2) / 20.0;
            previousY = currentY;
            previousVelocityY = player.f_35148123;
            return;
        }
        if (isBridging) ReplayRecorder.sample();
        if (isBridging && !player.f_47825615 && player.f_09676126 && (previousVelocityY > 0 || (long) currentTick - lastJumpSignalTick == 1) && !entries.isEmpty()) {
            double c = currentAxis.equals("X") ? player.f_78826675 : player.f_97691941;
            double d = Math.abs(c) - Math.floor(Math.abs(c));
            entries.add(new JumpEntry("Head hitter", c, d, Double.NaN, currentTick - lastJumpTick, JumpEntry.EntryType.HEAD_HITTER));
        }
        previousY = currentY;
        previousVelocityY = player.f_35148123;
        savePositionSnapshot(player, currentTick);
        saveMovementSnapshot(mc, currentTick);
        processPendingSamples();
        processPendingGainSamples();
        captureFinishedRun();

        // Determine current coordinate along active axis
        double currentCoord = currentAxis.equals("X") ? player.f_78826675 : player.f_97691941;

        // Check scoreboard time
        double sbTime = ScoreboardHelper.readScoreboardTime(mc.f_26407825.m_63054104());

        if (isBridging) currentBridgeTime = (currentTick - startTick) / 20.0;
        if (sbTime <= 0.0) sbTime = -1.0;
        if (sbTime >= 0.0) {
            // Flat duration detection around 1.00s - 1.25s
            if (isBridging && !flatFinalized && sbTime >= 1.00 && sbTime <= 1.25) {
                finalizeFlat(sbTime);
            }
        }

        if (!isBridging) {
            prevTickCoord = currentCoord;
            lastTickCoord = currentCoord;
            return;
        }

        // Check turn using the configured interval after the last jump.
        if (jumpCount > 0 && !turnRecorded && !platesRecorded) {
            int ticksSinceLastJump = currentTick - lastJumpTick;
            if (ticksSinceLastJump == BridgeConfig.turnTicks) {
                turnRecorded = true;
                double turnCoord = BridgeConfig.turnTicks == 12 ? currentCoord - 100.0 : currentCoord;
                double absCoord = Math.abs(turnCoord);
                double turnDecimal = absCoord - Math.floor(absCoord);
                double delta = turnDecimal - lastJumpDecimal;
                if (delta < -0.5) delta += 1.0;
                else if (delta > 0.5) delta -= 1.0;

                JumpEntry turnEntry = new JumpEntry("Turn", turnCoord, turnDecimal, delta, ticksSinceLastJump, JumpEntry.EntryType.TURN);
                entries.add(turnEntry);
            }
        }

        // Check Gold Pressure Plate
        if (isPlayerOnGoldPlate(mc)) {
            if (!platesRecorded) {
                platesRecorded = true;
                isBridging = false;
                status = RunStatus.SUCCESS;

                pendingPlateTick = currentTick + BridgeConfig.plateTickOffset;
                if (hasPositionSnapshot(pendingPlateTick)) {
                    recordPlateEntries(pendingPlateTick);
                } else if (pendingPlateTick > currentTick) {
                    pendingPlateSample = true;
                } else {
                    recordPlateEntries(pendingPlateTick);
                }
            }
            return;
        }

        // Save position history for Plates-1t
        prevTickCoord = lastTickCoord;
        lastTickCoord = currentCoord;
    }

    private static void onFailSpeed(double currentCoord, String reason, double sbTime) {
        if (!isBridging) return;
        isBridging = false;
        status = RunStatus.FAILED;

        // Use the same expected 9-tick sample (including the configured offset) as a real tap.
        int expectedTapTick = lastJumpTick + 9;
        int sampleTick = expectedTapTick + BridgeConfig.tickOffset;
        if (entries.isEmpty() || currentTick < expectedTapTick || !hasPositionSnapshot(sampleTick)) return;
        double tapCoord = coordinateAtTick(sampleTick);

        double absCoord = Math.abs(tapCoord);
        double decimal = absCoord - Math.floor(absCoord);
        double delta = decimal - lastJumpDecimal;
        if (delta < -0.5) delta += 1.0;
        else if (delta > 0.5) delta -= 1.0;

        if (sbTime >= 0.0) currentBridgeTime = sbTime;
        JumpEntry failEntry = new JumpEntry("Fail Tap", tapCoord, decimal, delta, 9, JumpEntry.EntryType.FAIL_TAP);
        entries.add(failEntry);

    }

    private static void recordPlateEntries(int plateTick) {
        double pCoord = coordinateAtTick(plateTick);
        double p1Coord = coordinateAtTick(plateTick - 1);
        if (Double.isNaN(pCoord)) pCoord = lastTickCoord;
        if (Double.isNaN(p1Coord)) p1Coord = prevTickCoord;

        double p1Abs = Math.abs(p1Coord);
        double p1Dec = p1Abs - Math.floor(p1Abs);
        entries.add(new JumpEntry("Plates-1t", p1Coord, p1Dec, Double.NaN, 0, JumpEntry.EntryType.PLATES_MINUS_1));

        double pAbs = Math.abs(pCoord);
        double pDec = pAbs - Math.floor(pAbs);
        entries.add(new JumpEntry("Plates", pCoord, pDec, Double.NaN, 0, JumpEntry.EntryType.PLATES));

        int decP1 = (int) Math.round(p1Dec * 1000.0) % 1000;
        int decP = (int) Math.round(pDec * 1000.0) % 1000;
        sendMessage(String.format(Locale.US, "\u00a7a[BridgeTap] Success! Time: \u00a7e%.2fs \u00a7a| Plates-1t: \u00a7e.%03d \u00a7a| Plates: \u00a7e.%03d", currentBridgeTime, decP1, decP));
    }

    private static void finalizeFlat(double duration) {
        flatDuration = duration;
        flatFinalized = true;
        if (!entries.isEmpty() && entries.get(0).getType() == JumpEntry.EntryType.FLAT && !entries.get(0).getLabel().equals("Jump")) {
            entries.get(0).setLabel(getFlatLabel(entries.get(0).getTicksElapsed(), flatDuration));
        }
    }

    private static String getFlatLabel(int ticks, double duration) {
        // The placement callback and jump callback can straddle a tick boundary, so
        // a real 12-tick flat may be observed as 11 elapsed tick intervals.
        if (ticks >= 11 && ticks <= 12) return "4b Flat";
        if (ticks == 4) return "1.05 Flat";
        if (ticks == 5) return "1.1 Flat";
        if (ticks == 6) return duration >= 1.175 ? "1.2 Flat" : "1.15 Flat";
        if (duration <= 1.075) return "1.05 Flat";
        if (duration <= 1.125) return "1.1 Flat";
        if (duration <= 1.175) return "1.15 Flat";
        if (duration <= 1.225) return "1.2 Flat";
        return String.format(Locale.US, "%.2f Flat", duration);
    }

    public static synchronized void onJump(C_42232651 entity) {
        Minecraft mc = Minecraft.m_20213497();
        if (mc == null || entity != mc.f_28396371) return;

        // A jump can only be recorded after a successful block placement starts the run.
        if (!isBridging || status == RunStatus.SUCCESS || status == RunStatus.FAILED) return;

        // A jump can occur later in the same client tick as the 12-tick turn check.
        // In that case it is a chained flat, not a turn.
        bridgetap$undoFalseTurn();
        int jumpTick = currentTick;
        lastJumpSignalTick = jumpTick;
        C_76792751 settings = mc.f_58088711;
        int inputIndex = Math.floorMod(jumpTick, POSITION_HISTORY_SIZE);
        jumpInputHistory[inputIndex] = settings == null ? null : JumpSignals.gain(settings.f_15607202.m_70603120(), settings.f_20678369.m_70603120(), settings.f_25268871.m_70603120(), settings.f_46759128.m_70603120());
        jumpInputTicks[inputIndex] = jumpTick;
        jumpInputValid[inputIndex] = true;
        int sampleTick = jumpTick + BridgeConfig.tickOffset;
        double jumpFlatTime = mc.f_26407825 == null ? flatDuration : ScoreboardHelper.readScoreboardTime(mc.f_26407825.m_63054104());
        if (jumpFlatTime < 0.0) jumpFlatTime = flatDuration;
        if (hasPositionSnapshot(sampleTick)) {
            int index = Math.floorMod(sampleTick, POSITION_HISTORY_SIZE);
            recordJump(positionHistoryX[index], positionHistoryZ[index], jumpTick, jumpFlatTime);
        } else if (sampleTick > jumpTick && BridgeConfig.tickOffset <= 40) {
            pendingJumps.add(new PendingJump(sampleTick, jumpTick, jumpFlatTime));
        } else {
            sendMessage("\u00a7c[BridgeTap] No saved jump position for tick " + sampleTick + ".");
        }
    }

    private static void bridgetap$undoFalseTurn() {
        if (turnRecorded && BridgeConfig.turnTicks == 12 && currentTick - lastJumpTick == 12
                && !entries.isEmpty() && entries.get(entries.size() - 1).getType() == JumpEntry.EntryType.TURN) {
            entries.remove(entries.size() - 1);
            turnRecorded = false;
        }
    }

    private static boolean hasJumpInput(int tick) {
        int i = Math.floorMod(tick, POSITION_HISTORY_SIZE);
        return jumpInputValid[i] && jumpInputTicks[i] == tick;
    }

    private static void recordJump(double sampledX, double sampledZ, int jumpTick, double jumpFlatTime) {
        // Determine the tracking axis using the selected tick's coordinates.
        if ("X".equalsIgnoreCase(BridgeConfig.preferredAxis)) {
            currentAxis = "X";
        } else if ("Z".equalsIgnoreCase(BridgeConfig.preferredAxis)) {
            currentAxis = "Z";
        } else {
            double dx = Math.abs(sampledX - startX);
            double dz = Math.abs(sampledZ - startZ);
            currentAxis = (dx > dz && dx > 1.0) ? "X" : "Z";
        }

        double currentCoord = currentAxis.equals("X") ? sampledX : sampledZ;
        double absCoord = Math.abs(currentCoord);
        double decimal = absCoord - Math.floor(absCoord);

        if (entries.isEmpty()) {
            // First jump (the flat) after first block placed
            int ticks = Math.max(1, jumpTick - startTick);
            String label = getFlatLabel(ticks, jumpFlatTime);
            double startCoord = currentAxis.equals("X") ? startX : startZ;
            if (!PlacementEvidence.qualifies(startTick, jumpTick, startCoord, currentCoord, currentAxis.equals("X"), ticks >= 11 && ticks <= 12 ? 4 : 1)) label = "Jump";
            JumpEntry flatEntry = new JumpEntry(label, currentCoord, decimal, Double.NaN, ticks, JumpEntry.EntryType.FLAT);
            if (hasJumpInput(jumpTick)) flatEntry.setGainType(jumpInputHistory[Math.floorMod(jumpTick, POSITION_HISTORY_SIZE)]);
            entries.add(flatEntry);

            lastJumpTick = jumpTick;
            lastJumpCoord = currentCoord;
            lastJumpDecimal = decimal;
        } else {
            // Subsequent speed telly taps
            jumpCount++;
            int ticksElapsed = jumpTick - lastJumpTick;

            // Delta depends on the last tap!
            double delta = decimal - lastJumpDecimal;
            if (delta < -0.5) delta += 1.0;
            else if (delta > 0.5) delta -= 1.0;

            double blockDist = Math.abs(currentCoord - lastJumpCoord);
            int blocks = (int) Math.round(blockDist);
            String label;
            if (turnRecorded) {
                label = "Jump #" + jumpCount;
            } else if (ticksElapsed == 12) {
                label = "4b Flat #" + jumpCount;
            } else if (ticksElapsed == 9) {
                label = "Tap Speed #" + jumpCount;
            } else if (ticksElapsed == 10) {
                label = "Hold Speed #" + jumpCount;
            } else {
                label = (blocks >= 4 ? "4b Jump #" : "Jump #") + jumpCount;
            }

            if (!turnRecorded && !PlacementEvidence.qualifies(lastJumpTick, jumpTick, lastJumpCoord, currentCoord, currentAxis.equals("X"), ticksElapsed == 12 ? 4 : 1)) label = "Jump #" + jumpCount;
            JumpEntry tapEntry = new JumpEntry(label, currentCoord, decimal, delta, ticksElapsed, JumpEntry.EntryType.TAP);
            entries.add(tapEntry);
            if (hasJumpInput(jumpTick)) tapEntry.setGainType(jumpInputHistory[Math.floorMod(jumpTick, POSITION_HISTORY_SIZE)]);

            lastJumpTick = jumpTick;
            lastJumpCoord = currentCoord;
            lastJumpDecimal = decimal;
        }

        wasInAir = false;
        groundTicksAfterLanding = 0;
    }

    public static boolean isPlayerOnGoldPlate(Minecraft mc) {
        if (mc == null || mc.f_28396371 == null || mc.f_26407825 == null) return false;
        C_57778754 p = mc.f_28396371;
        C_82690114 box = p.m_41837471();
        if (box == null) return false;
        int minX = (int) Math.floor(box.f_87518512);
        int maxX = (int) Math.floor(box.f_89130367);
        int minY = (int) Math.floor(box.f_80314167 - 0.25);
        int maxY = (int) Math.floor(box.f_98553650);
        int minZ = (int) Math.floor(box.f_85940458);
        int maxZ = (int) Math.floor(box.f_24562106);

        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    if (box.f_89130367 <= bx + 0.125 || box.f_87518512 >= bx + 0.875
                            || box.f_98553650 <= by || box.f_80314167 >= by + 0.25
                            || box.f_24562106 <= bz + 0.125 || box.f_85940458 >= bz + 0.875) continue;
                    C_32594356 pos = new C_32594356(bx, by, bz);
                    C_81592558 block = mc.f_26407825.m_77852500(pos).m_41958956();
                    if (block != null && C_81592558.m_63461719(block) == 147) return true;
                }
            }
        }

        return false;
    }

    public static boolean onChatMessage(String message) {
        if (message == null) return false;
        String clean = message.trim();
        if (!clean.startsWith("/bt") && !clean.startsWith("/bridgetap")) {
            return false;
        }

        String[] parts = clean.split("\\s+");
        if (parts.length == 1) {
            sendMessage("\u00a7b[BridgeTap] \u00a7eAvailable commands:");
            sendMessage("\u00a7e/bt toggle \u00a77- Enable/disable the HUD");
            sendMessage("\u00a7e/bt reset \u00a77- Reset the stats");
            sendMessage("\u00a7e/bt pos <x> <y> \u00a77- Set the HUD position");
            sendMessage("\u00a7e/bt ui \u00a77- Open BridgeTap settings");
            sendMessage("\u00a7e/bt axis <AUTO|Z|X> \u00a77- Set the tracking axis");
            return true;
        }

        String sub = parts[1].toLowerCase(Locale.US);
        if (sub.equals("toggle")) {
            BridgeConfig.enabled = !BridgeConfig.enabled;
            BridgeConfig.save();
            sendMessage("\u00a7b[BridgeTap] \u00a77HUD: " + (BridgeConfig.enabled ? "\u00a7aEnabled" : "\u00a7cDisabled"));
            return true;
        } else if (sub.equals("reset")) {
            reset();
            sendMessage("\u00a7b[BridgeTap] \u00a7aStats reset!");
            return true;
        } else if (sub.equals("pos") && parts.length >= 4) {
            try {
                BridgeConfig.hudX = Integer.parseInt(parts[2]);
                BridgeConfig.hudY = Integer.parseInt(parts[3]);
                BridgeConfig.save();
                sendMessage("\u00a7b[BridgeTap] \u00a7aPosition updated: (" + BridgeConfig.hudX + ", " + BridgeConfig.hudY + ")");
            } catch (Exception e) {
                sendMessage("\u00a7cUsage: /bt pos <x> <y>");
            }
            return true;
        } else if (sub.equals("ui") || sub.equals("settings")) {
            BridgeHUD.openSettings();
            return true;
        } else if (sub.equals("axis") && parts.length >= 3) {
            String ax = parts[2].toUpperCase(Locale.US);
            if (ax.equals("AUTO") || ax.equals("Z") || ax.equals("X")) {
                BridgeConfig.preferredAxis = ax;
                BridgeConfig.save();
                sendMessage("\u00a7b[BridgeTap] \u00a7aAxis set to: " + ax);
            } else {
                sendMessage("\u00a7cUsage: /bt axis <AUTO|Z|X>");
            }
            return true;
        }

        return true;
    }

    private static void sendMessage(String msg) {
        Minecraft mc = Minecraft.m_20213497();
        if (mc != null && mc.f_28396371 != null) {
            mc.f_28396371.m_83523863(new C_69136080(msg));
        }
    }
}
