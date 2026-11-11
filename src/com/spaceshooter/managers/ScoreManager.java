package com.spaceshooter.managers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages persistent local score storage and player history using a dedicated CSV file.
 * Handles directory creation, file migration, corrupted line recovery, and score rankings.
 */
public class ScoreManager {

    private static final Path DATA_DIR = Path.of("data");
    private static final Path FILE_PATH = DATA_DIR.resolve("scores.csv");
    private static final Path LEGACY_FILE_PATH = Path.of("scores.csv");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ScoreManager() {
        ensureDataFileExists();
    }

    /**
     * Ensures the data folder and scores file exist. Migrates legacy scores if present.
     */
    private synchronized void ensureDataFileExists() {
        try {
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
            }

            if (!Files.exists(FILE_PATH)) {
                // If legacy scores.csv exists in root directory, migrate it
                if (Files.exists(LEGACY_FILE_PATH)) {
                    try {
                        Files.copy(LEGACY_FILE_PATH, FILE_PATH);
                    } catch (IOException e) {
                        Files.createFile(FILE_PATH);
                    }
                } else {
                    Files.createFile(FILE_PATH);
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not initialize score storage: " + e.getMessage());
        }
    }

    /**
     * Validates and sanitizes a player name.
     * @param name Raw player input
     * @return Cleaned player name, guaranteed non-empty and within 25 characters
     */
    public static String sanitizePlayerName(String name) {
        if (name == null) {
            return "Pilot";
        }
        String cleaned = name.trim().replace("\n", " ").replace("\r", " ").replace("\"", "'");
        if (cleaned.isEmpty()) {
            return "Pilot";
        }
        if (cleaned.length() > 25) {
            cleaned = cleaned.substring(0, 25).trim();
        }
        return cleaned;
    }

    /**
     * Checks if a proposed player name is valid.
     * @param name Proposed username
     * @return Error message if invalid, or null if valid.
     */
    public static String validatePlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Player name cannot be empty!";
        }
        String trimmed = name.trim();
        if (trimmed.length() > 25) {
            return "Player name must be 25 characters or fewer!";
        }
        return null;
    }

    /**
     * Saves a completed game score.
     */
    public synchronized void saveScore(String playerName, int score, int levelReached) throws IOException {
        ensureDataFileExists();

        String safeName = sanitizePlayerName(playerName);
        String playedAt = LocalDateTime.now().format(DATE_FORMATTER);

        String line = csv(safeName) + ","
                + Math.max(0, score) + ","
                + Math.max(1, levelReached) + ","
                + csv(playedAt)
                + System.lineSeparator();

        Files.writeString(
                FILE_PATH,
                line,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    /**
     * Reads all valid score records from disk, gracefully ignoring any corrupted lines.
     */
    public synchronized List<ScoreRecord> getAllScores() {
        List<ScoreRecord> records = new ArrayList<>();
        ensureDataFileExists();

        if (!Files.exists(FILE_PATH)) {
            return records;
        }

        try {
            List<String> lines = Files.readAllLines(FILE_PATH, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }
                try {
                    List<String> fields = parseCsvLine(line);
                    if (fields.size() < 4) {
                        continue;
                    }

                    ScoreRecord record = new ScoreRecord();
                    record.playerName = fields.get(0);
                    record.score = Integer.parseInt(fields.get(1).trim());
                    record.levelReached = Integer.parseInt(fields.get(2).trim());
                    record.playedAt = fields.get(3).trim();

                    records.add(record);
                } catch (Exception ignored) {
                    // Skip corrupted or unparseable line
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Error reading scores: " + e.getMessage());
        }

        return records;
    }

    /**
     * Retrieves the persistent Top 10 scores sorted highest to lowest.
     */
    public synchronized List<ScoreRecord> getTopScores() {
        List<ScoreRecord> scores = getAllScores();

        scores.sort(
                Comparator.comparingInt((ScoreRecord r) -> r.score)
                        .reversed()
                        .thenComparing(r -> r.playedAt, Comparator.reverseOrder()));

        return new ArrayList<>(scores.subList(0, Math.min(10, scores.size())));
    }

    /**
     * Returns the highest score in history, or null if no scores exist yet.
     */
    public synchronized ScoreRecord getTopScore() {
        List<ScoreRecord> top = getTopScores();
        return top.isEmpty() ? null : top.get(0);
    }

    /**
     * Retrieves the highest score for a specific player name.
     */
    public synchronized int getPlayerHighScore(String playerName) {
        if (playerName == null) return 0;
        String clean = sanitizePlayerName(playerName);
        int max = 0;
        for (ScoreRecord r : getAllScores()) {
            if (r.playerName.equalsIgnoreCase(clean)) {
                if (r.score > max) {
                    max = r.score;
                }
            }
        }
        return max;
    }

    /**
     * Retrieves the username of the most recent player, or "Pilot" if none.
     */
    public synchronized String getLastPlayerName() {
        List<ScoreRecord> all = getAllScores();
        if (all.isEmpty()) {
            return "Pilot";
        }
        return all.get(all.size() - 1).playerName;
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);

            if (current == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (current == ',' && !insideQuotes) {
                fields.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }
        fields.add(value.toString());
        return fields;
    }

    public static class ScoreRecord {
        public String playerName;
        public int score;
        public int levelReached;
        public String playedAt;

        @Override
        public String toString() {
            return playerName + " - " + score + " pts (Level " + levelReached + ", " + playedAt + ")";
        }
    }
}
