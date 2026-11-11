package com.spaceshooter;

import com.spaceshooter.audio.SoundManager;
import com.spaceshooter.entities.Asteroid;
import com.spaceshooter.entities.Boss;
import com.spaceshooter.entities.BossBullet;
import com.spaceshooter.entities.Bullet;
import com.spaceshooter.entities.Enemy;
import com.spaceshooter.entities.Particle;
import com.spaceshooter.entities.PowerUp;
import com.spaceshooter.entities.Star;
import com.spaceshooter.managers.GameManager;
import com.spaceshooter.managers.ScoreManager;
import com.spaceshooter.ui.LeaderboardWindow;
import com.spaceshooter.ui.UIUtils;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;

/**
 * Main interactive gameplay canvas with high-performance 60 FPS loop,
 * futuristic HUD, screen shake, particle rendering, responsive coordinates,
 * and arcade overlays for pause, sector completion, and game over.
 */
public class GamePanel extends JPanel {

    private GameManager gameManager;
    private final Timer gameTimer;

    private GameState gameState = GameState.PLAYING;
    private String playerName;

    private final ScoreManager scoreManager = new ScoreManager();
    private boolean scoreSaved = false;
    private boolean initializedLayout = false;

    private final Runnable onReturnToMenu;

    // Button dimensions for menus and overlays
    private final int buttonWidth = 230;
    private final int buttonHeight = 44;

    // Mouse hover tracking
    private Point mousePoint = new Point(-1, -1);

    public GamePanel(String playerName) {
        this(playerName, null);
    }

    public GamePanel(String playerName, Runnable onReturnToMenu) {
        this.playerName = ScoreManager.sanitizePlayerName(playerName);
        this.onReturnToMenu = onReturnToMenu;

        setBackground(UIUtils.COLOR_BG_DEEP);
        setFocusable(true);
        setupKeyBindings();

        gameManager = new GameManager(800, 600);

        registerInputHandler();
        registerMouseHandler();

        gameTimer = new Timer(16, e -> {
            if (gameState == GameState.PLAYING) {
                updateGame();

                if (gameManager.isGameOver()) {
                    gameState = GameState.GAME_OVER;
                    saveFinalScore();
                }
            }
            repaint();
        });

        gameTimer.start();
    }

