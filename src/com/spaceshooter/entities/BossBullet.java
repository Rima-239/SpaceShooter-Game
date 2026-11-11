package com.spaceshooter.entities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Boss plasma blast with glowing magenta aura and high-energy core.
 */
public class BossBullet {

    private int x;
    private int y;

    private final int width = 10;
    private final int height = 16;
    private final int speed = 5;

    public BossBullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        y += speed;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Outer magenta glow
        AlphaComposite prev = (AlphaComposite) g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g2d.setColor(new Color(255, 0, 180));
        g2d.fillRoundRect(x - 3, y - 2, width + 6, height + 4, 8, 8);

        // Core plasma bolt
        g2d.setComposite(prev);
        g2d.setColor(new Color(255, 120, 220));
        g2d.fillRoundRect(x, y, width, height, 4, 4);

        // Hot center
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x + 2, y + 2, width - 4, height / 2);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isOffScreen(int panelHeight) {
        return y > panelHeight;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
