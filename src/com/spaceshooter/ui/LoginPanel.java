package com.spaceshooter.ui;

import com.spaceshooter.audio.SoundManager;
import com.spaceshooter.entities.Star;
import com.spaceshooter.managers.ScoreManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Pilot registration screen with crystal-clear username input visibility,
 * high-contrast dark futuristic styling, animated starfield, and live score recall.
 */
public class LoginPanel extends JPanel {

    private final Consumer<String> onLaunchGame;
    private final Runnable onBack;
    private final ScoreManager scoreManager = new ScoreManager();

    private final JTextField usernameField;
    private final JLabel errorLabel;
    private final JLabel personalBestLabel;
    private final List<Star> stars = new ArrayList<>();
    private final Timer animTimer;

    public LoginPanel(Consumer<String> onLaunchGame, Runnable onBack) {
        this.onLaunchGame = onLaunchGame;
        this.onBack = onBack;

        setLayout(new GridBagLayout());
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

        // Center card container
        JPanel cardPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                UIUtils.drawGlassCard(g2d, 0, 0, getWidth() - 1, getHeight() - 1, UIUtils.NEON_CYAN);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(28, 40, 28, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Header Title
        JLabel titleLabel = new JLabel("PILOT REGISTRATION", SwingConstants.CENTER);
        titleLabel.setFont(UIUtils.FONT_TITLE);
        titleLabel.setForeground(UIUtils.NEON_CYAN);
        gbc.gridy = 0;
        cardPanel.add(titleLabel, gbc);

        // Subtitle
        JLabel subLabel = new JLabel("Enter your pilot callsign to record your galactic honors", SwingConstants.CENTER);
        subLabel.setFont(UIUtils.FONT_SUBTITLE);
        subLabel.setForeground(UIUtils.TEXT_MUTED);
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 6, 16, 6);
        cardPanel.add(subLabel, gbc);

        // Highly Visible, High-Contrast Username Input Field
        String lastPlayer = scoreManager.getLastPlayerName();
        usernameField = new JTextField(lastPlayer.equalsIgnoreCase("Pilot") ? "" : lastPlayer) {
            private boolean focused = false;
            {
                setOpaque(false);
                addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        focused = true;
                        repaint();
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        focused = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Deep contrast dark navy background
                g2d.setColor(new Color(12, 18, 38));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                super.paintComponent(g);

                // Placeholder text when empty and not focused
                if (getText().isEmpty() && !focused) {
                    g2d.setFont(getFont().deriveFont(Font.ITALIC, 17f));
                    g2d.setColor(new Color(110, 145, 185));
                    String placeholder = "Enter Pilot Callsign...";
                    int strW = g2d.getFontMetrics().stringWidth(placeholder);
                    int strH = g2d.getFontMetrics().getAscent();
                    g2d.drawString(placeholder, (getWidth() - strW) / 2, (getHeight() + strH) / 2 - 3);
                }

                g2d.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(focused ? UIUtils.NEON_CYAN : new Color(40, 75, 125));
                g2d.setStroke(new BasicStroke(focused ? 2.5f : 1.5f));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                g2d.dispose();
            }
        };

        usernameField.setFont(new Font("Segoe UI", Font.BOLD, 22));
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(UIUtils.NEON_CYAN);
        usernameField.setSelectionColor(new Color(0, 130, 240));
        usernameField.setSelectedTextColor(Color.WHITE);
        usernameField.setPreferredSize(new Dimension(360, 52));
        usernameField.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        usernameField.setHorizontalAlignment(JTextField.CENTER);

        gbc.gridy = 2;
        gbc.insets = new Insets(8, 6, 4, 6);
        cardPanel.add(usernameField, gbc);

        // Personal Best Label
        int pb = scoreManager.getPlayerHighScore(usernameField.getText());
        personalBestLabel = new JLabel(pb > 0 ? "Personal Best: " + pb + " pts" : "Ready for maiden voyage", SwingConstants.CENTER);
        personalBestLabel.setFont(UIUtils.FONT_BODY);
        personalBestLabel.setForeground(UIUtils.NEON_GOLD);
        gbc.gridy = 3;
        gbc.insets = new Insets(2, 6, 4, 6);
        cardPanel.add(personalBestLabel, gbc);

        // Validation Error Label
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(UIUtils.FONT_BODY);
        errorLabel.setForeground(UIUtils.NEON_RED);
        gbc.gridy = 4;
        gbc.insets = new Insets(2, 6, 14, 6);
        cardPanel.add(errorLabel, gbc);

        // Live Document Listener to update Personal Best & validation
        usernameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updatePB(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updatePB(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updatePB(); }
        });

        // Launch Action when pressing Enter in text field
        usernameField.addActionListener(e -> attemptLaunch());

        // Start Game Button
        JButton launchButton = UIUtils.createGlowingButton(
                "START GAME",
                new Color(0, 100, 170),
                UIUtils.NEON_CYAN,
                360, 48);
        launchButton.addActionListener(e -> attemptLaunch());
        gbc.gridy = 5;
        gbc.insets = new Insets(4, 6, 8, 6);
        cardPanel.add(launchButton, gbc);

        // Back Button
        JButton backButton = UIUtils.createGlowingButton(
                "BACK TO MENU",
                new Color(40, 45, 70),
                UIUtils.NEON_PURPLE,
                360, 42);
        backButton.addActionListener(e -> {
            stopAnimation();
            this.onBack.run();
        });
        gbc.gridy = 6;
        gbc.insets = new Insets(4, 6, 4, 6);
        cardPanel.add(backButton, gbc);

        add(cardPanel);
    }

    private void updatePB() {
        String name = usernameField.getText().trim();
        if (name.isEmpty()) {
            personalBestLabel.setText("Enter your callsign");
            errorLabel.setText(" ");
            return;
        }
        if (name.length() > 25) {
            errorLabel.setText("Callsign must be 25 characters or fewer!");
            return;
        } else {
            errorLabel.setText(" ");
        }

        int pb = scoreManager.getPlayerHighScore(name);
        if (pb > 0) {
            personalBestLabel.setText("Personal Best: " + pb + " pts");
        } else {
            personalBestLabel.setText("New Pilot Profile");
        }
    }

    private void attemptLaunch() {
        String rawName = usernameField.getText();
        String validationError = ScoreManager.validatePlayerName(rawName);
        if (validationError != null) {
            errorLabel.setText(validationError);
            SoundManager.getInstance().playPlayerDamage();
            return;
        }

        String safeName = ScoreManager.sanitizePlayerName(rawName);
        stopAnimation();
        SoundManager.getInstance().playMenuClick();
        onLaunchGame.accept(safeName);
    }

    public void startAnimation() {
        if (!animTimer.isRunning()) {
            animTimer.start();
        }
        usernameField.requestFocusInWindow();
        if (!usernameField.getText().isEmpty()) {
            usernameField.selectAll();
        }
        updatePB();
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

        // Background space gradient
        GradientPaint bgGrad = new GradientPaint(
                0, 0, UIUtils.COLOR_BG_DEEP,
                getWidth(), getHeight(), new Color(2, 4, 10));
        g2d.setPaint(bgGrad);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Background stars
        for (Star star : stars) {
            star.draw(g2d);
        }
    }
}
