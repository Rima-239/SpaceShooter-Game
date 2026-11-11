package com.spaceshooter.entities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Visual particle for explosions, sparks, smoke, and thruster exhaust.
 */
public class Particle {

    public enum Type {
        SPARK,
        SMOKE,
        SHOCKWAVE
    }

    private double x;
    private double y;
    private double vx;
    private double vy;
    private final Color color;
    private final float size;
    private int life;
    private final int maxLife;
    private final Type type;

    public Particle(double x, double y, double vx, double vy, Color color, float size, int maxLife, Type type) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
        this.size = size;
        this.maxLife = maxLife;
        this.life = maxLife;
        this.type = type;
    }

    public void update() {
        x += vx;
        y += vy;
        vx *= 0.96; // drag
        vy *= 0.96;
        life--;
    }

    public boolean isDead() {
        return life <= 0;
    }

    public void draw(Graphics2D g2d) {
        if (isDead()) return;

        float alpha = Math.max(0f, Math.min(1f, (float) life / maxLife));
        AlphaComposite prevComposite = (AlphaComposite) g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (type == Type.SHOCKWAVE) {
            float currentRadius = size * (1.0f + (1.0f - alpha) * 2.5f);
            g2d.setColor(color);
            g2d.drawOval((int) (x - currentRadius / 2), (int) (y - currentRadius / 2), (int) currentRadius, (int) currentRadius);
        } else if (type == Type.SMOKE) {
            float currentSize = size * (1.0f + (1.0f - alpha) * 1.5f);
            g2d.setColor(color);
            g2d.fillOval((int) (x - currentSize / 2), (int) (y - currentSize / 2), (int) currentSize, (int) currentSize);
        } else {
            // SPARK
            g2d.setColor(color);
            g2d.fillOval((int) (x - size / 2), (int) (y - size / 2), (int) size, (int) size);
        }

        g2d.setComposite(prevComposite);
    }
}

