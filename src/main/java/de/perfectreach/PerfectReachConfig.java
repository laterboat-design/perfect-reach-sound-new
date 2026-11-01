package de.perfectreach;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class PerfectReachConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH =
            FabricLoader.getInstance().getConfigDir().resolve("perfectreachsound.json");

    private static PerfectReachConfig instance = new PerfectReachConfig();

    /** Mod an/aus. */
    public boolean enabled = true;

    /**
     * true  = Fenster wird aus dem Reichweiten-Attribut des Spielers berechnet
     *         (Standard 3.0, passt sich Waffen-/Reach-Mods an, die das Attribut ändern):
     *         [reichweite - windowSize, reichweite]
     * false = feste Werte minDistance / maxDistance.
     */
    public boolean useAttributeRange = true;
    public double windowSize = 0.5;

    public double minDistance = 2.95;
    public double maxDistance = 3.45;

    /** 0.0 - 1.0 */
    public float volume = 0.6f;
    /** Note-Block-Tonhöhe, 0.5 - 2.0 */
    public float pitch = 1.5f;
    /** Abstand zwischen zwei "Dings" in Ticks (20 Ticks = 1 Sekunde). */
    public int intervalTicks = 4;

    /** Nur Ton, wenn der Angriffs-Cooldown (fast) voll ist. */
    public boolean requireAttackReady = false;
    public boolean includePlayers = true;
    public boolean includeMobs = true;

    public static PerfectReachConfig get() {
        return instance;
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                PerfectReachConfig loaded = GSON.fromJson(reader, PerfectReachConfig.class);
                if (loaded != null) {
                    instance = loaded;
                }
            } catch (Exception e) {
                System.err.println("[PerfectReachSound] Config konnte nicht gelesen werden, nutze Standardwerte: " + e);
            }
        }
        instance.sanitize();
        save();
    }

    public static void save() {
        instance.sanitize();
        try (Writer writer = Files.newBufferedWriter(PATH)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            System.err.println("[PerfectReachSound] Config konnte nicht gespeichert werden: " + e);
        }
    }

    private void sanitize() {
        volume = Math.max(0f, Math.min(1f, volume));
        pitch = Math.max(0.5f, Math.min(2f, pitch));
        intervalTicks = Math.max(1, Math.min(40, intervalTicks));
        windowSize = Math.max(0.05, Math.min(2.0, windowSize));
        if (minDistance < 0) minDistance = 0;
        if (maxDistance < minDistance) maxDistance = minDistance;
    }
}
