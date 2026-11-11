package com.spaceshooter.entities;

import java.awt.AlphaComposite;
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
 * Player spaceship entity with advanced Java2D vector graphics,
 * animated ion thrusters, energy shields, and multi-tier plasma weapons.
 */
public class Player {

    private int x;
    private int y;

    private final int width = 40;
    private final int height = 40;

    private final int speed = 5;

    private boolean movingUp;
    private boolean movingDown;
    private boolean movingLeft;
    private boolean movingRight;
    private boolean shooting;

    // Health and lives
    private int health = 100;
    private int lives = 3;
    private int weaponLevel = 1;

    // Prevents damage temporarily after respawning
    private boolean invulnerable = false;

    // Animation timers
    private int thrusterFrame = 0;
    private int damageFlashFrames = 0;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Upgrade the player's weapon
    public void upgradeWeapon() {
        if (weaponLevel < 3) {
            weaponLevel++;
        }
    }

    // Get current weapon level
    public int getWeaponLevel() {
        return weaponLevel;
    }

    // Reset weapon level when restarting
    public void resetWeapon() {
        weaponLevel = 1;
    }

    // Update player movement
    public void update() {
        if (movingUp) {
            y -= speed;
        }
        if (movingDown) {
            y += speed;
        }
        if (movingLeft) {
            x -= speed;
        }
        if (movingRight) {
            x += speed;
        }

        thrusterFrame++;
        if (damageFlashFrames > 0) {
            damageFlashFrames--;
        }
    }

    // Draw the spaceship
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Blink the player while invulnerable
        if (invulnerable && System.currentTimeMillis() % 260 < 130) {
            return;
        }

        int centerX = x + width / 2;

        // 1. Animated Dual Thruster Flames
        int flameHeight = 8 + (int) (Math.sin(thrusterFrame * 0.4) * 4);
        if (movingUp) flameHeight += 4;

        // Left thruster flame
        GradientPaint flameGrad = new GradientPaint(
                centerX - 8, y + height - 2, new Color(0, 240, 255, 230),
                centerX - 8, y + height + flameHeight, new Color(255, 100, 0, 0));
        g2d.setPaint(flameGrad);
        int[] leftFlameX = {centerX - 12, centerX - 8, centerX - 4};
        int[] leftFlameY = {y + height - 2, y + height + flameHeight, y + height - 2};
        g2d.fillPolygon(leftFlameX, leftFlameY, 3);

        // Right thruster flame
        GradientPaint flameGradRight = new GradientPaint(
                centerX + 8, y + height - 2, new Color(0, 240, 255, 230),
                centerX + 8, y + height + flameHeight, new Color(255, 100, 0, 0));
        g2d.setPaint(flameGradRight);
        int[] rightFlameX = {centerX + 4, centerX + 8, centerX + 12};
        int[] rightFlameY = {y + height - 2, y + height + flameHeight, y + height - 2};
        g2d.fillPolygon(rightFlameX, rightFlameY, 3);

        // 2. Spaceship Hull
        Polygon hull = new Polygon();
        hull.addPoint(centerX, y);               // Nose
        hull.addPoint(x + width, y + height - 4);// Right wingtip
        hull.addPoint(centerX + 10, y + height - 8); // Right thruster notch
        hull.addPoint(centerX + 6, y + height);  // Right thruster
        hull.addPoint(centerX - 6, y + height);  // Center exhaust
        hull.addPoint(centerX - 10, y + height - 8); // Left thruster notch
        hull.addPoint(x, y + height - 4);        // Left wingtip

