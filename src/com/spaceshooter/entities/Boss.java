package com.spaceshooter.entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * Dreadnought Boss flagship with pulsing plasma core, twin laser turrets,
 * heavy armor plating, and animated thruster exhaust.
 */
public class Boss {

    private int x;
    private int y;

    private final int width = 100;
    private final int height = 60;

    private int health;
    private final int maxHealth;

    private int speed = 3;
    private boolean movingRight = true;

    // Boss bullets
    private List<BossBullet> bullets = new ArrayList<>();

    // Boss attack cooldown
    private int attackCooldown = 60;
    private final int attackDelay = 60;

    private int hitFlashFrames = 0;
    private int animFrame = 0;
    private int currentPanelHeight = 600;

    public Boss(int panelWidth) {
        this(panelWidth, 1.0);
    }

    public Boss(int panelWidth, double healthMultiplier) {
        x = panelWidth / 2 - width / 2;
        y = 50;

        maxHealth = (int) Math.round(30 * healthMultiplier);
        health = maxHealth;
    }

    // Move boss and update attacks
    public void update(int panelWidth) {
        update(panelWidth, currentPanelHeight);
    }

    public void update(int panelWidth, int panelHeight) {
        this.currentPanelHeight = panelHeight;
        animFrame++;

        if (movingRight) {
            x += speed;
        } else {
            x -= speed;
        }

        // Reverse direction at screen edges
        if (x + width >= panelWidth) {
            x = panelWidth - width;
            movingRight = false;
        }

        if (x <= 0) {
            x = 0;
            movingRight = true;
        }

        if (hitFlashFrames > 0) {
            hitFlashFrames--;
        }

        updateAttack(panelHeight);
    }

    // Boss attack logic
    public void updateAttack() {
        updateAttack(currentPanelHeight);
    }

    public void updateAttack(int panelHeight) {
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        if (attackCooldown == 0) {
            bullets.add(new BossBullet(
                    x + width / 2 - 5,
                    y + height));

            attackCooldown = attackDelay;
        }

        for (BossBullet bullet : bullets) {
            bullet.update();
        }

        bullets.removeIf(bullet -> bullet.isOffScreen(panelHeight));
    }

    public List<BossBullet> getBullets() {
        return bullets;
    }

    public void resetBullets() {
        bullets.clear();
        attackCooldown = attackDelay;
    }

    // Draw boss with vector graphics and energy effects
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = x + width / 2;

        if (hitFlashFrames > 0) {
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(x - 5, y - 5, width + 10, height + 10, 12, 12);
            return;
        }

        // 1. Top Thruster Flames (moving down into combat)
        int flameLen = 6 + (int) (Math.sin(animFrame * 0.3) * 4);
        g2d.setColor(new Color(255, 0, 150, 180));
        g2d.fillRect(x + 20, y - flameLen, 12, flameLen);
        g2d.fillRect(x + width - 32, y - flameLen, 12, flameLen);

        g2d.setColor(new Color(255, 180, 255, 230));
        g2d.fillRect(x + 23, y - (flameLen - 2), 6, flameLen - 2);
        g2d.fillRect(x + width - 29, y - (flameLen - 2), 6, flameLen - 2);

        // 2. Heavy Armored Wings
        Polygon leftWing = new Polygon();
        leftWing.addPoint(x, y + 10);
        leftWing.addPoint(x + 30, y);
        leftWing.addPoint(x + 35, y + height);
        leftWing.addPoint(x - 5, y + height - 10);

        Polygon rightWing = new Polygon();
        rightWing.addPoint(x + width, y + 10);
        rightWing.addPoint(x + width - 30, y);
        rightWing.addPoint(x + width - 35, y + height);
        rightWing.addPoint(x + width + 5, y + height - 10);

        GradientPaint wingGrad = new GradientPaint(
                x, y, new Color(80, 20, 85),
                x, y + height, new Color(40, 10, 50));
        g2d.setPaint(wingGrad);
        g2d.fillPolygon(leftWing);
        g2d.fillPolygon(rightWing);

        g2d.setColor(new Color(255, 0, 180));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawPolygon(leftWing);
        g2d.drawPolygon(rightWing);

        // 3. Central Dreadnought Hull
        GradientPaint hullGrad = new GradientPaint(
                centerX, y, new Color(50, 15, 60),
                centerX, y + height, new Color(25, 8, 30));
        g2d.setPaint(hullGrad);
        g2d.fillRoundRect(x + 15, y + 5, width - 30, height - 10, 12, 12);

        g2d.setColor(new Color(200, 40, 220));
        g2d.drawRoundRect(x + 15, y + 5, width - 30, height - 10, 12, 12);

        // 4. Twin Forward Plasma Cannon Turrets
        g2d.setColor(new Color(220, 20, 60));
        g2d.fillRoundRect(x + 18, y + height - 14, 16, 18, 4, 4);
        g2d.fillRoundRect(x + width - 34, y + height - 14, 16, 18, 4, 4);

        g2d.setColor(new Color(255, 120, 180));
        g2d.drawRoundRect(x + 18, y + height - 14, 16, 18, 4, 4);
        g2d.drawRoundRect(x + width - 34, y + height - 14, 16, 18, 4, 4);

        // 5. Pulsing Dark Matter Reactor Core
        int corePulse = (int) (Math.sin(animFrame * 0.2) * 3);
        int coreSize = 16 + corePulse;
        g2d.setColor(new Color(255, 0, 200, 140));
        g2d.fillOval(centerX - coreSize / 2, y + height / 2 - coreSize / 2, coreSize, coreSize);

        g2d.setColor(new Color(255, 200, 255));
        g2d.fillOval(centerX - 4, y + height / 2 - 4, 8, 8);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void takeDamage(int damage) {
        health -= damage;
        hitFlashFrames = 4;
        if (health < 0) {
            health = 0;
        }
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
