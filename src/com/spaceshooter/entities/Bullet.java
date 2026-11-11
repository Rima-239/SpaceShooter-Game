package com.spaceshooter.entities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Player laser plasma bolt with neon glow aura and bright core.
 */
public class Bullet {

    private int x;
    private int y;

    private final int width = 6;
    private final int height = 15;
    private final int speed = 8;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Move bullet upward
    public void update() {
        y -= speed;
    }

    // Draw glowing bullet
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Outer neon glow
        AlphaComposite prev = (AlphaComposite) g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g2d.setColor(new Color(0, 240, 255));
        g2d.fillRoundRect(x - 3, y - 2, width + 6, height + 4, 6, 6);

        // Core plasma bolt
        g2d.setComposite(prev);
        g2d.setColor(new Color(180, 255, 255));
        g2d.fillRoundRect(x, y, width, height, 4, 4);

        // Bright white center line
        g2d.setColor(Color.WHITE);
        g2d.drawLine(x + width / 2, y + 2, x + width / 2, y + height - 3);
    }

    // Collision area for enemy collisions
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // Check whether bullet has left the screen
    public boolean isOffScreen() {
        return y + height < 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
