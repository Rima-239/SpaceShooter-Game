package com.spaceshooter.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-fidelity, smooth procedural 16-bit audio synthesizer.
 * Generates non-piercing, balanced retro-arcade sound effects and an ambient space background track.
 * Supports independent master, music, and SFX volume levels, concurrency limiting, and mute toggling.
 */
public class SoundManager {

    private static final SoundManager INSTANCE = new SoundManager();

    private static final float SAMPLE_RATE = 22050f;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

    private boolean muted = false;
    private boolean audioSupported = true;

    private float masterVolume = 0.80f; // [0.0 .. 1.0]
    private float sfxVolume = 0.75f;    // [0.0 .. 1.0]
    private float musicVolume = 0.38f;  // [0.0 .. 1.0]

    // Pre-generated 16-bit sound buffers
    private byte[] shootSound;
    private byte[] explosionSound;
    private byte[] hitSound;
    private byte[] powerUpSound;
    private byte[] playerDamageSound;
    private byte[] bossWarningSound;
    private byte[] levelCompleteSound;
    private byte[] gameOverSound;
    private byte[] menuClickSound;

    // Background ambient music clip
    private Clip musicClip;
    private byte[] ambientMusicData;

    // Concurrency limiter to prevent overlapping SFX from stacking into loud distortion
    private final AtomicInteger activeSfxCount = new AtomicInteger(0);
    private static final int MAX_CONCURRENT_SFX = 4;