        if (damageFlashFrames > 0) {
            g2d.setColor(new Color(255, 255, 255, 240));
            g2d.fillPolygon(hull);
        } else {
            // High-tech gradient hull
            GradientPaint hullGrad = new GradientPaint(
                    x, y, new Color(15, 30, 60),
                    x + width, y + height, new Color(30, 80, 140));
            g2d.setPaint(hullGrad);
            g2d.fillPolygon(hull);

            // Neon cyan edge highlights
            g2d.setColor(new Color(0, 240, 255));
            g2d.setStroke(new BasicStroke(1.8f));
            g2d.drawPolygon(hull);

            // Wing cannon pods
            g2d.setColor(new Color(0, 180, 255));
            g2d.fillRect(x - 2, y + height - 14, 4, 10);
            g2d.fillRect(x + width - 2, y + height - 14, 4, 10);

            // Cockpit Canopy (Glowing cyan/blue glass)
            Polygon cockpit = new Polygon();
            cockpit.addPoint(centerX, y + 8);
            cockpit.addPoint(centerX + 5, y + 24);
            cockpit.addPoint(centerX - 5, y + 24);

            GradientPaint canopyGrad = new GradientPaint(
                    centerX, y + 8, new Color(160, 250, 255),
                    centerX, y + 24, new Color(0, 130, 210));
            g2d.setPaint(canopyGrad);
            g2d.fillPolygon(cockpit);

            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawLine(centerX - 1, y + 10, centerX - 1, y + 20); // Reflection shine
        }

        // 3. Energy Shield Bubble while invulnerable
        if (invulnerable) {
            AlphaComposite prev = (AlphaComposite) g2d.getComposite();
            float shieldPulse = 0.4f + (float) (Math.sin(thrusterFrame * 0.25) * 0.2f);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, shieldPulse));

            g2d.setColor(new Color(0, 240, 255));
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(x - 6, y - 6, width + 12, height + 12);

            g2d.setColor(new Color(0, 180, 255, 60));
            g2d.fillOval(x - 6, y - 6, width + 12, height + 12);

            g2d.setComposite(prev);
        }
    }

    // Shoot bullets based on current weapon level
    public List<Bullet> shootBullets() {
        List<Bullet> bullets = new ArrayList<>();
        int centerX = x + width / 2 - 3;
        int bulletY = y;

        // Level 1: one center plasma bolt
        if (weaponLevel == 1) {
            bullets.add(new Bullet(centerX, bulletY));
        }
        // Level 2: two twin plasma cannons
        else if (weaponLevel == 2) {
            bullets.add(new Bullet(centerX - 10, bulletY));
            bullets.add(new Bullet(centerX + 10, bulletY));
        }
        // Level 3: three heavy plasma cannons (center + spread)
        else {
            bullets.add(new Bullet(centerX, bulletY));
            bullets.add(new Bullet(centerX - 15, bulletY + 5));
            bullets.add(new Bullet(centerX + 15, bulletY + 5));
        }

        return bullets;
    }

    // Return the player's collision area
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // Shooting controls
    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }

    public boolean isShooting() {
        return shooting;
    }

    // Health management
    public void takeDamage(int damage) {
        if (invulnerable) {
            return;
        }

        health -= damage;
        damageFlashFrames = 6;

        if (health < 0) {
            health = 0;
        }
    }

    public int getHealth() {
        return health;
    }

    public void resetHealth() {
        health = 100;
    }

    // Movement controls
    public void setMovingUp(boolean movingUp) {
        this.movingUp = movingUp;
    }

    public void setMovingDown(boolean movingDown) {
        this.movingDown = movingDown;
    }

    public void setMovingLeft(boolean movingLeft) {
        this.movingLeft = movingLeft;
    }

    public void setMovingRight(boolean movingRight) {
        this.movingRight = movingRight;
    }

    public boolean isMovingUp() {
        return movingUp;
    }

    public boolean isMovingDown() {
        return movingDown;
    }

    public boolean isMovingLeft() {
        return movingLeft;
    }

    public boolean isMovingRight() {
        return movingRight;
    }

    // Lives management
    public int getLives() {
        return lives;
    }

    public void loseLife() {
        lives--;
        if (lives < 0) {
            lives = 0;
        }
    }

    // Invulnerability management
    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    // Respawn player after losing a life
    public void respawn(int panelWidth, int panelHeight) {
        x = panelWidth / 2 - width / 2;
        y = panelHeight - 100;

        resetHealth();

        // Reset movement and shooting
        movingUp = false;
        movingDown = false;
        movingLeft = false;
        movingRight = false;
        shooting = false;
    }

    // Keep player inside the game window
    public void keepInsideScreen(int panelWidth, int panelHeight) {
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (x + width > panelWidth) {
            x = panelWidth - width;
        }
        if (y + height > panelHeight) {
            y = panelHeight - height;
        }
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
