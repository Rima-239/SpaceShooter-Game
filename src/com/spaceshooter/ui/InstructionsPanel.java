package com.spaceshooter.ui;

import com.spaceshooter.entities.Star;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Modern flight manual and mission briefing screen with interactive control cards,
 * weapon blueprints, hazard intel, and animated starfield background.
 */
public class InstructionsPanel extends JPanel {

    private final Runnable onBack;
    private final List<Star> stars = new ArrayList<>();
    private final Timer animTimer;

    public InstructionsPanel(Runnable onBack) {
        this.onBack = onBack;

        setLayout(new BorderLayout());
        setBackground(UIUtils.COLOR_BG_DEEP);

        // Populate background stars
        Random rand = new Random();
        for (int i = 0; i < 90; i++) {
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

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("MISSION BRIEFING & CONTROLS", SwingConstants.CENTER);
        titleLabel.setFont(UIUtils.FONT_TITLE);
        titleLabel.setForeground(UIUtils.NEON_CYAN);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel subLabel = new JLabel("Study combat maneuvers and weapon upgrades before launching", SwingConstants.CENTER);
        subLabel.setFont(UIUtils.FONT_SUBTITLE);
        subLabel.setForeground(UIUtils.TEXT_MUTED);
        headerPanel.add(subLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // Center Content Grid (4 Info Cards)
        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        grid.add(createCard("GAME CONTROLS", UIUtils.NEON_CYAN, new String[]{
                "Arrow Keys / WASD \u2013 Move spaceship",
                "Space \u2013 Shoot plasma cannons",
                "ESC \u2013 Open Pause Menu",
                "P \u2013 Pause / Resume flight",
                "F11 \u2013 Toggle Fullscreen mode",
                "ESC (while Fullscreen) \u2013 Exit Fullscreen",
                "M \u2013 Mute / Unmute Audio",
                "L \u2013 Open Galactic Leaderboard"
        }));

        grid.add(createCard("WEAPON SYSTEMS", UIUtils.NEON_GREEN, new String[]{
                "LEVEL 1: Single Center Plasma Bolt",
                "LEVEL 2: Twin High-Rate Plasma Cannons",
                "LEVEL 3: Triple Heavy Spread Plasma Cannons",
                "POWER-UP (+): Collect floating green energy capsules",
                "UPGRADE EFFECT: Instantly enhances cannon tier up to Level 3"
        }));

        grid.add(createCard("TARGET INTEL & HAZARDS", UIUtils.NEON_RED, new String[]{
                "ALIEN INTERCEPTORS: Red attack drones (+10 pts)",
                "SPACE ASTEROIDS: Dense tumbling rock (+5 pts)",
                "DREADNOUGHT BOSS: Armored flagship with heavy plasma battery",
                "BOSS HEALTH: Multiplies with each sector wave",
                "DEFENSE: Boss spawns after required wave enemies are cleared"
        }));

        grid.add(createCard("SURVIVAL & RULES", UIUtils.NEON_GOLD, new String[]{
                "SHIP SHIELDS: 100 HP max hull integrity",
                "DAMAGE: Enemy/Asteroid collision (-20 HP), Boss Blast (-10 HP)",
                "LIVES: 3 Pilot Respawns per mission",
                "RESPAWN SHIELD: Shimmering bubble grants 1.5s invulnerability",
                "GOAL: Clear all 3 sectors, defeat the bosses, achieve Galactic High Score!"
        }));

        add(grid, BorderLayout.CENTER);

        // Bottom Button Bar
        JPanel bottomBar = new JPanel();
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(12, 10, 20, 10));

        JButton backBtn = UIUtils.createGlowingButton("RETURN TO MAIN MENU", new Color(40, 50, 80), UIUtils.NEON_CYAN, 280, 46);
        backBtn.addActionListener(e -> {
            stopAnimation();
            this.onBack.run();
        });
        bottomBar.add(backBtn);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createCard(String title, Color accentColor, String[] lines) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                UIUtils.drawGlassCard(g2d, 0, 0, getWidth() - 1, getHeight() - 1, accentColor);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;

        JLabel head = new JLabel(title);
        head.setFont(UIUtils.FONT_HEADER);
        head.setForeground(accentColor);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(head, gbc);

        for (int i = 0; i < lines.length; i++) {
            JLabel lineLabel = new JLabel(lines[i]);
            lineLabel.setFont(UIUtils.FONT_BODY);
            lineLabel.setForeground(UIUtils.TEXT_WHITE);
            gbc.gridy = i + 1;
            gbc.insets = new Insets(3, 4, 3, 0);
            card.add(lineLabel, gbc);
        }

        return card;
    }

    public void startAnimation() {
        if (!animTimer.isRunning()) {
            animTimer.start();
        }
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

        GradientPaint bgGrad = new GradientPaint(
                0, 0, UIUtils.COLOR_BG_DEEP,
                getWidth(), getHeight(), new Color(2, 4, 10));
        g2d.setPaint(bgGrad);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        for (Star star : stars) {
            star.draw(g2d);
        }
    }
}