    private final ExecutorService soundPool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "SpaceShooter-Audio");
        t.setDaemon(true);
        return t;
    });

    private SoundManager() {
        try {
            initSoundBuffers();
            initAmbientMusic();
        } catch (Throwable t) {
            audioSupported = false;
            System.err.println("Audio initialization notice: " + t.getMessage());
        }
    }

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    private void initSoundBuffers() {
        shootSound = generateSmoothLaser(520f, 260f, 0.065f);
        explosionSound = generateWarmExplosion(0.24f);
        hitSound = generateSoftPop(160f, 0.030f);
        powerUpSound = generateHarmonicChime(new float[]{523.25f, 659.25f, 783.99f, 1046.50f}, 0.07f);
        playerDamageSound = generateHullThud(85f, 0.13f);
        bossWarningSound = generateAmbientAlarm(110f, 0.18f);
        levelCompleteSound = generateMelodicFanfare(new float[]{440f, 554.37f, 659.25f, 880f}, 0.11f);
        gameOverSound = generateMelodicDescend(new float[]{440f, 370f, 311f, 220f}, 0.13f);
        menuClickSound = generateSoftPop(700f, 0.020f);
    }

    private void initAmbientMusic() {
        if (!audioSupported) return;
        ambientMusicData = generateAmbientSpaceMusic(8.0f);
        try {
            musicClip = AudioSystem.getClip();
            musicClip.open(FORMAT, ambientMusicData, 0, ambientMusicData.length);
            applyVolume(musicClip, getEffectiveMusicVolume());
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            // Background music optional; falls back gracefully
        }
    }

    public void playShoot() {
        playSfx(shootSound, 0.70f);
    }

    public void playExplosion() {
        playSfx(explosionSound, 0.85f);
    }

    public void playHit() {
        playSfx(hitSound, 0.50f);
    }

    public void playPowerUp() {
        playSfx(powerUpSound, 0.80f);
    }

    public void playPlayerDamage() {
        playSfx(playerDamageSound, 0.75f);
    }

    public void playBossWarning() {
        playSfx(bossWarningSound, 0.70f);
    }

    public void playLevelComplete() {
        playSfx(levelCompleteSound, 0.85f);
    }

    public void playGameOver() {
        playSfx(gameOverSound, 0.80f);
    }

    public void playMenuClick() {
        playSfx(menuClickSound, 0.50f);
    }

    private void playSfx(byte[] audioData, float individualGain) {
        if (muted || !audioSupported || audioData == null) {
            return;
        }

        // Limit concurrent SFX to prevent harsh stacking volume
        if (activeSfxCount.get() >= MAX_CONCURRENT_SFX) {
            return;
        }

        activeSfxCount.incrementAndGet();
        soundPool.submit(() -> {
            Clip clip = null;
            try {
                clip = AudioSystem.getClip();
                clip.open(FORMAT, audioData, 0, audioData.length);
                applyVolume(clip, getEffectiveSfxVolume() * individualGain);
                clip.start();

                Clip finalClip = clip;
                finalClip.addLineListener(event -> {
                    if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                        finalClip.close();
                        activeSfxCount.decrementAndGet();
                    }
                });
            } catch (LineUnavailableException | IllegalArgumentException e) {
                activeSfxCount.decrementAndGet();
            } catch (Exception ignored) {
                activeSfxCount.decrementAndGet();
            }
        });
    }

    private void applyVolume(Clip clip, float volumeFactor) {
        if (clip == null) return;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                if (muted || volumeFactor <= 0.001f) {
                    gainControl.setValue(gainControl.getMinimum());
                } else {
                    float dB = (float) (Math.log10(Math.max(0.0001, volumeFactor)) * 20.0);
                    dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
                    gainControl.setValue(dB);
                }
            }
        } catch (Exception ignored) {}
    }

    public float getEffectiveSfxVolume() {
        return masterVolume * sfxVolume;
    }

    public float getEffectiveMusicVolume() {
        return masterVolume * musicVolume;
    }

    public void updateMusicVolume() {
        if (musicClip != null && musicClip.isOpen()) {
            applyVolume(musicClip, getEffectiveMusicVolume());
        }
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        updateMusicVolume();
    }

    public void toggleMute() {
        this.muted = !this.muted;
        updateMusicVolume();
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(float masterVolume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, masterVolume));
        updateMusicVolume();
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = Math.max(0.0f, Math.min(1.0f, sfxVolume));
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, musicVolume));
        updateMusicVolume();
    }

    // =========================================================
    //  16-Bit Smooth Sound Synthesis Helpers (Warm, Zero Harshness)
    // =========================================================

    /**
     * Velvety sine-glide laser with smooth attack and exponential decay.
     * Replaces harsh square wave chirps.
     */
    private byte[] generateSmoothLaser(float startFreq, float endFreq, float durationSec) {
        int totalSamples = (int) (SAMPLE_RATE * durationSec);
        byte[] buffer = new byte[totalSamples * 2];
        double phase = 0;

        for (int i = 0; i < totalSamples; i++) {
            float progress = (float) i / totalSamples;
            float freq = startFreq + (endFreq - startFreq) * (float) Math.pow(progress, 1.4);
            phase += 2.0 * Math.PI * freq / SAMPLE_RATE;

            // Smooth attack to avoid clicks, exponential decay envelope
            double attack = Math.min(1.0, (double) i / (SAMPLE_RATE * 0.005));
            double decay = Math.exp(-progress * 5.0);
            double envelope = attack * decay;

            // Pure sine with subtle second harmonic for warmth
            double sample = Math.sin(phase) * 0.85 + Math.sin(phase * 2.0) * 0.15;
            short pcm = (short) (sample * envelope * 15000);

            buffer[i * 2] = (byte) (pcm & 0xFF);
            buffer[i * 2 + 1] = (byte) ((pcm >> 8) & 0xFF);
        }
        return buffer;
    }

    /**
     * Cinematic deep explosion with 50 Hz sub-bass rumble and low-pass filtered noise.
     * Completely eliminates sizzling high-frequency crackle.
     */
    private byte[] generateWarmExplosion(float durationSec) {
        int totalSamples = (int) (SAMPLE_RATE * durationSec);
        byte[] buffer = new byte[totalSamples * 2];
        Random rand = new Random(4242);
        float filterState = 0;

        for (int i = 0; i < totalSamples; i++) {
            float progress = (float) i / totalSamples;

            // Low-pass filtered pink/brown noise
            float white = rand.nextFloat() * 2.0f - 1.0f;
            filterState = filterState * 0.84f + white * 0.16f;

            // Sub-bass sine drop for physical impact
            double subPhase = 2.0 * Math.PI * (52.0 - progress * 24.0) * (i / SAMPLE_RATE);
            double subBass = Math.sin(subPhase);

            double sample = filterState * 0.65 + subBass * 0.35;
            double envelope = Math.pow(1.0 - progress, 2.0);
            short pcm = (short) (sample * envelope * 18000);

            buffer[i * 2] = (byte) (pcm & 0xFF);
            buffer[i * 2 + 1] = (byte) ((pcm >> 8) & 0xFF);
        }
        return buffer;
    }

    /**
     * Soft dull pop for bullet impacts or subtle button clicks.
     */
    private byte[] generateSoftPop(float freq, float durationSec) {
        int totalSamples = (int) (SAMPLE_RATE * durationSec);
        byte[] buffer = new byte[totalSamples * 2];
        double phase = 0;

        for (int i = 0; i < totalSamples; i++) {
            phase += 2.0 * Math.PI * freq / SAMPLE_RATE;
            float progress = (float) i / totalSamples;
            double envelope = Math.sin(progress * Math.PI) * Math.exp(-progress * 4.0);

            short pcm = (short) (Math.sin(phase) * envelope * 11000);
            buffer[i * 2] = (byte) (pcm & 0xFF);
            buffer[i * 2 + 1] = (byte) ((pcm >> 8) & 0xFF);
        }
        return buffer;
    }

    /**
     * Pleasant celesta/bell harmonic arpeggio for power-up collection.
     */
    private byte[] generateHarmonicChime(float[] freqs, float noteDurationSec) {
        int noteSamples = (int) (SAMPLE_RATE * noteDurationSec);
        int totalSamples = noteSamples * freqs.length;
        byte[] buffer = new byte[totalSamples * 2];

        for (int noteIdx = 0; noteIdx < freqs.length; noteIdx++) {
            float freq = freqs[noteIdx];
            double phase = 0;

            for (int i = 0; i < noteSamples; i++) {
                int sampleIdx = noteIdx * noteSamples + i;
                phase += 2.0 * Math.PI * freq / SAMPLE_RATE;

                float progress = (float) i / noteSamples;
                double envelope = Math.exp(-progress * 3.5);
                // Fundamental + octave harmonic
                double sample = Math.sin(phase) * 0.8 + Math.sin(phase * 2.0) * 0.2;

                short pcm = (short) (sample * envelope * 13000);
                buffer[sampleIdx * 2] = (byte) (pcm & 0xFF);
                buffer[sampleIdx * 2 + 1] = (byte) ((pcm >> 8) & 0xFF);
            }
        }
        return buffer;
    }

    /**
     * Dull hull impact thud.
     */
    private byte[] generateHullThud(float freq, float durationSec) {
        int totalSamples = (int) (SAMPLE_RATE * durationSec);
        byte[] buffer = new byte[totalSamples * 2];
        double phase = 0;

        for (int i = 0; i < totalSamples; i++) {
            phase += 2.0 * Math.PI * (freq - (i / SAMPLE_RATE) * 35.0) / SAMPLE_RATE;
            float progress = (float) i / totalSamples;
            double envelope = Math.exp(-progress * 6.0);

            short pcm = (short) (Math.sin(phase) * envelope * 16000);
            buffer[i * 2] = (byte) (pcm & 0xFF);
            buffer[i * 2 + 1] = (byte) ((pcm >> 8) & 0xFF);
        }
        return buffer;
    }

    /**
     * Low ambient pulsing alarm for boss warning.
     */
    private byte[] generateAmbientAlarm(float freq, float durationSec) {
        int totalSamples = (int) (SAMPLE_RATE * durationSec);
        byte[] buffer = new byte[totalSamples * 2];
        double phase = 0;

        for (int i = 0; i < totalSamples; i++) {
            phase += 2.0 * Math.PI * freq / SAMPLE_RATE;
            float progress = (float) i / totalSamples;
            double envelope = Math.sin(progress * Math.PI);

            short pcm = (short) (Math.sin(phase) * envelope * 12000);
            buffer[i * 2] = (byte) (pcm & 0xFF);
            buffer[i * 2 + 1] = (byte) ((pcm >> 8) & 0xFF);
        }
        return buffer;
    }

    private byte[] generateMelodicFanfare(float[] freqs, float noteDurationSec) {
        int noteSamples = (int) (SAMPLE_RATE * noteDurationSec);
        int totalSamples = noteSamples * freqs.length;
        byte[] buffer = new byte[totalSamples * 2];

        for (int noteIdx = 0; noteIdx < freqs.length; noteIdx++) {
            float freq = freqs[noteIdx];
            double phase = 0;

            for (int i = 0; i < noteSamples; i++) {
                int sampleIdx = noteIdx * noteSamples + i;
                phase += 2.0 * Math.PI * freq / SAMPLE_RATE;

                float progress = (float) i / noteSamples;
                double envelope = Math.min(1.0, 1.0 - progress * 0.45);
                double sample = Math.sin(phase) * 0.85 + Math.sin(phase * 2.0) * 0.15;

                short pcm = (short) (sample * envelope * 13500);
                buffer[sampleIdx * 2] = (byte) (pcm & 0xFF);
                buffer[sampleIdx * 2 + 1] = (byte) ((pcm >> 8) & 0xFF);
            }
        }
        return buffer;
    }

    private byte[] generateMelodicDescend(float[] freqs, float noteDurationSec) {
        return generateHarmonicChime(freqs, noteDurationSec);
    }

    /**
     * Synthesizes an 8-second ambient space synth pad that loops seamlessly in the background.
     * Calming chords in D minor (Dm - Bb - C - Am) at a gentle, non-intrusive volume.
     */
    private byte[] generateAmbientSpaceMusic(float totalSec) {
        int totalSamples = (int) (SAMPLE_RATE * totalSec);
        byte[] buffer = new byte[totalSamples * 2];

        float[][] chords = {
                {146.83f, 220.00f, 349.23f}, // Dm (D3, A3, F4)
                {116.54f, 174.61f, 293.66f}, // Bb (Bb2, F3, D4)
                {130.81f, 196.00f, 329.63f}, // C  (C3, G3, E4)
                {110.00f, 164.81f, 277.18f}  // Am/C# (A2, E3, C#4)
        };

        int samplesPerChord = totalSamples / 4;

        for (int i = 0; i < totalSamples; i++) {
            int chordIdx = (i / samplesPerChord) % 4;
            int chordSample = i % samplesPerChord;
            float chordProg = (float) chordSample / samplesPerChord;
            double chordEnv = Math.sin(chordProg * Math.PI);

            double sum = 0;
            float[] notes = chords[chordIdx];
            for (float note : notes) {
                double phase = 2.0 * Math.PI * note * (i / SAMPLE_RATE);
                sum += Math.sin(phase) * 0.33;
            }

            // Soft atmospheric harmonic shimmer
            double shimmer = Math.sin(2.0 * Math.PI * 440.0 * (i / SAMPLE_RATE)) * 0.04;
            sum += shimmer;

            short pcm = (short) ((sum * chordEnv) * 6000);
            buffer[i * 2] = (byte) (pcm & 0xFF);
            buffer[i * 2 + 1] = (byte) ((pcm >> 8) & 0xFF);
        }

        return buffer;
    }
}
