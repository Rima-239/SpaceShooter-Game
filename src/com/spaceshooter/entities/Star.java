package com.spaceshooter.entities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Parallax background star with multi-layered depth, motion streak, and twinkling effect.
 */
public class Star {

    private int x;
    private int y;
    private final int size;
    private final int speed;
    private final Color color;
    private final double twinkleSpeed;
    private double twinklePhase;

    private static final Random RANDOM = new Random();

    public Star(int x, int y, int size, int speed) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = speed;
        this.twinklePhase = RANDOM.nextDouble() * Math.PI * 2;
        this.twinkleSpeed = 0.05 + RANDOM.nextDouble() * 0.08;

        // Color tint based on layer depth
        if (speed >= 3) {
            this.color = new Color(220, 245, 255); // Crisp foreground cyan-white
        } else if (speed == 2) {
            this.color = new Color(170, 205, 255); // Mid-layer ice blue
        } else {
            this.color = new Color(130, 140, 180); // Distant violet/navy star
        }
    }

    public void update(int panelHeight) {
        update(800, panelHeight);
    }

    public void update(int panelWidth, int panelHeight) {
        y += speed;
        twinklePhase += twinkleSpeed;

        if (y > panelHeight) {
            y = 0;
            x = RANDOM.nextInt(Math.max(1, panelWidth));
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Twinkle alpha calculation
        float alpha = 0.5f + (float) (Math.sin(twinklePhase) * 0.45f);
        alpha = Math.max(0.15f, Math.min(1.0f, alpha));

        AlphaComposite origComposite = (AlphaComposite) g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setColor(color);

        if (speed >= 3) {
            // Motion streak for fast foreground stars
            g2d.drawLine(x, y, x, y + speed + 1);
            g2d.fillOval(x - 1, y, size, size);
        } else {
            g2d.fillOval(x, y, size, size);
        }

        g2d.setComposite(origComposite);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
