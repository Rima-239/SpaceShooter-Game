package com.spaceshooter.ui;

import com.spaceshooter.audio.SoundManager;
import com.spaceshooter.entities.Star;
import com.spaceshooter.managers.ScoreManager;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main Menu screen featuring an animated deep-space parallax starfield,
 * glowing neon typography, audio controls, galactic high-score ticker,
 * and arcade navigation buttons.
 */
public class MenuPanel extends JPanel {

    private final Runnable onStartGame;
    private final Runnable onLeaderboard;
    private final Runnable onInstructions;
    private final Runnable onExit;

    private final ScoreManager scoreManager = new ScoreManager();
    private final List<Star> stars = new ArrayList<>();
    private final Timer animTimer;
    private final JLabel topScoreLabel;
    private final JButton soundButton;

    public MenuPanel(Runnable onStartGame, Runnable onExit) {
        this(onStartGame, null, null, onExit);
    }

    public MenuPanel(
            Runnable onStartGame,
            Runnable onLeaderboard,
            Runnable onInstructions,
            Runnable onExit) {

        this.onStartGame = onStartGame;
        this.onLeaderboard = onLeaderboard != null ? onLeaderboard : () -> LeaderboardWindow.showLeaderboard(this);
        this.onInstructions = onInstructions != null ? onInstructions : () -> {};
        this.onExit = onExit;

        setLayout(new GridBagLayout());
        setBackground(UIUtils.COLOR_BG_DEEP);

        // Populate background stars
        Random rand = new Random();
        for (int i = 0; i < 110; i++) {
            stars.add(new Star(rand.nextInt(1920), rand.nextInt(1080), rand.nextInt(3) + 1, rand.nextInt(3) + 1));
        }

        animTimer = new Timer(25, e -> {
            int w = Math.max(100, getWidth());
            int h = Math.max(100, getHeight());
            for (Star star : stars) {
                star.update(w, h);
            }
            repaint();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 12, 6, 12);

        // Arcade Game Logo Title
        JLabel title = new JLabel("SPACE SHOOTER", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 52));
        title.setForeground(UIUtils.NEON_CYAN);
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 12, 2, 12);
        add(title, gbc);

        // Subtitle
        JLabel subtitle = new JLabel("DEFEND THE GALAXY", SwingConstants.CENTER);
        subtitle.setFont(UIUtils.FONT_SUBTITLE);
        subtitle.setForeground(UIUtils.TEXT_MUTED);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 12, 24, 12);
        add(subtitle, gbc);

        // Buttons
        JButton startBtn = UIUtils.createGlowingButton("START GAME", new Color(0, 100, 180), UIUtils.NEON_CYAN, 260, 48);
        startBtn.addActionListener(e -> {
            stopAnimation();
            this.onStartGame.run();
        });
        gbc.gridy = 2;
        gbc.insets = new Insets(6, 12, 6, 12);
        add(startBtn, gbc);

        JButton leadBtn = UIUtils.createGlowingButton("LEADERBOARD", new Color(20, 45, 90), UIUtils.NEON_BLUE, 260, 46);
        leadBtn.addActionListener(e -> this.onLeaderboard.run());
        gbc.gridy = 3;
        add(leadBtn, gbc);

        JButton instBtn = UIUtils.createGlowingButton("INSTRUCTIONS", new Color(40, 25, 80), UIUtils.NEON_PURPLE, 260, 46);
        instBtn.addActionListener(e -> {
            stopAnimation();
            this.onInstructions.run();
        });
        gbc.gridy = 4;
        add(instBtn, gbc);

        // Sound Toggle Button
        soundButton = UIUtils.createGlowingButton(
                getSoundButtonText(),
                new Color(25, 45, 55),
                UIUtils.NEON_GREEN,
                260, 42);
        soundButton.addActionListener(e -> {
            SoundManager.getInstance().toggleMute();
            soundButton.setText(getSoundButtonText());
        });
        gbc.gridy = 5;
        add(soundButton, gbc);

        JButton exitBtn = UIUtils.createGlowingButton("EXIT MISSION", new Color(60, 20, 30), UIUtils.NEON_RED, 260, 44);
        exitBtn.addActionListener(e -> this.onExit.run());
        gbc.gridy = 6;
        gbc.insets = new Insets(6, 12, 20, 12);
        add(exitBtn, gbc);

        // Galactic High Score Ticker Banner
        topScoreLabel = new JLabel("", SwingConstants.CENTER);
        topScoreLabel.setFont(UIUtils.FONT_HUD);
        topScoreLabel.setForeground(UIUtils.NEON_GOLD);
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 12, 10, 12);
        add(topScoreLabel, gbc);

        updateHighscoreBanner();
    }

    private String getSoundButtonText() {
        SoundManager sm = SoundManager.getInstance();
        if (sm.isMuted()) {
            return "\uD83D\uDD07 AUDIO: MUTED (M)";
        } else {
            int pct = (int) Math.round(sm.getMasterVolume() * 100);
            return "\uD83D\uDD0A AUDIO: " + pct + "% (M)";
        }
    }

    public void updateHighscoreBanner() {
        ScoreManager.ScoreRecord top = scoreManager.getTopScore();
        if (top != null) {
            topScoreLabel.setText("\uD83C\uDFC6 GALACTIC RECORD: " + top.playerName + " \u2014 " + top.score + " PTS (Sector " + top.levelReached + ")");
        } else {
            topScoreLabel.setText("\uD83D\uDE80 READY FOR LAUNCH \u2014 CLAIM THE FIRST RECORD!");
        }
        soundButton.setText(getSoundButtonText());
    }

    public void startAnimation() {
        if (!animTimer.isRunning()) {
            animTimer.start();
        }
        updateHighscoreBanner();
    }

    public void stopAnimation() {
        if (animTimer.isRunning()) {
            animTimer.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Deep space gradient
        GradientPaint bgGrad = new GradientPaint(
                0, 0, UIUtils.COLOR_BG_DEEP,
                getWidth(), getHeight(), new Color(2, 4, 10));
        g2d.setPaint(bgGrad);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Parallax stars
        for (Star star : stars) {
            star.draw(g2d);
        }
    }
}
