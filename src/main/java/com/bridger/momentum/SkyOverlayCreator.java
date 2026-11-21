package com.bridger.momentum;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_02398648;

/**
 * Extracts OptiFine and MCPatcher custom sky files from texture packs into standalone sky overlays,
 * with recursive folder discovery and hierarchical folder navigation.
 */
public final class SkyOverlayCreator {
    public static final String OUTPUT_FOLDER = "BridgeTap Sky Overlays";
    public static final class SkyFile {
        public final String relativePath;
        public final byte[] data;
        public SkyFile(String relativePath, byte[] data) {
            this.relativePath = relativePath;
            this.data = data;
        }
    }

    public static final class PackEntry {
        public final String displayName;
        public final File file;
        public final boolean active;
        public final String relativePath;
        public final String folderCategory;
        private volatile int skyCount = -1;
        private volatile boolean celestials;

        public PackEntry(String displayName, File file, boolean active) {
            this(displayName, file, active, file != null ? file.getName() : displayName, "");
        }

        public PackEntry(String displayName, File file, boolean active, String relativePath, String folderCategory) {
            this.displayName = displayName;
            this.file = file;
            this.active = active;
            this.relativePath = relativePath;
            this.folderCategory = folderCategory;
        }

        public int skyCount() {
            if (skyCount < 0) scan();
            return Math.max(0, skyCount);
        }

        public boolean hasCelestials() {
            if (skyCount < 0) scan();
            return celestials;
        }

        public boolean hasSky() {
            return skyCount() > 0;
        }

        private synchronized void scan() {
            if (skyCount >= 0) return;
            try {
                ScanResult res = scanPack(file, true);
                this.skyCount = res.count;
                this.celestials = res.hasCelestials;
            } catch (Exception ex) {
                this.skyCount = 0;
                this.celestials = false;
            }
        }
    }

    public static final class FolderNode {
        public final String name;
        public final String relativePath;
        public final File directory;
        public final List<FolderNode> subfolders = new ArrayList<>();
        public final List<PackEntry> packs = new ArrayList<>();
        public int totalPacks = 0;

        public FolderNode(String name, String relativePath, File directory) {
            this.name = name;
            this.relativePath = relativePath;
            this.directory = directory;
        }
    }

    public static final class ScanResult {
        public final int count;
        public final boolean hasCelestials;
        public final List<String> samples;
        public ScanResult(int count, boolean hasCelestials, List<String> samples) {
            this.count = count;
            this.hasCelestials = hasCelestials;
            this.samples = samples;
        }
    }

    public static final class CreateResult {
        public final boolean success;
        public final int filesCount;
        public final File outputFile;
        public final String message;
        public CreateResult(boolean success, int filesCount, File outputFile, String message) {
            this.success = success;
            this.filesCount = filesCount;
            this.outputFile = outputFile;
            this.message = message;
        }
    }

    public static boolean isSkyPath(String path, boolean includeCelestials) {
        String p = path.replace('\\', '/').toLowerCase(Locale.ROOT);
        while (p.startsWith("/")) p = p.substring(1);
        if (p.startsWith("assets/minecraft/optifine/sky/") || p.startsWith("assets/minecraft/mcpatcher/sky/")
                || p.startsWith("optifine/sky/") || p.startsWith("mcpatcher/sky/")) {
            return !p.endsWith("/");
        }
        if (p.startsWith("assets/minecraft/optifine/colormap/sky") || p.startsWith("assets/minecraft/mcpatcher/colormap/sky")
                || p.startsWith("optifine/colormap/sky") || p.startsWith("mcpatcher/colormap/sky")) {
            return !p.endsWith("/");
        }
        if (includeCelestials) {
            if (p.equals("assets/minecraft/textures/environment/sun.png") || p.equals("textures/environment/sun.png")
                    || p.equals("assets/minecraft/textures/environment/moon_phases.png") || p.equals("textures/environment/moon_phases.png")
                    || p.equals("assets/minecraft/textures/environment/clouds.png") || p.equals("textures/environment/clouds.png")) {
                return true;
            }
        }
        return false;
    }

    public static String normalizeSkyPath(String path) {
        String p = path.replace('\\', '/');
        while (p.startsWith("/")) p = p.substring(1);
        String lower = p.toLowerCase(Locale.ROOT);
        if (lower.startsWith("optifine/sky/") || lower.startsWith("mcpatcher/sky/")
                || lower.startsWith("optifine/colormap/") || lower.startsWith("mcpatcher/colormap/")) {
            return "assets/minecraft/" + p;
        }
        if (lower.startsWith("textures/environment/")) {
            return "assets/minecraft/" + p;
        }
        return p;
    }

