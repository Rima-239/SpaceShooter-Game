package com.spaceshooter.entities;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Holographic weapon upgrade capsule with pulsating energy aura
 * and floating neon icon.
 */
public class PowerUp {

    private int x;
    private int y;

    private final int size = 24;
    private final int speed = 2;

    private double animPhase = 0;

    public PowerUp(int x, int y) {
        this.x = x;
        this.y = y;
        this.animPhase = Math.random() * Math.PI * 2;
    }

    // Move power-up downward
    public void update() {
        y += speed;
        animPhase += 0.08;
    }

    // Draw holographic glowing power-up
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = x + size / 2;
        int centerY = y + size / 2;

        // 1. Pulsating Outer Energy Ring
        float pulse = 0.4f + (float) (Math.sin(animPhase) * 0.25f);
        AlphaComposite prev = (AlphaComposite) g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));

        int ringRadius = size + (int) (Math.sin(animPhase * 2) * 3);
        g2d.setColor(new Color(0, 255, 128));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(centerX - ringRadius / 2, centerY - ringRadius / 2, ringRadius, ringRadius);

        // 2. Glowing Orb Core
        g2d.setComposite(prev);
        GradientPaint orbGrad = new GradientPaint(
                x, y, new Color(50, 255, 140),
                x + size, y + size, new Color(0, 160, 70));
        g2d.setPaint(orbGrad);
        g2d.fillOval(x, y, size, size);

        g2d.setColor(new Color(180, 255, 210));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(x, y, size, size);

        // 3. Crisp Glowing "+" Icon
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("+", x + 6, y + 18);
    }

    // Collision area
    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    // Check whether power-up has left the screen
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