    private void setupKeyBindings() {
        // Leaderboard shortcut (L)
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("L"), "openLeaderboard");
        getActionMap().put("openLeaderboard", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LeaderboardWindow.showLeaderboard(GamePanel.this);
            }
        });

        // Mute shortcut (M)
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("M"), "toggleMute");
        getActionMap().put("toggleMute", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SoundManager.getInstance().toggleMute();
            }
        });

        // Pause shortcut (P)
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("P"), "togglePauseKey");
        getActionMap().put("togglePauseKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });
    }

    private void updateGame() {
        int w = Math.max(100, getWidth());
        int h = Math.max(100, getHeight());

        // Center player on the initial frame once layout width is known
        if (!initializedLayout && w > 100 && h > 100) {
            initializedLayout = true;
            if (w != 800 || h != 600) {
                gameManager.getPlayer().respawn(w, h);
            }
        }

        gameManager.update(w, h);
    }

    public void registerInputHandler() {
        for (KeyListener listener : getKeyListeners()) {
            removeKeyListener(listener);
        }

        InputHandler inputHandler = new InputHandler(gameManager.getPlayer(), this);
        addKeyListener(inputHandler);

        setFocusable(true);
        requestFocusInWindow();
    }

    private void registerMouseHandler() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePoint = e.getPoint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int x = e.getX();
                int y = e.getY();

                // PAUSED SCREEN
                if (gameState == GameState.PAUSED) {
                    // Volume adjustment controls: [ - ] and [ + ] and Mute
                    if (x >= centerX - 145 && x <= centerX - 95 && y >= centerY - 45 && y <= centerY - 15) {
                        float v = SoundManager.getInstance().getMasterVolume() - 0.10f;
                        SoundManager.getInstance().setMasterVolume(Math.max(0.0f, v));
                        SoundManager.getInstance().playMenuClick();
                    } else if (x >= centerX + 95 && x <= centerX + 145 && y >= centerY - 45 && y <= centerY - 15) {
                        float v = SoundManager.getInstance().getMasterVolume() + 0.10f;
                        SoundManager.getInstance().setMasterVolume(Math.min(1.0f, v));
                        SoundManager.getInstance().playMenuClick();
                    } else if (x >= centerX - 85 && x <= centerX + 85 && y >= centerY - 45 && y <= centerY - 15) {
                        SoundManager.getInstance().toggleMute();
                    }
                    // RESUME button
                    else if (isInsideButton(x, y, centerX, centerY + 20)) {
                        resumeGame();
                    }
                    // MAIN MENU button
                    else if (isInsideButton(x, y, centerX, centerY + 75)) {
                        returnToMainMenu();
                    }
                    // EXIT GAME button
                    else if (isInsideButton(x, y, centerX, centerY + 130)) {
                        confirmAndExit();
                    }
                }
                // GAME OVER SCREEN
                else if (gameState == GameState.GAME_OVER) {
                    // PLAY AGAIN
                    if (isInsideButton(x, y, centerX, centerY + 15)) {
                        restartGame();
                    }
                    // LEADERBOARD
                    else if (isInsideButton(x, y, centerX, centerY + 68)) {
                        LeaderboardWindow.showLeaderboard(GamePanel.this);
                    }
                    // MAIN MENU
                    else if (isInsideButton(x, y, centerX, centerY + 121)) {
                        returnToMainMenu();
                    }
                    // EXIT GAME
                    else if (isInsideButton(x, y, centerX, centerY + 174)) {
                        confirmAndExit();
                    }
                }
                // FALLBACK MENU SCREEN
                else if (gameState == GameState.MENU) {
                    if (isInsideButton(x, y, centerX, centerY + 30)) {
                        restartGame();
                    }
                }

                requestFocusInWindow();
            }
        });
    }

    public void confirmAndExit() {
        int choice = JOptionPane.showOptionDialog(
                this,
                "Are you sure you want to exit SpaceShooter?",
                "Exit SpaceShooter",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"YES – Exit Game", "NO – Continue Playing"},
                "NO – Continue Playing");

        if (choice == 0) {
            System.exit(0);
        }
    }

    private void returnToMainMenu() {
        if (onReturnToMenu != null) {
            gameTimer.stop();
            onReturnToMenu.run();
        } else {
            gameState = GameState.MENU;
        }
    }

    private boolean isInsideButton(int mouseX, int mouseY, int centerX, int centerY) {
        return mouseX >= centerX - buttonWidth / 2
                && mouseX <= centerX + buttonWidth / 2
                && mouseY >= centerY - buttonHeight / 2
                && mouseY <= centerY + buttonHeight / 2;
    }

    public void pauseGame() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            stopPlayerInput();
        }
    }

    public void resumeGame() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            registerInputHandler();
            requestFocusInWindow();
        }
    }

    public void togglePause() {
        if (gameState == GameState.PLAYING) {
            pauseGame();
        } else if (gameState == GameState.PAUSED) {
            resumeGame();
        }
    }

    private void stopPlayerInput() {
        gameManager.getPlayer().setMovingUp(false);
        gameManager.getPlayer().setMovingDown(false);
        gameManager.getPlayer().setMovingLeft(false);
        gameManager.getPlayer().setMovingRight(false);
        gameManager.getPlayer().setShooting(false);
    }

    public void restartGame() {
        int w = Math.max(100, getWidth());
        int h = Math.max(100, getHeight());
        gameManager.restart(w, h);
        scoreSaved = false;
        gameState = GameState.PLAYING;
        registerInputHandler();
        requestFocusInWindow();
    }

    public void startGame() {
        restartGame();
    }

    public GameState getGameState() {
        return gameState;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public void setPlayerName(String playerName) {
        this.playerName = ScoreManager.sanitizePlayerName(playerName);
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Deep Space Parallax Background
        GradientPaint bgGrad = new GradientPaint(
                0, 0, UIUtils.COLOR_BG_DEEP,
                w, h, new Color(2, 4, 10));
        g2d.setPaint(bgGrad);
        g2d.fillRect(0, 0, w, h);

        // Apply Screen Shake offset
        int shakeX = gameManager.getScreenShakeX();
        int shakeY = gameManager.getScreenShakeY();
        g2d.translate(shakeX, shakeY);

        // 2. Stars
        for (Star star : gameManager.getStars()) {
            star.draw(g2d);
        }

        // 3. Bullets
        for (Bullet bullet : gameManager.getBullets()) {
            bullet.draw(g2d);
        }

        // 4. Enemies
        for (Enemy enemy : gameManager.getEnemies()) {
            enemy.draw(g2d);
        }

        // 5. Asteroids
        for (Asteroid asteroid : gameManager.getAsteroids()) {
            asteroid.draw(g2d);
        }

        // 6. Power-ups
        for (PowerUp powerUp : gameManager.getPowerUps()) {
            powerUp.draw(g2d);
        }

        // 7. Boss & Boss Bullets
        Boss boss = gameManager.getBoss();
        if (boss != null && !boss.isDestroyed()) {
            boss.draw(g2d);
            for (BossBullet bullet : boss.getBullets()) {
                bullet.draw(g2d);
            }
        }

        // 8. Player Spaceship
        gameManager.getPlayer().draw(g2d);

        // 9. Particle System (Sparks, Smoke, Shockwaves)
        for (Particle particle : gameManager.getParticles()) {
            particle.draw(g2d);
        }

        // Reset Screen Shake transform for HUD
        g2d.translate(-shakeX, -shakeY);

        // 10. Futuristic Heads-Up Display (HUD)
        drawFuturisticHUD(g2d, w, h);

        // 11. Boss Health Bar (Centered top)
        if (boss != null && !boss.isDestroyed()) {
            drawBossHealthBar(g2d, boss, w);
        }

        // 12. Level Complete Message
        if (gameManager.getLevelManager().isLevelComplete()) {
            drawLevelCompleteBanner(g2d, w, h);
        }

        // 13. Overlays
        if (gameState == GameState.PAUSED) {
            drawPauseScreen(g2d, w, h);
        } else if (gameState == GameState.GAME_OVER) {
            drawGameOverScreen(g2d, w, h);
        } else if (gameState == GameState.MENU) {
            drawMainMenu(g2d, w, h);
        }
    }

    private void drawFuturisticHUD(Graphics2D g2d, int w, int h) {
        int cardHeight = 82;

        // --- Left HUD Card: Pilot, Score, Wave Progress ---
        int leftCardWidth = 280;
        UIUtils.drawGlassCard(g2d, 16, 14, leftCardWidth, cardHeight, UIUtils.NEON_BLUE);

        // Pilot Name
        g2d.setFont(UIUtils.FONT_HUD.deriveFont(Font.BOLD, 13f));
        g2d.setColor(UIUtils.NEON_CYAN);
        g2d.drawString("\uD83D\uDE80 " + playerName.toUpperCase(), 28, 34);

        // Score
        g2d.setFont(UIUtils.FONT_HUD_LARGE.deriveFont(Font.BOLD, 22f));
        g2d.setColor(Color.WHITE);
        String scoreStr = String.format("%06d PTS", gameManager.getScore());
        g2d.drawString(scoreStr, 28, 60);

        // Sector / Wave Progress
        int currentLevel = gameManager.getLevelManager().getCurrentLevel();
        int def = gameManager.getLevelManager().getEnemiesDefeated();
        int req = gameManager.getLevelManager().getEnemiesRequired();

        g2d.setFont(UIUtils.FONT_HUD.deriveFont(12f));
        g2d.setColor(UIUtils.TEXT_MUTED);
        String waveStr = gameManager.getLevelManager().isGameComplete()
                ? "VICTORY"
                : (gameManager.getLevelManager().getState() == com.spaceshooter.managers.LevelManager.LevelState.BOSS_FIGHT
                ? "SECTOR " + currentLevel + " \u2022 BOSS ENGAGED"
                : "SECTOR " + currentLevel + " \u2022 " + def + "/" + req);
        g2d.drawString(waveStr, 28, 80);

        // Mini Enemy Progress Bar
        int barW = 100;
        int barX = leftCardWidth - barW - 6;
        int barY = 71;
        g2d.setColor(new Color(25, 35, 60));
        g2d.fillRoundRect(barX, barY, barW, 9, 6, 6);
        float progress = req > 0 ? Math.min(1.0f, (float) def / req) : 1.0f;
        g2d.setColor(UIUtils.NEON_GREEN);
        g2d.fillRoundRect(barX, barY, (int) (barW * progress), 9, 6, 6);
        g2d.setColor(new Color(60, 90, 140));
        g2d.drawRoundRect(barX, barY, barW, 9, 6, 6);

        // --- Center HUD Reminder Badge ---
        g2d.setFont(UIUtils.FONT_HUD.deriveFont(Font.BOLD, 12f));
        g2d.setColor(new Color(130, 160, 205));
        String pauseReminder = "Press shift+ESC to Pause  \u2022  shift+P Quick Pause";
        int prWidth = g2d.getFontMetrics().stringWidth(pauseReminder);
        g2d.drawString(pauseReminder, (w - prWidth) / 2, 30);

        // --- Right HUD Card: Shield Health, Lives, Weapon Level ---
        int rightCardWidth = 270;
        int rightX = w - rightCardWidth - 16;
        UIUtils.drawGlassCard(g2d, rightX, 14, rightCardWidth, cardHeight, UIUtils.NEON_PURPLE);

        // Shield Health Bar
        int hp = gameManager.getPlayerHealth();
        g2d.setFont(UIUtils.FONT_HUD.deriveFont(Font.BOLD, 13f));
        g2d.setColor(hp > 40 ? UIUtils.NEON_GREEN : (hp > 20 ? UIUtils.NEON_GOLD : UIUtils.NEON_RED));
        g2d.drawString("SHIELD INTEGRITY: " + hp + "%", rightX + 16, 34);

        int hpBarW = rightCardWidth - 32;
        int hpBarY = 40;
        g2d.setColor(new Color(30, 20, 40));
        g2d.fillRoundRect(rightX + 16, hpBarY, hpBarW, 10, 6, 6);

        Color hpColor = hp > 50 ? UIUtils.NEON_GREEN : (hp > 25 ? UIUtils.NEON_GOLD : UIUtils.NEON_RED);
        GradientPaint hpGrad = new GradientPaint(rightX + 16, hpBarY, hpColor, rightX + 16 + hpBarW, hpBarY, hpColor.darker());
        g2d.setPaint(hpGrad);
        int currentHpW = (int) (hpBarW * (Math.max(0, Math.min(100, hp)) / 100.0));
        g2d.fillRoundRect(rightX + 16, hpBarY, currentHpW, 10, 6, 6);
        g2d.setColor(new Color(80, 50, 100));
        g2d.drawRoundRect(rightX + 16, hpBarY, hpBarW, 10, 6, 6);

        // Lives with mini spaceship icons
        int lives = gameManager.getPlayerLives();
        g2d.setFont(UIUtils.FONT_HUD.deriveFont(12f));
        g2d.setColor(UIUtils.TEXT_MUTED);
        g2d.drawString("LIVES:", rightX + 16, 75);

        for (int i = 0; i < 3; i++) {
            int shipIconX = rightX + 65 + i * 22;
            int shipIconY = 64;
            if (i < lives) {
                drawMiniShipIcon(g2d, shipIconX, shipIconY, UIUtils.NEON_CYAN);
            } else {
                drawMiniShipIcon(g2d, shipIconX, shipIconY, new Color(50, 65, 90));
            }
        }

        // Weapon Level Pips
        int weaponTier = gameManager.getPlayer().getWeaponLevel();
        g2d.setColor(UIUtils.TEXT_MUTED);
        g2d.drawString("WEAPON:", rightX + 148, 75);

        for (int p = 1; p <= 3; p++) {
            int pipX = rightX + 210 + (p - 1) * 14;
            int pipY = 66;
            if (p <= weaponTier) {
                g2d.setColor(UIUtils.NEON_GREEN);
                g2d.fillRect(pipX, pipY, 10, 10);
            } else {
                g2d.setColor(new Color(40, 50, 70));
                g2d.drawRect(pipX, pipY, 10, 10);
            }
        }
    }

    private void drawMiniShipIcon(Graphics2D g2d, int cx, int cy, Color color) {
        g2d.setColor(color);
        Polygon p = new Polygon();
        p.addPoint(cx + 6, cy);
        p.addPoint(cx + 12, cy + 12);
        p.addPoint(cx, cy + 12);
        g2d.fillPolygon(p);
    }

    private void drawBossHealthBar(Graphics2D g2d, Boss boss, int w) {
        int barW = Math.min(460, w - 120);
        int barX = (w - barW) / 2;
        int barY = 106;

        // Pulsing Warning Banner
        g2d.setFont(UIUtils.FONT_HUD.deriveFont(Font.BOLD, 13f));
        g2d.setColor(UIUtils.NEON_RED);
        String warnText = "\u26A0 WARNING: DREADNOUGHT FLAGSHIP ACTIVE \u26A0";
        int strW = g2d.getFontMetrics().stringWidth(warnText);
        g2d.drawString(warnText, (w - strW) / 2, barY - 6);

        // Bar Background
        g2d.setColor(new Color(40, 10, 20, 220));
        g2d.fillRoundRect(barX, barY, barW, 16, 8, 8);

        // Bar Fill
        int fillW = (int) (barW * ((double) boss.getHealth() / boss.getMaxHealth()));
        GradientPaint bossGrad = new GradientPaint(
                barX, barY, new Color(255, 30, 80),
                barX + barW, barY, new Color(180, 0, 200));
        g2d.setPaint(bossGrad);
        g2d.fillRoundRect(barX, barY, Math.max(0, fillW), 16, 8, 8);

        // Glowing border
        g2d.setColor(new Color(255, 60, 140));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(barX, barY, barW, 16, 8, 8);

        // Percent Text
        int pct = (int) Math.round(100.0 * boss.getHealth() / boss.getMaxHealth());
        g2d.setColor(Color.WHITE);
        g2d.setFont(UIUtils.FONT_HUD.deriveFont(Font.BOLD, 11f));
        String hpStr = boss.getHealth() + " / " + boss.getMaxHealth() + " HP (" + pct + "%)";
        int hpStrW = g2d.getFontMetrics().stringWidth(hpStr);
        g2d.drawString(hpStr, (w - hpStrW) / 2, barY + 12);
    }

    private void drawLevelCompleteBanner(Graphics2D g2d, int w, int h) {
        int centerX = w / 2;
        int centerY = h / 2;

        AlphaComposite prev = (AlphaComposite) g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, w, h);
        g2d.setComposite(prev);

        UIUtils.drawGlassCard(g2d, centerX - 240, centerY - 80, 480, 160, UIUtils.NEON_GREEN);

        g2d.setColor(UIUtils.NEON_GREEN);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 38));
        String msg = "SECTOR SECURED!";
        int tw = g2d.getFontMetrics().stringWidth(msg);
        g2d.drawString(msg, centerX - tw / 2, centerY - 15);

        g2d.setColor(UIUtils.TEXT_WHITE);
        g2d.setFont(UIUtils.FONT_BODY);
        String sub = "Hyperspace coordinates locked... Initializing next sector jump!";
        int subW = g2d.getFontMetrics().stringWidth(sub);
        g2d.drawString(sub, centerX - subW / 2, centerY + 28);
    }

    private void drawPauseScreen(Graphics2D g2d, int w, int h) {
        int centerX = w / 2;
        int centerY = h / 2;

        // Dark dim backdrop
        AlphaComposite prev = (AlphaComposite) g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.80f));
        g2d.setColor(new Color(5, 8, 18));
        g2d.fillRect(0, 0, w, h);
        g2d.setComposite(prev);

        // Center Pause Card
        int cardW = 420;
        int cardH = 360;
        UIUtils.drawGlassCard(g2d, centerX - cardW / 2, centerY - cardH / 2, cardW, cardH, UIUtils.NEON_CYAN);

        // Title: GAME PAUSED
        g2d.setColor(UIUtils.NEON_CYAN);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 36));
        String title = "GAME PAUSED";
        int tw = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, centerX - tw / 2, centerY - 110);

        // Volume / Mute Row
        drawVolumeControlRow(g2d, centerX, centerY - 35);

        // Buttons
        drawCustomButton(g2d, "RESUME", centerX, centerY + 20, UIUtils.NEON_CYAN);
        drawCustomButton(g2d, "MAIN MENU", centerX, centerY + 75, UIUtils.NEON_BLUE);
        drawCustomButton(g2d, "EXIT GAME", centerX, centerY + 130, UIUtils.NEON_RED);
    }

    private void drawVolumeControlRow(Graphics2D g2d, int centerX, int centerY) {
        SoundManager sm = SoundManager.getInstance();
        int volPct = (int) Math.round(sm.getMasterVolume() * 100);
        boolean isMuted = sm.isMuted();

        // Volume background pill
        g2d.setColor(new Color(16, 24, 46));
        g2d.fillRoundRect(centerX - 150, centerY - 16, 300, 32, 16, 16);
        g2d.setColor(new Color(35, 55, 95));
        g2d.drawRoundRect(centerX - 150, centerY - 16, 300, 32, 16, 16);

        // Minus [ - ] button
        boolean hoverMinus = mousePoint.x >= centerX - 145 && mousePoint.x <= centerX - 95
                && mousePoint.y >= centerY - 16 && mousePoint.y <= centerY + 16;
        g2d.setColor(hoverMinus ? UIUtils.NEON_CYAN : UIUtils.TEXT_MUTED);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g2d.drawString("—", centerX - 128, centerY + 7);

        // Plus [ + ] button
        boolean hoverPlus = mousePoint.x >= centerX + 95 && mousePoint.x <= centerX + 145
                && mousePoint.y >= centerY - 16 && mousePoint.y <= centerY + 16;
        g2d.setColor(hoverPlus ? UIUtils.NEON_CYAN : UIUtils.TEXT_MUTED);
        g2d.drawString("+", centerX + 115, centerY + 8);

        // Center Volume Text / Mute state
        g2d.setFont(UIUtils.FONT_HUD.deriveFont(Font.BOLD, 13f));
        String volText = isMuted ? "\uD83D\uDD07 AUDIO MUTED" : "\uD83D\uDD0A VOLUME: " + volPct + "%";
        g2d.setColor(isMuted ? UIUtils.NEON_RED : UIUtils.TEXT_WHITE);
        int strW = g2d.getFontMetrics().stringWidth(volText);
        g2d.drawString(volText, centerX - strW / 2, centerY + 5);
    }

    private void drawGameOverScreen(Graphics2D g2d, int w, int h) {
        int centerX = w / 2;
        int centerY = h / 2;

        AlphaComposite prev = (AlphaComposite) g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        g2d.setColor(new Color(4, 6, 14));
        g2d.fillRect(0, 0, w, h);
        g2d.setComposite(prev);

        boolean won = gameManager.getLevelManager().isGameComplete();
        Color accentColor = won ? UIUtils.NEON_GOLD : UIUtils.NEON_RED;

        int cardW = 450;
        int cardH = 490;
        UIUtils.drawGlassCard(g2d, centerX - cardW / 2, centerY - cardH / 2 - 25, cardW, cardH, accentColor);

        // Header Title
        g2d.setColor(accentColor);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 34));
        String heading = won ? "\uD83C\uDFC6 GALAXY DEFENDED!" : "\u2620 MISSION FAILED";
        int hw = g2d.getFontMetrics().stringWidth(heading);
        g2d.drawString(heading, centerX - hw / 2, centerY - 200);

        // Stats
        g2d.setColor(Color.WHITE);
        g2d.setFont(UIUtils.FONT_HUD_LARGE);
        String scoreText = "FINAL SCORE: " + gameManager.getScore() + " PTS";
        int sw = g2d.getFontMetrics().stringWidth(scoreText);
        g2d.drawString(scoreText, centerX - sw / 2, centerY - 150);

        g2d.setColor(UIUtils.TEXT_MUTED);
        g2d.setFont(UIUtils.FONT_BODY);
        String infoText = "Pilot: " + playerName + "   |   Sectors Cleared: " + gameManager.getLevelReached() + " / 3";
        int iw = g2d.getFontMetrics().stringWidth(infoText);
        g2d.drawString(infoText, centerX - iw / 2, centerY - 120);

        // Personal High Score notification
        int pb = scoreManager.getPlayerHighScore(playerName);
        if (gameManager.getScore() >= pb && gameManager.getScore() > 0) {
            g2d.setColor(UIUtils.NEON_GOLD);
            g2d.setFont(UIUtils.FONT_BOLD);
            String pbText = "\u2605 NEW PERSONAL RECORD FOR " + playerName.toUpperCase() + "! \u2605";
            int pbW = g2d.getFontMetrics().stringWidth(pbText);
            g2d.drawString(pbText, centerX - pbW / 2, centerY - 90);
        } else if (pb > 0) {
            g2d.setColor(UIUtils.TEXT_MUTED);
            g2d.setFont(UIUtils.FONT_BODY);
            String pbText = "Personal Best: " + pb + " pts";
            int pbW = g2d.getFontMetrics().stringWidth(pbText);
            g2d.drawString(pbText, centerX - pbW / 2, centerY - 90);
        }

        g2d.setColor(new Color(0, 255, 128));
        g2d.setFont(UIUtils.FONT_HUD.deriveFont(12f));
        String autoSavedMsg = "\u2713 Combat score saved to local leaderboard";
        int asmW = g2d.getFontMetrics().stringWidth(autoSavedMsg);
        g2d.drawString(autoSavedMsg, centerX - asmW / 2, centerY - 55);

        // Buttons
        drawCustomButton(g2d, "PLAY AGAIN", centerX, centerY + 15, UIUtils.NEON_CYAN);
        drawCustomButton(g2d, "LEADERBOARD", centerX, centerY + 68, UIUtils.NEON_GOLD);
        drawCustomButton(g2d, "MAIN MENU", centerX, centerY + 121, UIUtils.NEON_BLUE);
        drawCustomButton(g2d, "EXIT GAME", centerX, centerY + 174, UIUtils.NEON_RED);
    }

    private void drawMainMenu(Graphics2D g2d, int w, int h) {
        int centerX = w / 2;
        int centerY = h / 2;

        g2d.setColor(UIUtils.NEON_CYAN);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 46));
        String title = "SPACE SHOOTER";
        int tw = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, centerX - tw / 2, centerY - 60);

        g2d.setFont(UIUtils.FONT_SUBTITLE);
        g2d.setColor(UIUtils.TEXT_MUTED);
        String sub = "DEFEND THE GALAXY";
        int sw = g2d.getFontMetrics().stringWidth(sub);
        g2d.drawString(sub, centerX - sw / 2, centerY - 20);

        drawCustomButton(g2d, "START GAME", centerX, centerY + 30, UIUtils.NEON_CYAN);
    }

    private void drawCustomButton(Graphics2D g2d, String text, int centerX, int centerY, Color accentColor) {
        boolean hovered = isInsideButton(mousePoint.x, mousePoint.y, centerX, centerY);
        int x = centerX - buttonWidth / 2;
        int y = centerY - buttonHeight / 2;

        // Button background
        Color topColor = hovered ? accentColor.darker() : new Color(18, 26, 48);
        Color botColor = hovered ? accentColor.darker().darker() : new Color(12, 16, 32);

        GradientPaint gp = new GradientPaint(x, y, topColor, x, y + buttonHeight, botColor);
        g2d.setPaint(gp);
        g2d.fillRoundRect(x, y, buttonWidth, buttonHeight, 14, 14);

        // Glowing border
        if (hovered) {
            AlphaComposite prev = (AlphaComposite) g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
            g2d.setColor(accentColor);
            g2d.setStroke(new BasicStroke(3.5f));
            g2d.drawRoundRect(x, y, buttonWidth, buttonHeight, 14, 14);
            g2d.setComposite(prev);
        }

        g2d.setColor(hovered ? accentColor : Color.WHITE);
        g2d.setStroke(new BasicStroke(hovered ? 2.0f : 1.2f));
        g2d.drawRoundRect(x, y, buttonWidth, buttonHeight, 14, 14);

        // Text
        g2d.setFont(UIUtils.FONT_BUTTON);
        g2d.setColor(hovered ? Color.WHITE : UIUtils.TEXT_WHITE);
        int tw = g2d.getFontMetrics().stringWidth(text);
        int th = g2d.getFontMetrics().getAscent();
        g2d.drawString(text, centerX - tw / 2, centerY + th / 2 - 2);
    }

    private void saveFinalScore() {
        if (scoreSaved) {
            return;
        }
        scoreSaved = true;

        try {
            scoreManager.saveScore(
                    playerName,
                    gameManager.getScore(),
                    gameManager.getLevelReached());
            System.out.println("Score locally saved for pilot: " + playerName);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not save score locally!\n" + e.getMessage(),
                    "Storage Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}