    public static ScanResult scanPack(File file, boolean includeCelestials) throws IOException {
        if (file == null || !file.exists()) return new ScanResult(0, false, Collections.emptyList());
        int count = 0;
        boolean celestials = false;
        List<String> samples = new ArrayList<>();
        if (file.isDirectory()) {
            Path root = file.toPath();
            try (java.util.stream.Stream<Path> stream = Files.walk(root)) {
                for (Iterator<Path> it = stream.iterator(); it.hasNext();) {
                    Path p = it.next();
                    if (Files.isDirectory(p)) continue;
                    String rel = root.relativize(p).toString().replace('\\', '/');
                    if (isSkyPath(rel, true)) {
                        boolean isCelestial = rel.toLowerCase(Locale.ROOT).contains("environment");
                        if (!isCelestial || includeCelestials) count++;
                        if (isCelestial) celestials = true;
                        if (samples.size() < 5) samples.add(rel);
                    }
                }
            }
        } else if (file.isFile() && file.getName().toLowerCase(Locale.ROOT).endsWith(".zip")) {
            try (ZipFile zip = new ZipFile(file)) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.isDirectory()) continue;
                    String name = entry.getName();
                    if (isSkyPath(name, true)) {
                        boolean isCelestial = name.toLowerCase(Locale.ROOT).contains("environment");
                        if (!isCelestial || includeCelestials) count++;
                        if (isCelestial) celestials = true;
                        if (samples.size() < 5) samples.add(name);
                    }
                }
            }
        }
        return new ScanResult(count, celestials, samples);
    }

    public static CreateResult createOverlay(File sourcePack, File outputDir, String overlayName, boolean includeCelestials) {
        return createOverlay(sourcePack, outputDir, overlayName, includeCelestials, OverlayRecolor.Settings.original());
    }

    public static CreateResult createOverlay(File sourcePack, File outputDir, String overlayName, boolean includeCelestials, OverlayRecolor.Settings recolor) {
        if (sourcePack == null || !sourcePack.exists()) {
            return new CreateResult(false, 0, null, "Source pack file does not exist.");
        }
        if (overlayName == null || overlayName.trim().isEmpty()) {
            overlayName = defaultOverlayName(sourcePack);
        }
        String sanitized = sanitizeFilename(overlayName.trim());
        if (!sanitized.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            sanitized += ".zip";
        }
        File targetDir = new File(outputDir, OUTPUT_FOLDER);
        File outputFile;
        try {
            Files.createDirectories(targetDir.toPath());
            String stem = sanitized.substring(0, sanitized.length() - 4);
            Path candidate = targetDir.toPath().resolve(sanitized);
            int suffix = 2;
            while (Files.exists(candidate)) candidate = targetDir.toPath().resolve(stem + " (" + suffix++ + ").zip");
            outputFile = candidate.toFile();
        } catch (IOException ex) {
            return new CreateResult(false, 0, null, "Could not create the BridgeTap sky folder.");
        }

        int written = 0; boolean createdFile = false;
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(outputFile.toPath(), StandardOpenOption.CREATE_NEW)))) {
            createdFile = true;
            String cleanTitle = sanitized.replaceAll("(?i)\\.zip$", "");
            String mcmeta = "{\n  \"pack\": {\n    \"pack_format\": 1,\n    \"description\": \"§b[Sky Overlay] §f"
                    + cleanTitle.replace("\"", "\\\"") + "\"\n  }\n}\n";
            ZipEntry metaEntry = new ZipEntry("pack.mcmeta");
            zos.putNextEntry(metaEntry);
            zos.write(mcmeta.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            byte[] iconBytes = extractOrGenerateIcon(sourcePack);
            if (iconBytes != null) {
                ZipEntry iconEntry = new ZipEntry("pack.png");
                zos.putNextEntry(iconEntry);
                zos.write(iconBytes);
                zos.closeEntry();
            }

            Set<String> addedPaths = new HashSet<>();
            if (sourcePack.isDirectory()) {
                Path root = sourcePack.toPath();
                try (java.util.stream.Stream<Path> stream = Files.walk(root)) {
                    for (Iterator<Path> it = stream.iterator(); it.hasNext();) {
                        Path p = it.next();
                        if (Files.isDirectory(p)) continue;
                        String rel = root.relativize(p).toString().replace('\\', '/');
                        if (isSkyPath(rel, includeCelestials)) {
                            String targetPath = normalizeSkyPath(rel);
                            if (addedPaths.add(targetPath.toLowerCase(Locale.ROOT))) {
                                ZipEntry entry = new ZipEntry(targetPath);
                                zos.putNextEntry(entry);
                                try(InputStream input=Files.newInputStream(p)){OverlayRecolor.copy(input,zos,targetPath,recolor);}
                                zos.closeEntry();
                                written++;
                            }
                        }
                    }
                }
            } else if (sourcePack.isFile()) {
                try (ZipFile zip = new ZipFile(sourcePack)) {
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (entry.isDirectory()) continue;
                        String name = entry.getName();
                        if (isSkyPath(name, includeCelestials)) {
                            String targetPath = normalizeSkyPath(name);
                            if (addedPaths.add(targetPath.toLowerCase(Locale.ROOT))) {
                                ZipEntry outEntry = new ZipEntry(targetPath);
                                zos.putNextEntry(outEntry);
                                try (InputStream is = zip.getInputStream(entry)) {
                                    OverlayRecolor.copy(is,zos,targetPath,recolor);
                                }
                                zos.closeEntry();
                                written++;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (createdFile && outputFile.exists()) outputFile.delete();
            return new CreateResult(false, 0, null, "Failed to create overlay: " + ex.getMessage());
        }

        if (written == 0) {
            outputFile.delete();
            return new CreateResult(false, 0, null, "No custom sky files were found in '" + sourcePack.getName() + "'.");
        }

        return new CreateResult(true, written, outputFile, "Created '" + outputFile.getName() + "' (" + written + " sky files)");
    }

    public static byte[] extractOrGenerateIcon(File sourcePack) {
        BufferedImage original = null;
        if (sourcePack.isDirectory()) {
            File packPng = new File(sourcePack, "pack.png");
            if (packPng.isFile()) {
                try { original = ImageIO.read(packPng); } catch (Exception ignored) {}
            }
        } else if (sourcePack.isFile()) {
            try (ZipFile zip = new ZipFile(sourcePack)) {
                ZipEntry entry = zip.getEntry("pack.png");
                if (entry != null) {
                    try (InputStream is = zip.getInputStream(entry)) {
                        original = ImageIO.read(is);
                    }
                }
            } catch (Exception ignored) {}
        }

        BufferedImage icon;
        if (original != null) {
            icon = new BufferedImage(Math.max(64, original.getWidth()), Math.max(64, original.getHeight()), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = icon.createGraphics();
            g.drawImage(original, 0, 0, icon.getWidth(), icon.getHeight(), null);
            drawSkyBadge(g, icon.getWidth(), icon.getHeight());
            g.dispose();
        } else {
            icon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = icon.createGraphics();
            g.setColor(new Color(15, 25, 45));
            g.fillRect(0, 0, 64, 64);
            g.setColor(new Color(99, 211, 211));
            g.drawRect(2, 2, 59, 59);
            drawSkyBadge(g, 64, 64);
            g.dispose();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(icon, "png", baos);
            return baos.toByteArray();
        } catch (Exception ex) {
            return null;
        }
    }

    private static void drawSkyBadge(Graphics2D g, int width, int height) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int barHeight = Math.max(16, height / 4);
        int barY = height - barHeight;
        g.setColor(new Color(10, 15, 25, 220));
        g.fillRect(0, barY, width, barHeight);
        g.setColor(new Color(99, 211, 211));
        g.fillRect(0, barY, width, 2);
        g.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, barHeight - 4)));
        String label = "SKY OVERLAY";
        int textWidth = g.getFontMetrics().stringWidth(label);
        int textX = Math.max(2, (width - textWidth) / 2);
        int textY = barY + barHeight - Math.max(2, (barHeight - g.getFontMetrics().getAscent()) / 2);
        g.drawString(label, textX, textY);
    }

    public static String defaultOverlayName(File packFile) {
        String name = packFile.getName().replaceAll("(?i)\\.zip$", "");
        return name + " [Sky Overlay]";
    }

    public static String sanitizeFilename(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public static void findPacksRecursively(File dir, List<File> result, int depth) {
        if (dir == null || !dir.isDirectory() || depth > 6) return;
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File f : children) {
            if (f.isFile() && f.getName().toLowerCase(Locale.ROOT).endsWith(".zip")) {
                result.add(f);
            } else if (f.isDirectory()) {
                if (new File(f, "pack.mcmeta").isFile() || new File(f, "assets").isDirectory()) {
                    result.add(f);
                } else {
                    findPacksRecursively(f, result, depth + 1);
                }
            }
        }
    }

    public static void sortPacksByFolder(File rootDir, List<File> list) {
        if (rootDir == null || list == null || list.size() <= 1) return;
        Path root = rootDir.toPath();
        Collections.sort(list, (a, b) -> {
            String relA = getRelativeSafe(root, a);
            String relB = getRelativeSafe(root, b);
            boolean isRootA = !relA.contains("/");
            boolean isRootB = !relB.contains("/");
            if (isRootA != isRootB) return isRootA ? -1 : 1;
            return relA.compareToIgnoreCase(relB);
        });
    }

    private static String getRelativeSafe(Path root, File file) {
        try {
            return root.relativize(file.toPath()).toString().replace('\\', '/');
        } catch (Exception ex) {
            return file.getName();
        }
    }

    public static String getFolderPrefix(File packDir, File packFile) {
        if (packDir == null || packFile == null) return null;
        try {
            String root = packDir.getCanonicalPath().replace('\\', '/');
            String f = packFile.getCanonicalPath().replace('\\', '/');
            if (f.startsWith(root)) {
                String sub = f.substring(root.length());
                while (sub.startsWith("/")) sub = sub.substring(1);
                int lastSlash = sub.lastIndexOf('/');
                if (lastSlash > 0) {
                    return sub.substring(0, lastSlash);
                }
            }
        } catch (Exception ex) {
            String root = packDir.getAbsolutePath().replace('\\', '/');
            String f = packFile.getAbsolutePath().replace('\\', '/');
            if (f.startsWith(root)) {
                String sub = f.substring(root.length());
                while (sub.startsWith("/")) sub = sub.substring(1);
                int lastSlash = sub.lastIndexOf('/');
                if (lastSlash > 0) {
                    return sub.substring(0, lastSlash);
                }
            }
        }
        return null;
    }

    public static FolderNode buildFolderTree(File packDir, Set<String> activePaths) {
        if (packDir == null || !packDir.isDirectory()) return new FolderNode("root", "", packDir);
        return buildFolderNode(packDir, packDir, "", activePaths, 0);
    }

    private static FolderNode buildFolderNode(File rootDir, File currentDir, String relPath, Set<String> activePaths, int depth) {
        String nodeName = currentDir.equals(rootDir) ? "root" : currentDir.getName();
        FolderNode node = new FolderNode(nodeName, relPath, currentDir);
        if (depth > 6) return node;

        File[] children = currentDir.listFiles();
        if (children == null) return node;

        Arrays.sort(children, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        for (File f : children) {
            if (f.isFile() && f.getName().toLowerCase(Locale.ROOT).endsWith(".zip")) {
                String pRel = relPath.isEmpty() ? f.getName() : relPath + "/" + f.getName();
                boolean active = isActivePath(f, activePaths);
                node.packs.add(new PackEntry(f.getName(), f, active, pRel, relPath));
            } else if (f.isDirectory()) {
                if (new File(f, "pack.mcmeta").isFile() || new File(f, "assets").isDirectory()) {
                    String pRel = relPath.isEmpty() ? f.getName() : relPath + "/" + f.getName();
                    boolean active = isActivePath(f, activePaths);
                    node.packs.add(new PackEntry(f.getName(), f, active, pRel, relPath));
                } else {
                    String subRel = relPath.isEmpty() ? f.getName() : relPath + "/" + f.getName();
                    FolderNode sub = buildFolderNode(rootDir, f, subRel, activePaths, depth + 1);
                    node.subfolders.add(sub);
                }
            }
        }

        int count = node.packs.size();
        for (FolderNode s : node.subfolders) count += s.totalPacks;
        node.totalPacks = count;

        return node;
    }

    private static boolean isActivePath(File file, Set<String> activePaths) {
        if (file == null || activePaths == null || activePaths.isEmpty()) return false;
        return activePaths.contains(file.getAbsolutePath().toLowerCase(Locale.ROOT))
                || activePaths.contains(file.getName().toLowerCase(Locale.ROOT));
    }

    public static FolderNode findNode(FolderNode root, String path) {
        if (path == null || path.isEmpty() || root == null) return root;
        String[] parts = path.split("/");
        FolderNode current = root;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            FolderNode match = null;
            for (FolderNode sub : current.subfolders) {
                if (sub.name.equalsIgnoreCase(part)) {
                    match = sub;
                    break;
                }
            }
            if (match == null) return current;
            current = match;
        }
        return current;
    }

    public static Set<String> getActivePackPaths(Minecraft mc) {
        Set<String> activePaths = new HashSet<>();
        if (mc == null) return activePaths;
        C_02398648 repo = mc.m_21067396();
        if (repo == null) return activePaths;

        List<C_02398648.C_53553981> activeEntries = repo.m_23972443();
        if (activeEntries != null) {
            for (C_02398648.C_53553981 e : activeEntries) {
                File f = resolvePackFile(repo, e);
                if (f != null) {
                    activePaths.add(f.getAbsolutePath().toLowerCase(Locale.ROOT));
                    
                }
            }
        }
        return activePaths;
    }

    public static List<PackEntry> listAvailablePacks(Minecraft mc) {
        List<PackEntry> result = new ArrayList<>();
        if (mc == null) return result;
        C_02398648 repo = mc.m_21067396();
        if (repo == null) return result;

        Set<String> activePaths = getActivePackPaths(mc);
        File packDir = repo.m_08141491();
        if (packDir != null && packDir.isDirectory()) {
            List<File> discovered = new ArrayList<>();
            findPacksRecursively(packDir, discovered, 0);
            sortPacksByFolder(packDir, discovered);
            Path rootPath = packDir.toPath();
            for (File f : discovered) {
                String rel;
                try {
                    rel = rootPath.relativize(f.toPath()).toString().replace('\\', '/');
                } catch (Exception ex) {
                    rel = f.getName();
                }
                String category = getFolderPrefix(packDir, f);
                boolean active = isActivePath(f, activePaths);
                result.add(new PackEntry(f.getName(), f, active, rel, category == null ? "" : category));
            }
        }

        Collections.sort(result, (a, b) -> {
            if (a.active != b.active) return a.active ? -1 : 1;
            return a.displayName.compareToIgnoreCase(b.displayName);
        });
        return result;
    }

    public static File resolvePackFile(C_02398648 repo, C_02398648.C_53553981 entry) {
        if (entry == null) return null;
        if (repo != null && repo.m_08141491() != null) {
            File f = new File(repo.m_08141491(), entry.m_46157105());
            if (f.exists()) return f;
            File found = findNamedPack(repo.m_08141491(), entry.m_46157105(), 0);
            if (found != null) return found;
        }
        try {
            Object pack = entry.m_76480089();
            if (pack != null) {
                for (Class<?> c = pack.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                    for (java.lang.reflect.Field field : c.getDeclaredFields()) {
                        if (File.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            File f = (File) field.get(pack);
                            if (f != null && f.exists()) return f;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        try {
            for (java.lang.reflect.Field field : entry.getClass().getDeclaredFields()) {
                if (File.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    File f = (File) field.get(entry);
                    if (f != null && f.exists()) return f;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static File findNamedPack(File dir, String name, int depth) {
        if (dir == null || !dir.isDirectory() || depth > 6) return null;
        File[] children = dir.listFiles();
        if (children == null) return null;
        for (File f : children) {
            if (f.getName().equalsIgnoreCase(name)) return f;
            if (f.isDirectory() && !new File(f, "pack.mcmeta").isFile() && !new File(f, "assets").isDirectory()) {
                File sub = findNamedPack(f, name, depth + 1);
                if (sub != null) return sub;
            }
        }
        return null;
    }

    public static boolean activateOverlay(Minecraft mc, File zipFile) {
        if (mc == null || zipFile == null) return false;
        C_02398648 repo = mc.m_21067396();
        if (repo == null) return false;
        try {
            repo.m_62580839();
            List<C_02398648.C_53553981> all = repo.m_08797075();
            C_02398648.C_53553981 match = null;
            for (C_02398648.C_53553981 entry : all) {
                File f = resolvePackFile(repo, entry);
                if (f != null && f.getCanonicalFile().equals(zipFile.getCanonicalFile())) {
                    match = entry;
                    break;
                }
            }
            if (match == null) {
                // Explicit activation of one generated pack, without flattening the native repository.
                java.lang.reflect.Constructor<C_02398648.C_53553981> ctor = C_02398648.C_53553981.class.getDeclaredConstructor(C_02398648.class, File.class);
                ctor.setAccessible(true);
                match = ctor.newInstance(repo, zipFile);
                match.m_43595052();
            }
            if (match != null) {
                List<C_02398648.C_53553981> current = new ArrayList<>(repo.m_23972443());
                current.remove(match);
                current.add(match);
                repo.m_59621098(current);
                mc.m_16950819();
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
