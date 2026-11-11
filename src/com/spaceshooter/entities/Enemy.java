package com.spaceshooter.entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Alien interceptor drone with sleek angular hull, glowing sensors,
 * and top-mounted thrusters.
 */
public class Enemy {

    private int x;
    private int y;

    private final int width = 40;
    private final int height = 35;

    private int speed;
    private int health = 1;
    private int hitFlashFrames = 0;

    public Enemy(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    public void update() {
        y += speed;
        if (hitFlashFrames > 0) {
            hitFlashFrames--;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = x + width / 2;

        if (hitFlashFrames > 0) {
            // Damage flash
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(x - 8, y, width + 16, height, 8, 8);
            return;
        }

        // 1. Top Thruster Flame (moving down)
        g2d.setColor(new Color(255, 80, 0, 200));
        g2d.fillRect(centerX - 4, y - 5, 8, 5);
        g2d.setColor(new Color(255, 220, 0, 240));
        g2d.fillRect(centerX - 2, y - 3, 4, 3);

        // 2. Wings (Sweep angled down)
        Polygon leftWing = new Polygon();
        leftWing.addPoint(x, y + 8);
        leftWing.addPoint(x - 8, y + 16);
        leftWing.addPoint(x - 8, y + 28);
        leftWing.addPoint(x, y + 24);

        Polygon rightWing = new Polygon();
        rightWing.addPoint(x + width, y + 8);
        rightWing.addPoint(x + width + 8, y + 16);
        rightWing.addPoint(x + width + 8, y + 28);
        rightWing.addPoint(x + width, y + 24);

        GradientPaint wingGrad = new GradientPaint(
                x, y, new Color(140, 15, 30),
                x, y + height, new Color(220, 30, 45));
        g2d.setPaint(wingGrad);
        g2d.fillPolygon(leftWing);
        g2d.fillPolygon(rightWing);

        g2d.setColor(new Color(255, 60, 80));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawPolygon(leftWing);
        g2d.drawPolygon(rightWing);

        // 3. Central Fuselage
        Polygon fuselage = new Polygon();
        fuselage.addPoint(centerX, y + height);    // Bottom nose pointing down
        fuselage.addPoint(x + width, y + 6);       // Top right
        fuselage.addPoint(centerX + 6, y);         // Top notch right
        fuselage.addPoint(centerX - 6, y);         // Top notch left
        fuselage.addPoint(x, y + 6);               // Top left

        GradientPaint hullGrad = new GradientPaint(
                x, y, new Color(180, 20, 40),
                centerX, y + height, new Color(60, 5, 15));
        g2d.setPaint(hullGrad);
        g2d.fillPolygon(fuselage);

        g2d.setColor(new Color(255, 70, 70));
        g2d.drawPolygon(fuselage);

        // 4. Glowing Red Sensor / Cockpit
        Polygon sensor = new Polygon();
        sensor.addPoint(centerX, y + 22);
        sensor.addPoint(centerX + 7, y + 12);
        sensor.addPoint(centerX - 7, y + 12);

        g2d.setColor(new Color(255, 200, 50));
        g2d.fillPolygon(sensor);
        g2d.setColor(Color.WHITE);
        g2d.drawLine(centerX - 2, y + 14, centerX + 2, y + 14);
    }

    public Rectangle getBounds() {
        return new Rectangle(x - 8, y, width + 16, height);
    }

    public boolean isOffScreen(int panelHeight) {
        return y > panelHeight;
    }

    public void takeDamage(int damage) {
        health -= damage;
        hitFlashFrames = 4;
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
}
